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

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.crittercism.app.Crittercism;
import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.XMLParserException;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;
import com.thebuzzmedia.sjxp.rule.IRule.Type;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class BartRouteParser extends AsyncTask<String, String, routeResponse> {
	String responseDate;
	String responseTime;
	String routeOriginDate;
	String routeOriginTime;
	String routeDestinationDate;
	String routeDestinationTime;
	String legTime;
	String legDate;
	SimpleDateFormat curFormater;
	Date dateObj;
	TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
	boolean updateUI;
	
	//DEBUGGING
	boolean dateError = false;
	
	BartRouteParser(boolean updateUI) {
        this.updateUI = updateUI;
    }

	@Override
	protected routeResponse doInBackground(String... input) {
		final routeResponse response = new routeResponse();
		//convert string into input stream for xml parsing
		ByteArrayInputStream bais = new ByteArrayInputStream( input[0].getBytes());
		
		IRule originStationRule = new DefaultRule(Type.CHARACTER, "/root/origin") {
			@Override
			public void handleParsedCharacters(XMLParser parser, String text, Object userObject) {
				response.originStation = text;
			}
		};
		IRule destinationStationRule = new DefaultRule(Type.CHARACTER, "/root/destination") {
			@Override
			public void handleParsedCharacters(XMLParser parser, String text, Object userObject) {
				response.destinationStation = text;
			}
		};
		IRule timeRule = new DefaultRule(Type.CHARACTER, "/root/schedule/time") {
			@Override
			public void handleParsedCharacters(XMLParser parser, String text, Object userObject) {
				responseTime = text;
			}
		};
		IRule dateRule = new DefaultRule(Type.CHARACTER, "/root/schedule/date") {
			@Override
			public void handleParsedCharacters(XMLParser parser, String text, Object userObject) {
				responseDate = text;
			}
		};
		IRule specialScheduleRule = new DefaultRule(Type.CHARACTER, "/root/message/special_schedule") {
			@Override
			public void handleParsedCharacters(XMLParser parser, String text, Object userObject) {
				//fix for BART returning relative, not absolute, links
				response.specialSchedule = text.replace("href=\"/", "href=\"http://bart.gov/");
			}
		};
		// origin="DBRK" destination="WCRK" fare="3.15" origTimeMin="3:18 PM" origTimeDate="03/16/2012 " destTimeMin="3:44 PM" destTimeDate="03/16/2012"
		IRule tripAttributeRule = new DefaultRule(Type.ATTRIBUTE, "/root/schedule/request/trip", new String[]{"fare", "origTimeMin","origTimeDate","destTimeMin","destTimeDate"}) {
			@Override
			public void handleParsedAttribute(XMLParser parser, int num, String value, Object userObject) {
				
				//("ATTRIBUTE",String.valueOf(num)+ " - "+value);
				if(num == 0){ // fare
					route thisRoute = response.addRoute();
					thisRoute.fare = value;
					//leg thisLeg = thisRoute.addLeg();
					//thisLeg.disembarkStation = TheActivity.STATION_MAP.get(value);
				}
				else if(num == 1){ // origTimeMin
					routeOriginTime = value;
				}
				else if(num == 2){
					routeOriginDate = value;
				}
				else if(num == 3){
					routeDestinationTime = value;
					//("ROUTE_DEST_TIME",value.toString());
				}
				else if(num == 4){
					//Log.v("ROUTE_DEST_DATE",value.toString());
					routeDestinationDate = value;
				}
				//thisRoute.fare
			}
		};
		IRule tripTagRule = new DefaultRule(Type.TAG, "/root/schedule/request/trip") {
			@Override
			public void handleTag(XMLParser parser, boolean isStartTag, Object userObject) {
				if (!isStartTag){
					route thisRoute = response.getLastRoute();
					
					// Determine if bikes are allowed on all legs of this route
					boolean bikes = true;
					for(int x=0;x<thisRoute.legs.size();x++){
						if(!((leg)thisRoute.legs.get(x)).bikes)
							bikes = false;
					}
					thisRoute.bikes = bikes;
					
					String originDateStr = routeOriginDate + " " + routeOriginTime;
					String destinationDateStr = routeDestinationDate + " " + routeDestinationTime;
					// Bart Route responses don't indicate timezone (?), though etd responses do
					// Set SimpleDateFormat TimeZone to America/Los-Angeles
					// This ensures the application can correctly combine etd responses (with timezone) and route (without)
					// Even if the application is run on a device who's locale differs from BARTs (PST)
					curFormater = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US); 
					curFormater.setTimeZone(tz);
					try {
						Log.d("BartRouteParserEndTrip","originDate: " + originDateStr + " destDate: " + destinationDateStr);
						thisRoute.departureDate = curFormater.parse(originDateStr);//append BART response timezone
						thisRoute.arrivalDate = curFormater.parse(destinationDateStr);//append BART response timezone
					} catch (ParseException e) {
						Log.d("BartRouteParserEndTrip","non-PDT coerced parse failed");
						// TODO Auto-generated catch block
						// 5.15.2012: I've received multiple crashes here. Something's going on related to irregular time format...
						// IF coercing PDT fails, try without at the risk of displaying incorrect time
						/*try{
							Crittercism.leaveBreadcrumb(Log.getStackTraceString(e));
							thisRoute.departureDate = curFormater.parse(originDateStr);
							thisRoute.arrivalDate = curFormater.parse(destinationDateStr);
						}
						catch(ParseException e2){*/
							Log.d("BartRouteParserEndTrip","non-PST coerced parse failed");
							Log.d("BartRouteParserEndTrip_DateString", "origin: " + originDateStr+" , destination: "+destinationDateStr);
							Log.d("BartRouteParserEndTripException",e.getClass().toString() + ": " + e.getMessage());
							Crittercism.leaveBreadcrumb(originDateStr+" , "+destinationDateStr);
							Crittercism.leaveBreadcrumb(Log.getStackTraceString(e));
							Crittercism.logHandledException(e);
							dateError = true;
						//}
					}
					if(!dateError)
						Log.d("RouteParserDate","depart: " + thisRoute.departureDate.toString() + " arrive: " + thisRoute.arrivalDate.toString());
					else{
						// if an improper date is given, remove this route from response
						response.removeLastRoute();
					}
					// reset these variables for use by the next route
					originDateStr = "";
					destinationDateStr = "";
				}

			}
		};
		//
		IRule legAttributeRule = new DefaultRule(Type.ATTRIBUTE, "/root/schedule/request/trip/leg", new String[]{"transfercode", "origin","destination","origTimeMin","origTimeDate","destTimeMin","destTimeDate","trainHeadStation", "bikeflag"}) {
			@Override
			public void handleParsedAttribute(XMLParser parser, int num, String value, Object userObject) {
				// TODO: Fix assumed order of XML attributes
				route thisRoute;
				leg thisLeg;
				String dateStr;
				switch(num){
				case 0:// transfercode
					thisRoute = response.getLastRoute();
					thisLeg = thisRoute.addLeg();
					thisLeg.transferCode = value;
					break;
					//leg thisLeg = thisRoute.addLeg();
					//thisLeg.disembarkStation = TheActivity.STATION_MAP.get(value);
				
				case 1: // orig station
					thisRoute = response.getLastRoute();
					thisLeg = thisRoute.getLastLeg();
					thisLeg.boardStation = value;
					break;
					
				case 2: // dest station
					thisRoute = response.getLastRoute();
					thisLeg = thisRoute.getLastLeg();
					thisLeg.disembarkStation = value;
					break;
				
				case 3: // board time
					legTime = value;
					break;
				
				case 4: // board date
					legDate = value;
					dateStr = legDate + " " + legTime;
					curFormater = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US); 
					curFormater.setTimeZone(tz);
					thisRoute = response.getLastRoute();
					thisLeg = thisRoute.getLastLeg();
					try {
						thisLeg.boardTime = curFormater.parse(dateStr); 
					} catch (ParseException e) {
						Log.d("BartRouteParser","boardTime unparseable: "+ dateStr);
						e.printStackTrace();
					}
					break;
				
				case 5: // dest time
					legTime = value;
					break;
				
				case 6: // dest date
					legDate = value;
					//assume date always follows time:
					dateStr = legDate + " " + legTime;
					curFormater = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US); 
					curFormater.setTimeZone(tz);
					thisRoute = response.getLastRoute();
					thisLeg = thisRoute.getLastLeg();
					try {
						thisLeg.disembarkTime = curFormater.parse(dateStr);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						Log.d("BartRouteParser","disembarkTime unparseable: "+ dateStr);
						e.printStackTrace();
					}
					break;
				
				case 7: // train head station
					thisRoute = response.getLastRoute();
					thisLeg = thisRoute.getLastLeg();
					thisLeg.trainHeadStation = value.toLowerCase();
					break;
				
				case 8:  // bikeFlag
					thisRoute = response.getLastRoute();
					thisLeg = thisRoute.getLastLeg();
					if(value.equals("1"))
						thisLeg.bikes = true;
					else
						thisLeg.bikes = false;
					break;
				}
				
			}
		};
		
		XMLParser parser = new XMLParser(originStationRule, destinationStationRule, timeRule, dateRule, tripAttributeRule, tripTagRule, legAttributeRule, specialScheduleRule);
		try{
			parser.parse(bais);
		}
		catch(XMLParserException e){
			// Send a message to TheActivity to display an error dialog
			// Then cancel this AsyncTask
			sendError("Open BART received a malformed response. Please try again.");
			this.cancel(true);
		}
		//11:15:32 AM PDT
		
		//String[] timesplit = time.split(" ");
		String dateStr = responseDate + " " + responseTime + " PST";
		//Log.v("time split", timesplit.toString());
		//Log.v("Time",dateStr);
		curFormater = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US); 
		curFormater.setTimeZone(tz);
		Date dateObj = new Date();
		try {
			dateObj = curFormater.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			//Log.v("XMLParse", "date formatting error");
			e.printStackTrace();
		} 
		response.date = dateObj;
		
		return response;
	}
	
	@Override
    protected void onPostExecute(routeResponse result) {
		//((TheActivity) caller).handleResponse(result, updateUI);
		sendMessage(result);
        super.onPostExecute(result);
    }
	
	private void sendMessage(routeResponse result) { // 0 = service stopped , 1 = service started, 2 = refresh view with call to bartApiRequest(), 3 = 
		  int status = 4; // hardcode status for calling TheActivity.handleResponse
		  Log.d("sender", "Sending AsyncTask message");
	  	  Intent intent = new Intent("service_status_change");
	  	  // You can also include some extra data.
	  	  intent.putExtra("status", status);
	  	  intent.putExtra("result", (Serializable) result);
	  	  //intent.putExtra("result",(CharSequence)result);
	  	  intent.putExtra("updateUI", updateUI);
	  	  LocalBroadcastManager.getInstance(TheActivity.c).sendBroadcast(intent);
	}
	
	// Send a LocalBroadCast message to TheActivity indicating an error
	private void sendError(String message){
		int status = 13;
		Intent intent = new Intent("service_status_change");
		intent.putExtra("status", status);
		intent.putExtra("message", message);
		LocalBroadcastManager.getInstance(TheActivity.c).sendBroadcast(intent);
	}

}
