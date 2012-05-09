/*
 *  Copyright (C) 2012  David Brodsky
 *	This file is part of Open BART.
 *
 *  Open BART is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Open BART is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Open BART.  If not, see <http://www.gnu.org/licenses/>.
*/


package pro.dbro.bart;

import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


// TODO: Look at Intent to send on notification click to ENSURE stop service label is created in TheActivity

public class UsherService extends Service {
    private NotificationManager mNM;
    
    private PendingIntent contentIntent;
    
    private Context c;
    
    private Notification notification; // keep an instance of the notification to update time text
    
    private int currentLeg; // keep track of which leg of the route we're currently on
    private boolean didBoard; // keep track of whether we've boarded the current leg, or are waiting for it to arrive
    private CountDownTimer timer; // keep track of current countdown for cancelling if new request comes
    							  // else we can get errors related to a timer expecting previous route
    private CountDownTimer reminderTimer; // timer separated from actual timer by REMINDER_PADDING
    private route usherRoute;	// the route to guide along. Updated with etd data
    
    private long REMINDER_PADDING = 120*1000; // ms before an event (board, disembark) the usher should issue a reminder

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        UsherService getService() {
            return UsherService.this;
        }
    }
    
    @Override
    public void onCreate() {
    	c = this;
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Log.v("Usher","OnCreate");
     // LocalBroadCast Stuff
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceDataMessageReceiver,
        	      new IntentFilter("service_status_change"));
        // Display a notification about us starting.  We put an icon in the status bar.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	//TODO: Check if service-start should be sent OnCreate
    	sendMessage(1); // send service-start message
    	usherRoute = TheActivity.usherRoute;
        Log.i("UsherService", "Received start id " + startId + ": " + intent);
        if(timer != null){
        	timer.cancel();
        }
        if(reminderTimer != null){
        	reminderTimer.cancel();
        }
        
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        showNotification();
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
    	super.onDestroy();
    	sendMessage(0); // send service-stopped message
        Log.d("onDestroy","called");
        
    	if(timer != null)
    		timer.cancel();
    	if(reminderTimer != null)
    		reminderTimer.cancel();
    	// Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
        //Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
        
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Initialize first guidance timer and send initial notification
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        //CharSequence text = getText(R.string.local_service_started);
    
    	String destinationStation = ((leg)usherRoute.legs.get(usherRoute.legs.size()-1)).disembarkStation;
    	currentLeg = 0;
    	didBoard = false;
    	CharSequence tickerText = "Guiding to " + TheActivity.REVERSE_STATION_MAP.get(destinationStation.toLowerCase());

        // Set the info for the views that show in the notification panel.
        Date now = new Date();
        long minutesUntilNext = ((((leg)usherRoute.legs.get(0)).boardTime.getTime() - now.getTime()));
        //minutesUntilNext is, for this brief moment, actually milliseconds. 
        makeLegCountdownTimer(minutesUntilNext);
        // back to minutes
        minutesUntilNext = (minutesUntilNext)/(1000*60);
        //catch negative time state
        if(minutesUntilNext < 0){
        	Log.v("Negative ETA", "Catch me");
        }
        
        CharSequence currentStepText = "At " + TheActivity.REVERSE_STATION_MAP.get(((leg)usherRoute.legs.get(0)).boardStation.toLowerCase());
        CharSequence nextStepText = "Board "+ TheActivity.REVERSE_STATION_MAP.get(((leg)usherRoute.legs.get(0)).trainHeadStation.toLowerCase()) + " train in " + String.valueOf(minutesUntilNext) + "m";

     // The PendingIntent to launch our activity if the user selects this notification
        Intent i = new Intent(this, TheActivity.class);
        i.putExtra("Service", true);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        contentIntent = PendingIntent.getActivity(this, 0,
                i, PendingIntent.FLAG_CANCEL_CURRENT);
        
        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c);
        
        builder.setContentIntent(contentIntent)
        .setSmallIcon(R.drawable.ic_launcher_notification)
        //.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.some_big_img))
        .setTicker(tickerText)
        .setWhen(0)
        //.setAutoCancel(true)
        .setContentTitle(currentStepText)
        .setContentText(nextStepText)
        .setOngoing(true);
        
        notification = builder.getNotification();

        mNM.notify(NOTIFICATION, notification);
    }
    
    //if newNotification is true, generate new notification with scroller text
    //else, simply update menu item text
    private void updateNotification(boolean newNotification){
    	Date now = new Date();
    	CharSequence nextStepText ="";
    	CharSequence currentStepText = "";
    	if(didBoard){
    		currentStepText = "On " + TheActivity.REVERSE_STATION_MAP.get(((leg)usherRoute.legs.get(currentLeg)).trainHeadStation.toLowerCase())+ " train";
    		long minutesUntilNext = ((((leg)usherRoute.legs.get(currentLeg)).disembarkTime.getTime() - now.getTime())/(1000*60));
    		if(minutesUntilNext < 0){
            	Log.v("Negative ETA", "Catch me");
            }
    		if(currentLeg+1 == usherRoute.legs.size()){
    			nextStepText = "Get off at "+ TheActivity.REVERSE_STATION_MAP.get(((leg)usherRoute.legs.get(currentLeg)).disembarkStation.toLowerCase()) + " in " + String.valueOf(minutesUntilNext) + "m";
    		}
    		else{
    			nextStepText = "Transfer at "+ TheActivity.REVERSE_STATION_MAP.get(((leg)usherRoute.legs.get(currentLeg)).disembarkStation.toLowerCase()) + " in " + String.valueOf(minutesUntilNext) + "m";
    		}
    	}
    	else{
    		long minutesUntilNext = ((((leg)usherRoute.legs.get(currentLeg)).boardTime.getTime() - now.getTime())/(1000*60));
    		if(minutesUntilNext < 0){
            	Log.v("Negative ETA", "Catch me");
            }
    		nextStepText = "Board "+ TheActivity.REVERSE_STATION_MAP.get(((leg)usherRoute.legs.get(currentLeg)).trainHeadStation.toLowerCase()) + " train in " + String.valueOf(minutesUntilNext) + "m";
    		currentStepText = "At " + TheActivity.REVERSE_STATION_MAP.get(((leg)usherRoute.legs.get(currentLeg)).boardStation.toLowerCase());
    	}
        
    	NotificationCompat.Builder builder = new NotificationCompat.Builder(c);
    	builder.setContentIntent(contentIntent)
        .setSmallIcon(R.drawable.ic_launcher_notification)
        //.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.some_big_img))
        .setWhen(0)
        .setContentTitle(currentStepText)
        .setContentText(nextStepText)
        .setOngoing(true);
    	
        if(newNotification){
            builder.setTicker(nextStepText);
    	}

        notification = builder.getNotification();
        mNM.notify(NOTIFICATION, notification);
                
    }
    
    private void makeLegCountdownTimer(long msUntilNext){
    	//make sure we don't leak any timers
    	if(timer != null)
    		timer.cancel();
    	
    	
    	timer = new CountDownTimer(msUntilNext, 60000){
            //new CountDownTimer(5000, 1000){

    			@Override
    			public void onFinish() {
    				// TODO Auto-generated method stub
    				Vibrator v = (Vibrator) getSystemService(c.VIBRATOR_SERVICE);
    				long[] vPattern = {0,200,200,200,200,200,200,200};
    				v.vibrate(vPattern,-1);
    				//if(didBoard) // if we've boarded, we're handling the last leg
    				//	currentLeg ++;
    				didBoard = !didBoard;
    				if(didBoard)
    					Log.v("UsherState","leg: "+ String.valueOf(currentLeg)+ " / "+String.valueOf(usherRoute.legs.size())+" Boarded");
    				else
    					Log.v("UsherState","leg: "+ String.valueOf(currentLeg)+ " / "+String.valueOf(usherRoute.legs.size())+" Waiting");
    				
    				if ((usherRoute.legs.size() == currentLeg+1) && !didBoard){
    					NotificationCompat.Builder builder = new NotificationCompat.Builder(c);
    			    	builder.setContentIntent(contentIntent)
    			        .setSmallIcon(R.drawable.ic_launcher_notification)
    			        //.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.some_big_img))
    			        .setWhen(0)
    			        .setTicker("This is your stop! Take care!");
    			        
    			        notification = builder.getNotification();
    			        mNM.notify(NOTIFICATION, notification);
    			        //TheActivity.removeStopServiceText();
    			        stopSelf();
    				}
    				else if(didBoard){ //Set timer for this leg's disembark time
    					Date now = new Date();
    			        long msUntilNext = ((((leg)usherRoute.legs.get(currentLeg)).disembarkTime.getTime() - now.getTime()));
    					makeLegCountdownTimer(msUntilNext);
    					updateNotification(true);
    				}
    				else{ // Set timer for next leg's board time
    					currentLeg ++;
    					Date now = new Date();
    			        long msUntilNext = ((((leg)usherRoute.legs.get(currentLeg)).boardTime.getTime() - now.getTime()));
    					makeLegCountdownTimer(msUntilNext);
    					updateNotification(true);
    				}
    			}
    			
    			@Override
    			public void onTick(long arg0) {
    				updateNotification(false);				
    			}
            	
            }.start();
            //timer.start();
            // Set Reminder timer REMINDER_PADDING ms before event
            if(msUntilNext > (REMINDER_PADDING+30*1000)){ // if next event is more than 30 seconds + REMINDER_PADDING out, set reminder
            	//avoid leaking timer
            	if(reminderTimer != null)
            		reminderTimer.cancel();
	            reminderTimer = new CountDownTimer(msUntilNext - REMINDER_PADDING, msUntilNext - REMINDER_PADDING){
	
					@Override
					public void onFinish() {
						// TODO: Is this a good time for updating?
						//requestDataUpdate();
						updateNotification(true);
						Vibrator v = (Vibrator) getSystemService(c.VIBRATOR_SERVICE);
	    				long[] vPattern = {0,400,300,400};
	    				v.vibrate(vPattern,-1);
						
					}
	
					@Override
					public void onTick(long millisUntilFinished) {
						// TODO Auto-generated method stub
						
					}
	            }.start();
            }
         }
    
    private void sendMessage(int status) { // 0 = service stopped , 1 = service started
    	  Log.d("sender", "Broadcasting message");
    	  Intent intent = new Intent("service_status_change");
    	  // You can also include some extra data.
    	  intent.putExtra("status", status);
    	  LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    
    private BroadcastReceiver serviceDataMessageReceiver = new BroadcastReceiver() {
  	  @Override
  	  public void onReceive(Context context, Intent intent) {
  	    // Get extra data included in the Intent
  	    int status = intent.getIntExtra("status", -1);
  	    if(status == 5){ // service stopped
  	    	//update timers with fresh data
  	    	updateTimersWithEtdResponse((etdResponse)intent.getSerializableExtra("etdResponse"));
  	    }
  	  }
  	};
  	
  	private void requestDataUpdate(){
  		String curStation;
  		if( didBoard){
  			curStation = ((leg)usherRoute.legs.get(currentLeg)).disembarkStation.toLowerCase();
  		}
  		else{
  			curStation = ((leg)usherRoute.legs.get(currentLeg)).boardStation.toLowerCase();
  		}

  		new RequestTask("etd", false).execute(TheActivity.BART_API_ROOT+"etd.aspx?cmd=etd&orig="+curStation+"&key="+TheActivity.BART_API_KEY);
  	}
  	// TODO: I've gotten NullPointerException pointed here ?
  	//04-11 18:24:42.420: E/AndroidRuntime(6698): java.lang.NullPointerException
  	//04-11 18:24:42.420: E/AndroidRuntime(6698): 	at pro.dbro.bart.UsherService.updateTimersWithEtdResponse(UsherService.java:319)

  	private void updateTimersWithEtdResponse(etdResponse response){
  		leg curLeg = (leg)usherRoute.legs.get(currentLeg);
  		long curTargetTime; // time until next move according to schedule
  		int etd = -1;
  		/*
  		for(int y=0;y<response.etds.size();y++){
    		// DEBUG
    		try{
    			//Check that destination train is listed in terminal-station format. Ex: "Fremont" CounterEx: 'SFO/Milbrae'
    			if (!TheActivity.STATION_MAP.containsKey(((etd)response.etds.get(y)).destination)){
    				// If this is not a known silly-named train terminal station
    				if (!TheActivity.KNOWN_SILLY_TRAINS.containsKey(((etd)response.etds.get(y)).destination)){
    					// Let's try and guess what it is
    					boolean station_guessed = false;
    					for(int z = 0; z< TheActivity.STATIONS.length; z++){
    						
    						// Can we match a station name within the silly-train name?
    						// haystack.indexOf(needle1);
    						if ( (((etd)response.etds.get(y)).destination).indexOf(TheActivity.STATIONS[z]) != -1){
    							// Set the etd destination to the guessed real station name
    							((etd)response.etds.get(y)).destination = TheActivity.STATIONS[z];
    							station_guessed = true;
    						}
    					}
    					if (!station_guessed){
    						break; //We have to give up on updating routes based on this utterly silly-named etd
    					}
    				}
    				else{
    					// Set the etd destination station to the real station name
    					((etd)response.etds.get(y)).destination = KNOWN_SILLY_TRAINS.get(((etd)response.etds.get(y)).destination);
    					//break;
    				}		
    			} // end STATION_MAP silly-name train check and replace
    			
    				// Comparing BART station abbreviations
    			if (TheActivity.STATION_MAP.get(((etd)response.etds.get(y)).destination).compareTo(((leg)((route)input.routes.get(x)).legs.get(0)).trainHeadStation) == 0 ){
	    			//If matching etd is not all ready matched to a route, match it to this one
    				if (!routeToEtd.containsKey(x) && !routeToEtd.containsValue(y)){
	    				routeToEtd.put(x, y);
    				}
    				else{
    					//if the etd is all ready claimed by a route, go to next etd
    					break;
    				}
	    		}
	    		else if (TheActivity.STATION_MAP.get(((etd)currentEtdResponse.etds.get(y)).destination).compareTo(((leg)((route)input.routes.get(x)).legs.get(lastLeg)).trainHeadStation) == 0 ){
	    			if (!routeToEtd.containsKey(x) && !routeToEtd.containsValue(y)){
	    				routeToEtd.put(x, y);
    				}
    				else{
    					//if the etd is all ready claimed by a route, go to next etd
    					break;
    				}
	    		}
    			
    		}catch(Throwable T){
    			// Likely, a train with destination listed as a
    			// special tuple and not an actual station name
    			// was encountered 
    			Log.v("WTF", "Find me");
    		}
    		}// end etd for loop
  		*/
  		for(int x=0;x<response.etds.size();x++){
  			//find the etd of response which matches current train
  			//check this logic
  			//crash here
  			/* old method
  			if(curLeg.trainHeadStation.compareTo(TheActivity.STATION_MAP.get(((etd)response.etds.get(x)).destination)) == 0){
  				etd = x;
  				break;
  			}
  			*/
  		}
  		//something went wrong
  		if(etd == -1)
  			return;
  		long etdTargetTime = ((etd)response.etds.get(etd)).minutesToArrival*60*1000;
  		
  		
  		Date now = new Date();
  		if(didBoard){
  			curTargetTime = curLeg.disembarkTime.getTime(); // for debug only
  			// update the usherRoute disembarkTime by adding minutesToArrival to new Date()
  			curLeg.disembarkTime = new Date(now.getTime() + ((etd)response.etds.get(etd)).minutesToArrival*60*1000);
  			makeLegCountdownTimer(curLeg.disembarkTime.getTime());
  		}
  		else{
  			curTargetTime = curLeg.boardTime.getTime(); // for debug only
  			// update the usherRoute boardTime by adding minutesToArrival to new Date()
  			curLeg.boardTime = new Date(now.getTime() + ((etd)response.etds.get(etd)).minutesToArrival*60*1000);
  			makeLegCountdownTimer(curLeg.boardTime.getTime());
  		}
  		
  		Log.v("USHER SYNC",String.valueOf((etdTargetTime-curTargetTime)/1000)); // s diff b/t current and etd
  		
  	}
}

