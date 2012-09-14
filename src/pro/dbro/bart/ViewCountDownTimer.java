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

import java.util.ArrayList;
import java.util.Date;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

public class ViewCountDownTimer extends CountDownTimer {
	
	// views to be counted down by timer
	ArrayList timerViews;
	// type of request: "etd" or "route" This reveals the format of views within timerViews
	String request;
	
	// absolute time when countdown will be finished in ms since 1970
	public static long expiryTime;
	
	static final long DEPARTING_TRAIN_PADDING_MS = 15*1000; // how long after departure should we display train?
															  // since timer polls only once per minute setting <60s
															  // only effectively removes trains leaving when request is first sent
	static final long MINIMUM_TICK_MS = 1*1000; // disable timer from ticking onStart(); Causes flickering when set on view that has just been
												 // populated with a time. 
	private long COUNTDOWN_TIME_MS; // initial countdown time

	public ViewCountDownTimer(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		// TODO Auto-generated constructor stub
	}
	public ViewCountDownTimer(ArrayList tViews, String request, long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		COUNTDOWN_TIME_MS = millisInFuture;
		timerViews = tViews;
		this.request = request;
		expiryTime = new Date().getTime() + millisInFuture;
		
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		//broadcast message to TheActivity to refresh data from the BART API
		sendMessage(2);
	}

	@Override
	public void onTick(long millisUntilFinished) {
		// Prevent timer from ticking within MINIMUM_TICK_MS of beginning
		// Prevents unnecessary view re-drawing, assuming UI initial data is updated before timer set
		if(COUNTDOWN_TIME_MS - millisUntilFinished < MINIMUM_TICK_MS){
			return;
		}
		// Get current time in MS since '70
		long now = new Date().getTime();
		
		for(int x=0;x<timerViews.size();x++){
			// ETA tagged on TimerView is ms since '70
			long eta = (Long) ((TextView)timerViews.get(x)).getTag();
			
			// If train was scheduled to leave more than DEPARTING_TRAIN_PADDING_MS ago, remove it from view
			if((eta + DEPARTING_TRAIN_PADDING_MS - now ) < 0){
				// If an etd or route countdown has expired, hide it's view
				try{
					if(request.compareTo("route") == 0){
	
						View parent = ((View) ((TextView)timerViews.get(x)).getParent());
						route thisRoute = (route)parent.getTag();
						parent.setVisibility(View.GONE);
						if (thisRoute.isExpanded){
							ViewGroup grandparent = (ViewGroup) parent.getParent();
							//if route view is expanded, the next row in Table will be route detail
							grandparent.getChildAt((grandparent.indexOfChild(parent)+1)).setVisibility(View.GONE);
						}
						//tableLayout.removeView((View)((View)timerViews.get(x)).getParent());
					}
					else if(request.compareTo("etd") == 0){
						((TextView)timerViews.get(x)).setVisibility(View.GONE);
					}
				}catch(Throwable t){
					// removing departed trains is a 'garnish' so let's not 
					// worry TOO much if some weird casting exception crops up
				}
				
			}
			// Else if train was scheduled to leave less than DEPARTING_TRAIN_PADDING_MS ago, show it in view, but set eta = 0
			// set eta = 0 tied to display "<1m". Due to integer division time between 0 and 60*1000-1 ms will be displayed as 0
			else if((eta - now ) <= 60*1000){
				eta = 0;
			}
			else{
				eta = eta - now;
			}
			// Display 0 eta as "<1"
			if(eta == 0)
				((TextView)timerViews.get(x)).setText("<1");
			else
				((TextView)timerViews.get(x)).setText(String.valueOf((eta) / ( 1000 *60)));
		}
	}
	
	private void sendMessage(int status) { // 0 = service stopped , 1 = service started, 2 = refresh view with call to bartApiRequest()
  	  Log.d("sender", "View countdown expired");
  	  Intent intent = new Intent("service_status_change");
  	  // You can also include some extra data.
  	  intent.putExtra("status", status);
  	  intent.putExtra("request", request);
  	  LocalBroadcastManager.getInstance(TheActivity.c).sendBroadcast(intent);
  	}

}
