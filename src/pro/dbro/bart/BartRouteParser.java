package pro.dbro.bart;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;
import com.thebuzzmedia.sjxp.rule.IRule.Type;

import android.app.Activity;
import android.os.AsyncTask;
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
	
	Activity caller;
	BartRouteParser(Activity caller) {
        this.caller = caller;
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
		// origin="DBRK" destination="WCRK" fare="3.15" origTimeMin="3:18 PM" origTimeDate="03/16/2012 " destTimeMin="3:44 PM" destTimeDate="03/16/2012"
		IRule tripAttributeRule = new DefaultRule(Type.ATTRIBUTE, "/root/schedule/request/trip", new String[]{"fare", "origTimeMin","origTimeDate","destTimeMin","destTimeDate"}) {
			@Override
			public void handleParsedAttribute(XMLParser parser, int num, String value, Object userObject) {
				
				Log.v("ATTRIBUTE",String.valueOf(num)+ " - "+value);
				if(num == 0){ // fare
					route thisRoute = response.addRoute();
					thisRoute.fare = Double.valueOf(value);
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
					Log.v("ROUTE_DEST_TIME",value.toString());
				}
				else if(num == 4){
					Log.v("ROUTE_DEST_DATE",value.toString());
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
					
					String originDateStr = routeOriginDate + " " + routeOriginTime;
					String destinationDateStr = routeDestinationDate + " " + routeDestinationTime;
					curFormater = new SimpleDateFormat("MM/dd/yyyy hh:mm a"); 
					try {
						thisRoute.arrivalDate = curFormater.parse(originDateStr);
						thisRoute.departureDate = curFormater.parse(destinationDateStr);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					originDateStr = "";
					destinationDateStr = "";
					
				}

			}
		};
		//
		IRule legAttributeRule = new DefaultRule(Type.ATTRIBUTE, "/root/schedule/request/trip/leg", new String[]{"transfercode", "origin","destination","origTimeMin","origTimeDate","destTimeMin","destTimeDate","trainHeadStation"}) {
			@Override
			public void handleParsedAttribute(XMLParser parser, int num, String value, Object userObject) {
				// TODO: Fix assumed order of XML attributes
				if(num == 0){ // transfercode
					route thisRoute = response.getLastRoute();
					leg thisLeg = thisRoute.addLeg();
					thisLeg.transferCode = value;
					//leg thisLeg = thisRoute.addLeg();
					//thisLeg.disembarkStation = TheActivity.STATION_MAP.get(value);
				}
				else if(num == 1){ // orig station
					route thisRoute = response.getLastRoute();
					leg thisLeg = thisRoute.getLastLeg();
					thisLeg.boardStation = value;
				}
				else if(num == 2){ // dest station
					route thisRoute = response.getLastRoute();
					leg thisLeg = thisRoute.getLastLeg();
					thisLeg.disembarkStation = value;
				}
				else if(num == 3){ // board time
					legTime = value;
				}
				else if(num == 4){ // board date
					legDate = value;
					String dateStr = legDate + " " + legTime;
					curFormater = new SimpleDateFormat("MM/dd/yyyy hh:mm a"); 
					route thisRoute = response.getLastRoute();
					leg thisLeg = thisRoute.getLastLeg();
					try {
						thisLeg.boardTime = curFormater.parse(dateStr);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(num == 5){ // board time
					legTime = value;
				}
				else if(num == 6){ // board date
					legDate = value;
					//assume date always follows time:
					String dateStr = legDate + " " + legTime;
					curFormater = new SimpleDateFormat("MM/dd/yyyy hh:mm a"); 
					route thisRoute = response.getLastRoute();
					leg thisLeg = thisRoute.getLastLeg();
					try {
						thisLeg.disembarkTime = curFormater.parse(dateStr);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(num == 7){
					route thisRoute = response.getLastRoute();
					leg thisLeg = thisRoute.getLastLeg();
					thisLeg.trainHeadStation = value;
				}
				
			}
		};
		
		XMLParser parser = new XMLParser(originStationRule, destinationStationRule, timeRule, dateRule, tripAttributeRule, tripTagRule, legAttributeRule);
		parser.parse(bais);
		//11:15:32 AM PDT
		
		//String[] timesplit = time.split(" ");
		String dateStr = responseDate + " " + responseTime;
		//Log.v("time split", timesplit.toString());
		Log.v("Time",dateStr);
		curFormater = new SimpleDateFormat("MMM dd, yyyy hh:mm a"); 
		Date dateObj = new Date();
		try {
			dateObj = curFormater.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			Log.v("XMLParse", "date formatting error");
			e.printStackTrace();
		} 
		response.date = dateObj;
		
		return response;
	}
	
	@Override
    protected void onPostExecute(routeResponse result) {
		((TheActivity) caller).updateUI(result);
        super.onPostExecute(result);
    }

}
