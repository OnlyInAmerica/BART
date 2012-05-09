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

import com.thebuzzmedia.sjxp.XMLParser;
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
	boolean updateUI;
	
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
				response.specialSchedule = text;
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
					
					boolean bikes = true;
					for(int x=0;x<thisRoute.legs.size();x++){
						if(!((leg)thisRoute.legs.get(x)).bikes)
							bikes = false;
					}
					thisRoute.bikes = bikes;
					
					String originDateStr = routeOriginDate + " " + routeOriginTime;
					String destinationDateStr = routeDestinationDate + " " + routeDestinationTime;
					// Bart Route responses don't indicate timezone (?), though etd responses do
					// Comparing them, it appears the route response is always in PDT
					// Therefore, I'll append PDT to the date strings
					// This ensures the application can correctly combine etd responses (with timezone) and route (without)
					// Even if the application is run on a device who's locale isn't PDT
					curFormater = new SimpleDateFormat("MM/dd/yyyy hh:mm a z"); 
					try {
						thisRoute.departureDate = curFormater.parse(originDateStr+" PDT");//append BART response timezone
						thisRoute.arrivalDate = curFormater.parse(destinationDateStr+" PDT");//append BART response timezone
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.d("RouteParserDate","depart: " + thisRoute.departureDate.toString() + " arrive: " + thisRoute.arrivalDate.toString());
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
					curFormater = new SimpleDateFormat("MM/dd/yyyy hh:mm a z"); 
					thisRoute = response.getLastRoute();
					thisLeg = thisRoute.getLastLeg();
					try {
						thisLeg.boardTime = curFormater.parse(dateStr+ " PDT"); //append BART response timezone
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				
				case 5: // board time
					legTime = value;
					break;
				
				case 6: // board date
					legDate = value;
					//assume date always follows time:
					dateStr = legDate + " " + legTime;
					curFormater = new SimpleDateFormat("MM/dd/yyyy hh:mm a z"); 
					thisRoute = response.getLastRoute();
					thisLeg = thisRoute.getLastLeg();
					try {
						thisLeg.disembarkTime = curFormater.parse(dateStr + " PDT"); // append BART response timezone
					} catch (ParseException e) {
						// TODO Auto-generated catch block
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
		parser.parse(bais);
		//11:15:32 AM PDT
		
		//String[] timesplit = time.split(" ");
		String dateStr = responseDate + " " + responseTime;
		//Log.v("time split", timesplit.toString());
		//Log.v("Time",dateStr);
		curFormater = new SimpleDateFormat("MMM dd, yyyy hh:mm a z"); 
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

}
