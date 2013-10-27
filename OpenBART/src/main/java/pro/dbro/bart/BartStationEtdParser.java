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

import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.XMLParserException;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;
import com.thebuzzmedia.sjxp.rule.IRule.Type;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import pro.dbro.bart.model.Etd;
import pro.dbro.bart.model.EtdResponse;


public class BartStationEtdParser extends AsyncTask<String, String, EtdResponse> {
	//date and time are two different xpaths in bart response
	//we combine them into one Java Date
	private String date = new String();
	private String time = new String();
	TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
	boolean updateUI;
	
	BartStationEtdParser(boolean updateUI) {
        this.updateUI = updateUI;
    }

	@Override
	protected EtdResponse doInBackground(String... input) {
		final EtdResponse response = new EtdResponse();
		//convert string into input stream for xml parsing
		ByteArrayInputStream bais = new ByteArrayInputStream( input[0].getBytes());
		
		IRule stationRule = new DefaultRule(Type.CHARACTER, "/root/station/name") {
			@Override
			public void handleParsedCharacters(XMLParser parser, String text, Object userObject) {
				response.station = text;
			}
		};
		IRule timeRule = new DefaultRule(Type.CHARACTER, "/root/time") {
			@Override
			public void handleParsedCharacters(XMLParser parser, String text, Object userObject) {
				time = text;
			}
		};
		IRule dateRule = new DefaultRule(Type.CHARACTER, "/root/date") {
			@Override
			public void handleParsedCharacters(XMLParser parser, String text, Object userObject) {
				date = text;
			}
		};
	
		IRule etdTagRule = new DefaultRule(Type.TAG, "/root/station/etd") {
			@Override
			public void handleTag(XMLParser parser, boolean isStartTag, Object userObject) {
				//not yet used
				if (isStartTag){
					//Log.v("XMLParse", "etd start");
					//response.addEtd();
				}
				else{
					//Log.v("XMLParse", "etd end");
					//response.etds.add(currentEtd);
				}
			}
		};
		IRule estTagRule = new DefaultRule(Type.TAG, "/root/station/etd/estimate") {
			@Override
			public void handleTag(XMLParser parser, boolean isStartTag, Object userObject) {
				if (isStartTag){
					//Log.v("XMLParse", "etd start");
					Etd newGuy = response.addEtd();
					newGuy.destination = response.tmpDestination;
				}
				else{
					//Log.v("XMLParse", "etd end");
					//response.etds.add(currentEtd);
				}
			}
		};
		
		IRule destinationRule = new DefaultRule(Type.CHARACTER, "/root/station/etd/destination") {
			@Override
			public void handleParsedCharacters(XMLParser parser, String text, Object userObject) {
				//currentEtd.destination = text;
				//response.lastEtd().destination = text;
				response.tmpDestination = text;
			}
		};
		IRule minuteRule = new DefaultRule(Type.CHARACTER, "/root/station/etd/estimate/minutes") {
			@Override
			public void handleParsedCharacters(XMLParser parser, String text, Object userObject) {
				//currentEtd.minutesToArrival = Integer.parseInt(text);
				try{
					response.lastEtd().minutesToArrival = Integer.parseInt(text);
				}
				catch(Exception e){
					response.lastEtd().minutesToArrival = 0; //BART emits "Leaving..." here :/
				}
			}
		};
		IRule platformRule = new DefaultRule(Type.CHARACTER, "/root/station/etd/estimate/platform") {
			@Override
			public void handleParsedCharacters(XMLParser parser, String text, Object userObject) {
				//currentEtd.platform = Integer.parseInt(text);
				response.lastEtd().platform = Integer.parseInt(text);
			}
		};
		IRule bikeRule = new DefaultRule(Type.CHARACTER, "/root/station/etd/estimate/bikeflag") {
			@Override
			public void handleParsedCharacters(XMLParser parser, String text, Object userObject) {
				//currentEtd.bikes = Boolean.getBoolean(text);
				if(Integer.parseInt(text) == 1)
					response.lastEtd().bikes = true;
				else
					response.lastEtd().bikes = false;
			}
		};
		IRule warningRule = new DefaultRule(Type.CHARACTER, "/root/message/warning") {
			@Override
			public void handleParsedCharacters(XMLParser parser, String text, Object userObject) {
				//currentEtd.bikes = Boolean.getBoolean(text);
				response.message = text;
			}
		};
		XMLParser parser = new XMLParser(stationRule, timeRule, dateRule, estTagRule, destinationRule, minuteRule, platformRule, bikeRule, warningRule);
		try{
			parser.parse(bais); // CRASH: XMLPullParserException
		}
		catch(XMLParserException e){
			// Send a message to TheActivity to display an error dialog
			// Then cancel this AsyncTask
			sendError("Open BART received a malformed response. Please try again.");
			this.cancel(true);
		}
		//11:15:32 AM PDT
		
		
		//String[] timesplit = time.split(" ");
		String dateStr = date + " " + time;
		//Log.v("time split", timesplit.toString());
		Log.d("BartStationEtdParser","dateStr: "+ dateStr);
		/* Example etdResponse data/time data:
		 * <date>06/11/2012</date>
		 * <time>01:11:16 PM PDT</time>
		 */
		SimpleDateFormat curFormater = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a z", Locale.US); 
		//curFormater.setTimeZone(tz);
		
		Date dateObj = new Date();
		Log.d("BartStationEtdParser","expected dateStr format: " + curFormater.format(dateObj) + "DST: "+ String.valueOf(tz.inDaylightTime(dateObj)));
		try {
			dateObj = curFormater.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			// If this fails, dateObj will have value of current device datetime
		} 
		response.date = dateObj;
		Log.d("EtdParserDate",response.date.toString());
		
		return response;
	}
	
	@Override
    protected void onPostExecute(EtdResponse result) {
		//((TheActivity) caller).handleResponse(result, updateUI);
		sendMessage(result);
        super.onPostExecute(result);
    }
	
	private void sendMessage(EtdResponse result) { // 0 = service stopped , 1 = service started, 2 = refresh view with call to bartApiRequest(), 3 =
		  int status = 4; // hardcode status for calling TheActivity.handleResponse
		  //Log.d("sender", "Sending AsyncTask message");
	  	  Intent intent = new Intent("service_status_change");
	  	  // You can also include some extra data.
	  	  intent.putExtra("status", status);
	  	  intent.putExtra("result", (Serializable) result);
	  	  //intent.putExtra("result",(CharSequence)result);
	  	  intent.putExtra("updateUI", updateUI);
	  	  LocalBroadcastManager.getInstance(TheActivity.c).sendBroadcast(intent);
	}
	
	private void sendError(String message){
		int status = 13;
		Intent intent = new Intent("service_status_change");
		intent.putExtra("status", status);
		intent.putExtra("message", message);
		LocalBroadcastManager.getInstance(TheActivity.c).sendBroadcast(intent);
	}

}
