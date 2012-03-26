package pro.dbro.bart;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

// TODO: access to recent stations

public class TheActivity extends Activity {
	Context c;
	TableLayout tableLayout;
	LinearLayout tableContainerLayout;
	String lastRequest="";
	Resources res;
	AutoCompleteTextView destinationTextView;
	AutoCompleteTextView originTextView;
	ImageView reverse;
	
	private final String BART_API_ROOT = "http://api.bart.gov/api/";
	private final String BART_API_KEY="MW9S-E7SL-26DU-VV8V";
	
	//AutoComplete behavior on origin/destination inputs matches against these strings
	private static final String[] STATIONS = new String[] {
		"12th St. Oakland City Center","16th St. Mission (SF)","19th St. Oakland",
		"24th St. Mission (SF)","Ashby (Berkeley)","Balboa Park (SF)","Bay Fair (San Leandro)",
		"Castro Valley","Civic Center (SF)","Coliseum/Oakland Airport","Colma","Concord",
		"Daly City","Downtown Berkeley","Dublin/Pleasanton","El Cerrito del Norte","El Cerrito Plaza",
		"Embarcadero (SF)","Fremont","Fruitvale (Oakland)","Glen Park (SF)","Hayward","Lafayette",
		"Lake Merritt (Oakland)","MacArthur (Oakland)","Millbrae","Montgomery St. (SF)",
		"North Berkeley","North Concord/Martinez","Orinda","Pittsburg/Bay Point","Pleasant Hill",
		"Powell St. (SF)","Richmond","Rockridge (Oakland)","San Bruno","San Francisco Int'l Airport SFO",
		"San Leandro","South Hayward","South San Francisco","Union City","Walnut Creek","West Oakland"
    };
	
	//Convert plain text to BART API station string representations
	static final HashMap<String, String> STATION_MAP = new HashMap<String, String>() {
		{
			put("12th St. Oakland City Center", "12th");put("16th St. Mission (SF)", "16th");put("19th St. Oakland", "19th");
			put("24th St. Mission (SF)", "24th");put("Ashby (Berkeley)", "ashb");put("Balboa Park (SF)", "balb");put("Bay Fair (San Leandro)", "bayf");
			put("Castro Valley", "cast");put("Civic Center (SF)", "civc");put("Coliseum/Oakland Airport", "cols");put("Colma", "colm");
			put("Concord", "conc");put("Daly City", "daly");put("Downtown Berkeley", "dbrk");put("Dublin/Pleasanton", "dubl");
			put("El Cerrito del Norte", "deln");put("El Cerrito Plaza", "plza");put("Embarcadero (SF)", "embr");put("Fremont", "frmt");
			put("Fruitvale (Oakland)", "ftvl");put("Glen Park (SF)", "glen");put("Hayward", "hayw");put("Lafayette", "lafy");
			put("Lake Merritt (Oakland)", "lake");put("MacArthur (Oakland)", "mcar");put("Millbrae", "mlbr");put("Montgomery St. (SF)", "mont");
			put("North Berkeley", "nbrk");put("North Concord/Martinez", "ncon");put("Orinda", "orin");put("Pittsburg/Bay Point", "pitt");
			put("Pleasant Hill", "phil");put("Powell St. (SF)", "powl");put("Richmond", "rich");put("Rockridge (Oakland)", "rock");
			put("San Bruno", "sbrn");put("San Francisco Int'l Airport SFO", "sfia");put("San Leandro", "sanl");put("South Hayward", "shay");
			put("South San Francisco", "ssan");put("Union City", "ucty");put("Walnut Creek", "wcrk");put("West Oakland", "woak");
		}
	};
	
	//Convert BART API station string to plain text representation
	//This map is only used to populate UI, so the values (but NOT keys) are safe to meddle with.
	static final HashMap<String, String> REVERSE_STATION_MAP = new HashMap<String, String>(){
		{
			put("12th", "12th St. Oakland City Center");put("16th", "16th St. Mission (SF)");put("19th", "19th St. Oakland");
			put("24th", "24th St. Mission (SF)");put("ashb", "Ashby (Berkeley)");put("balb", "Balboa Park (SF)");put("bayf", "Bay Fair (San Leandro)");
			put("cast", "Castro Valley");put("civc", "Civic Center (SF)");put("cols", "Coliseum/Oakland Airport");put("colm", "Colma");
			put("conc", "Concord");put("daly", "Daly City");put("dbrk", "Downtown Berkeley");put("dubl", "Dublin/Pleasanton");
			put("deln", "El Cerrito del Norte");put("plza", "El Cerrito Plaza");put("embr", "Embarcadero (SF)");put("frmt", "Fremont");
			put("ftvl", "Fruitvale (Oakland)");put("glen", "Glen Park (SF)");put("hayw", "Hayward");put("lafy", "Lafayette");
			put("lake", "Lake Merritt (Oakland)");put("mcar", "MacArthur (Oakland)");put("mlbr", "Millbrae");put("mont", "Montgomery St. (SF)");
			put("nbrk", "North Berkeley");put("ncon", "North Concord/Martinez");put("orin", "Orinda");put("pitt", "Pittsburg/Bay Point");
			put("phil", "Pleasant Hill");put("powl", "Powell St. (SF)");put("rich", "Richmond");put("rock", "Rockridge (Oakland)");
			put("sbrn", "San Bruno");put("sfia", "SFO Airport");put("sanl", "San Leandro");put("shay", "South Hayward");
			put("ssan", "South San Francisco");put("ucty", "Union City");put("wcrk", "Walnut Creek");put("woak", "West Oakland");
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        tableLayout = (TableLayout) findViewById(R.id.tableLayout);
        tableContainerLayout = (LinearLayout)findViewById(R.id.tableContainerLayout);
        c = this;
        res = getResources();
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, STATIONS);
        originTextView = (AutoCompleteTextView)
                findViewById(R.id.originTv);
        
        destinationTextView = (AutoCompleteTextView) findViewById(R.id.destinationTv);
        destinationTextView.setAdapter(adapter);
        originTextView.setAdapter(adapter);
        
        reverse = (ImageView) findViewById(R.id.reverse);
        
        reverse.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Editable originTempText = originTextView.getText();
				originTextView.setText(destinationTextView.getText());
				destinationTextView.setText(originTempText);	
				
				if(STATION_MAP.get(originTextView.getText().toString()) != null){
					if(STATION_MAP.get(destinationTextView.getText().toString()) != null){
						lastRequest = "route";
						bartApiRequest();
					}
					else{
						lastRequest = "etd";
						bartApiRequest();
					}
				}
			}
        });
        	
        
        originTextView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View arg1, int position,
					long arg3) {
				AutoCompleteTextView originTextView = (AutoCompleteTextView)
		                findViewById(R.id.originTv);
				originTextView.setThreshold(200);
				hideSoftKeyboard(arg1);
				
				// If a valid destination is entered, treat origin change as a request for new route
				if(STATION_MAP.get(destinationTextView.getText().toString()) != null){
					lastRequest = "route";
					bartApiRequest();
					return;
				}
				lastRequest = "etd";
				bartApiRequest();
				
				//reveal destination actv and reverse view
				//destinationTextView.setVisibility(0);
				//reverse.setVisibility(0);
			}
        });
        
        originTextView.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				AutoCompleteTextView originTextView = (AutoCompleteTextView)
		                findViewById(R.id.originTv);
				originTextView.setThreshold(1);
				originTextView.setText("");
				return false;
			}
        	
        });
                
        destinationTextView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View arg1, int position,
					long arg3) {
				String destinationStation = STATION_MAP.get(parent.getItemAtPosition(position).toString());
				//If a valid origin station is not entered, return
				if(STATION_MAP.get(originTextView.getText().toString()) == null)
					return;
					
				// Actv not available as arg1
				AutoCompleteTextView destinationTextView = (AutoCompleteTextView)
		                findViewById(R.id.destinationTv);
				destinationTextView.setThreshold(200);
				hideSoftKeyboard(arg1);
				//lastRequest = "etd";
				//String url = "http://api.bart.gov/api/etd.aspx?cmd=etd&orig="+originStation+"&key=MW9S-E7SL-26DU-VV8V";
				// TEMP: For testing route function
				lastRequest = "route";
				bartApiRequest();
			}
        });
        
        destinationTextView.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				AutoCompleteTextView originTextView = (AutoCompleteTextView)
		                findViewById(R.id.destinationTv);
				originTextView.setThreshold(1);
				originTextView.setText("");
				return false;
			}
        });    
    }
    
    //CALLED-BY: originTextView and destinationTextView item-select listeners
    //CALLS: HTTP requester: RequestTask
    private void bartApiRequest(){
    	String url = BART_API_ROOT;
    	if (lastRequest == "etd"){
    		url += "etd.aspx?cmd=etd&orig="+STATION_MAP.get(originTextView.getText().toString());
    	}
    	else if (lastRequest == "route"){
    		url += "sched.aspx?cmd=depart&a=3&b=0&orig="+STATION_MAP.get(originTextView.getText().toString())+"&dest="+STATION_MAP.get(destinationTextView.getText().toString());
    	}
    	url += "&key="+BART_API_KEY;
    	Log.v("BART API",url);
    	new RequestTask((Activity)c).execute(url);
    }
    
    private void hideSoftKeyboard (View view) {
        InputMethodManager imm = (InputMethodManager)c.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
      }
    
    //CALLED-BY: HTTP requester: RequestTask
    //CALLS: Bart API XML response parsers
    public void parseBart(String response){
    	if (response=="error"){
			new AlertDialog.Builder(c)
	        .setTitle(res.getStringArray(R.array.networkErrorDialog)[0])
	        .setMessage(res.getStringArray(R.array.networkErrorDialog)[1])
	        .setPositiveButton("Bummer", null)
	        .show();
    	}
    	else if(lastRequest == "etd")
    		new BartStationEtdParser(this).execute(response);
    	else if(lastRequest == "route")
    		new BartRouteParser(this).execute(response);
    }
    
    //CALLED-BY: Bart API XML response parsers: BartRouteParser, BartEtdParser
    //CALLS: the appropriate method to update the UI
    public void updateUI(Object response){
    	//If special messages exist from a previous request, remove them
    	if (tableContainerLayout.getChildCount() > 1)
    		tableContainerLayout.removeViews(1, tableContainerLayout.getChildCount()-1);
    	if (response instanceof etdResponse){
    		displayEtdResponse((etdResponse) response);
    	}
    	else if (response instanceof routeResponse){
    		displayRouteResponse((routeResponse) response);
    	}
    }

    //CALLED-BY: updateUI()
    //Updates the UI with data from a routeResponse
    public void displayRouteResponse(routeResponse routeResponse){
    	tableLayout.removeAllViews();
    	Log.v("DATE",new Date().toString());
    	long now = new Date().getTime();
    
    	for (int x=0;x<routeResponse.routes.size();x++){
    		TableRow tr = (TableRow) View.inflate(c, R.layout.tablerow, null);
    		route thisRoute = routeResponse.routes.get(x);
    		
    		LinearLayout legLayout = (LinearLayout) View.inflate(c, R.layout.routelinearlayout, null);
				
    		for(int y=0;y<thisRoute.legs.size();y++){
    			TextView trainTv = (TextView) View.inflate(c, R.layout.tabletext, null);
    			trainTv.setTextSize(20);
    			trainTv.setGravity(3); // set left gravity
    			if (y>0)
    				trainTv.setText("to "+REVERSE_STATION_MAP.get(((leg)thisRoute.legs.get(y)).trainHeadStation));
    			else
    				trainTv.setText(REVERSE_STATION_MAP.get(((leg)thisRoute.legs.get(y)).trainHeadStation));
    			
    			legLayout.addView(trainTv);
    		}
    		
    		tr.addView(legLayout);
    		
    		TextView arrivalTimeTv = (TextView) View.inflate(c, R.layout.tabletext, null);
    		arrivalTimeTv.setPadding(30, 0, 0, 0);
    		arrivalTimeTv.setTextSize(36);
    		Log.v("DEPART_DATE",thisRoute.departureDate.toString());
    		arrivalTimeTv.setText(String.valueOf((thisRoute.departureDate.getTime()-now)/(1000*60)));
    		tr.addView(arrivalTimeTv);
    		tr.setTag(thisRoute);
    		tableLayout.addView(tr);
    		tr.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					int index = tableLayout.indexOfChild(arg0); // index of clicked view. Expanded view will always be +1
					route thisRoute = (route) arg0.getTag();
					if (!thisRoute.isExpanded){ // if route not expanded
						thisRoute.isExpanded = true;
						LinearLayout routeDetail = (LinearLayout) View.inflate(c, R.layout.routedetail, null);
						TextView arrivalTv = (TextView) View.inflate(c, R.layout.tabletext, null);
						SimpleDateFormat curFormater = new SimpleDateFormat("hh:mm a"); 
						arrivalTv.setTextColor(0xFFC9C7C8);
						arrivalTv.setText("arrives "+curFormater.format(thisRoute.arrivalDate));
						arrivalTv.setTextSize(20);
						routeDetail.addView(arrivalTv);
						tableLayout.addView(routeDetail, index+1);
					}
					else{
						thisRoute.isExpanded = false;
						tableLayout.removeViewAt(index+1);
					}
					
				}
    		});
    	}
    	if (routeResponse.specialSchedule != null){
    		String[] specialMessage = routeResponse.specialSchedule.split("<br>\n<br>\n");
    		LinearLayout specialSchedule = (LinearLayout)View.inflate(c, R.layout.specialschedulelayout, null);
    		TextView specialScheduleTv = (TextView) View.inflate(c, R.layout.tabletext, null);
    		//specialScheduleTv.setWidth(275);
    		//specialScheduleTv.setText("Route Alerts");
    		//specialScheduleTv.setTextSize(18);
    		//specialScheduleTv.setMovementMethod(LinkMovementMethod.getInstance());
    		//specialSchedule.addView(specialScheduleTv);
    		specialSchedule.setTag(routeResponse.specialSchedule);
    		specialSchedule.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
				    TextView specialScheduleTv = (TextView) View.inflate(c, R.layout.tabletext, null);
				    specialScheduleTv.setText(Html.fromHtml(arg0.getTag().toString()));
				    specialScheduleTv.setTextSize(16);
				    specialScheduleTv.setMovementMethod(LinkMovementMethod.getInstance());
				    new AlertDialog.Builder(c)
			        .setTitle("Route Alerts")
			        .setIcon(R.drawable.warning)
			        .setView(specialScheduleTv)
			        .setPositiveButton("Bummer", null)
			        .show();
					
				}
    			
    		});
    		//tableContainerLayout.addView(specialSchedule);
    		tableLayout.addView(specialSchedule, tableLayout.getChildCount());
    		/*
    		for(int x=0;x<specialMessage.length;x++){
    			TableRow tr = new TableRow(c);
	    		LinearLayout specialSchedule = (LinearLayout)View.inflate(c, R.layout.specialschedulelayout, null);
	    		TextView specialScheduleTv = (TextView) View.inflate(c, R.layout.tabletext, null);
	    		//specialScheduleTv.setWidth(275);
	    		specialScheduleTv.setText(Html.fromHtml(specialMessage[x]));
	    		specialScheduleTv.setTextSize(18);
	    		specialScheduleTv.setMovementMethod(LinkMovementMethod.getInstance());
	    		//Html.fromHtml("<h2>Title</h2><br><p>Description here</p>"));
	    		//tr.addView(specialScheduleDisplay);
	    		//tr.addView(specialScheduleTv);
	    		specialSchedule.addView(specialScheduleTv);
	    		tableContainerLayout.addView(specialSchedule);
	    		//tableLayout.addView(specialScheduleTv, tableLayout.getChildCount());
    		}*/
    	}
    	//tableLayout.setColumnStretchable(1, true);
    }
    
    //CALLED-BY: updateUI()
    //Updates the UI with data from a etdResponse
    public void displayEtdResponse(etdResponse etdResponse){
		tableLayout.removeAllViews();
		String lastDestination = "";
		
		if (etdResponse.message != null){
    		LinearLayout specialScheduleDisplay = (LinearLayout)View.inflate(c, R.layout.specialschedulelayout, null);
    		TextView specialScheduleTv = (TextView) View.inflate(c, R.layout.tabletext, null);
    		//specialScheduleTv.setWidth(275);
    		if(etdResponse.message.contains("No data matched your criteria."))
    			specialScheduleTv.setText("This station is closed for tonight.");
    		else
    			specialScheduleTv.setText(Html.fromHtml(etdResponse.message));
    		specialScheduleTv.setTextSize(18);
    		specialScheduleTv.setMovementMethod(LinkMovementMethod.getInstance());
    		//Html.fromHtml("<h2>Title</h2><br><p>Description here</p>"));
    		specialScheduleDisplay.addView(specialScheduleTv);
    		tableContainerLayout.addView(specialScheduleDisplay);
    	}
		
		TableRow tr = (TableRow) View.inflate(c, R.layout.tablerow, null);
		//TextView timeTv =(TextView) View.inflate(c, R.layout.tabletext, null);
		int numAlt = 0;
		for(int x=0;x<etdResponse.etds.size();x++){
			if (etdResponse.etds.get(x) == null)
				break;
			etd thisEtd = (etd)etdResponse.etds.get(x);
			if (thisEtd.destination != lastDestination){ // new train destination
				numAlt = 0;
				tr = new TableRow(c);
				TextView destinationTv = (TextView) View.inflate(c, R.layout.tabletext, null);
				//bullet.setWidth(200);
				destinationTv.setTextSize(20);
				destinationTv.setText(thisEtd.destination);
				TextView timeTv = (TextView) View.inflate(c, R.layout.tabletext, null);
				timeTv.setText(String.valueOf(thisEtd.minutesToArrival));
				timeTv.setSingleLine(false);
				timeTv.setTextSize(36);
				timeTv.setPadding(30, 0, 0, 0);
				int counterTime = thisEtd.minutesToArrival * 60*1000;
	    		new ViewCountDownTimer(timeTv, counterTime, 60*1000).start();
				//text.setWidth(120);
				tr.addView(destinationTv);
				tr.addView(timeTv);
				tableLayout.addView(tr);
			}
			else{ // append next trains arrival time to existing destination display
				//timeTv.append(String.valueOf(", "+thisEtd.minutesToArrival));
				numAlt++;
				TextView nextTimeTv =(TextView) View.inflate(c, R.layout.tabletext, null);
				//nextTimeTv.setTextSize(36-(5*numAlt));
				nextTimeTv.setTextSize(36);
				nextTimeTv.setText(String.valueOf(thisEtd.minutesToArrival));
				nextTimeTv.setPadding(30, 0, 0, 0);
				if (numAlt == 1)	//0xFFF06D2F  C9C7C8
					nextTimeTv.setTextColor(0xFFC9C7C8);
				else if (numAlt == 2)
					nextTimeTv.setTextColor(0xFFA8A7A7);
				int counterTime = thisEtd.minutesToArrival * 60*1000;
	    		new ViewCountDownTimer(nextTimeTv, counterTime, 60*1000).start();
				tr.addView(nextTimeTv);
			}
			lastDestination = thisEtd.destination;
		} // end for
		//scrolly.scrollTo(0, 0);
		//setTimers();
	} 
}