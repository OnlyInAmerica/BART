package pro.dbro.bart;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

// TODO: access to recent stations

public class TheActivity extends Activity {
	Context c;
	TableLayout tableLayout;
	LinearLayout tableContainerLayout;
	String lastRequest="";
	Resources res;
	AutoCompleteTextView destinationTextView;
	AutoCompleteTextView originTextView;
	TextView fareTv;
	
	// route that the usher service should access
	public static route usherRoute; 
	
	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;
	
	private UsherService mBoundService;
	private boolean mIsBound;
	
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
        
        prefs = getSharedPreferences("PREFS", 0);
        editor = prefs.edit();
        
        if(prefs.getBoolean("first_timer", true)){
        	new AlertDialog.Builder(c)
	        .setTitle("Welcome to Open BART")
	        .setIcon(R.drawable.ic_launcher)
	        .setMessage(R.string.greeting)
	        .setPositiveButton("Right on", null)
	        .show();
        	
        	editor.putBoolean("first_timer", false);
	        editor.commit();
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, STATIONS);
        originTextView = (AutoCompleteTextView)
                findViewById(R.id.originTv);
        
        fareTv = (TextView) findViewById(R.id.fareTv);
        destinationTextView = (AutoCompleteTextView) findViewById(R.id.destinationTv);
        destinationTextView.setAdapter(adapter);
        originTextView.setAdapter(adapter);
        
        if(prefs.contains("state")){
        	//state= originTextView | destinationTextView
        	String[] s = prefs.getString("state", "|").split("|");
        	originTextView.setText(s[0]);
        	destinationTextView.setText(s[1]);
        	validateInputAndDoRequest();
        }
        
        ImageView map = (ImageView) findViewById(R.id.map);
        map.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(c, MapActivity.class);
		        startActivity(intent);
			}
        	
        });
        
        ImageView reverse = (ImageView) findViewById(R.id.reverse);
        reverse.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Editable originTempText = originTextView.getText();
				originTextView.setText(destinationTextView.getText());
				destinationTextView.setText(originTempText);	
				
				validateInputAndDoRequest();
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
				/*if(STATION_MAP.get(destinationTextView.getText().toString()) != null){
					lastRequest = "route";
					bartApiRequest();
					return;
				}
				lastRequest = "etd";
				bartApiRequest();*/
				validateInputAndDoRequest();
				
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
				validateInputAndDoRequest();
				//lastRequest = "etd";
				//String url = "http://api.bart.gov/api/etd.aspx?cmd=etd&orig="+originStation+"&key=MW9S-E7SL-26DU-VV8V";
				// TEMP: For testing route function
				//lastRequest = "route";
				//bartApiRequest();
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
    // Initialize settings menu
    @Override public boolean onCreateOptionsMenu(Menu menu) {
    	if(Integer.parseInt(Build.VERSION.SDK) < 14){
	        MenuItem mi = menu.add(0,0,0,"About");
	        mi.setIcon(android.R.drawable.ic_menu_info_details);
    	}
    	else{
    		MenuInflater inflater = getMenuInflater();
    	    inflater.inflate(R.layout.actionitem, menu);
    	}
        return super.onCreateOptionsMenu(menu);
    }
    
@Override public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == 0){
			TextView aboutTv = (TextView) View.inflate(c, R.layout.tabletext, null);
			aboutTv.setText(Html.fromHtml(res.getStringArray(R.array.aboutDialog)[1]));
			aboutTv.setTextSize(18);
			aboutTv.setMovementMethod(LinkMovementMethod.getInstance());
			new AlertDialog.Builder(c)
	        .setTitle(res.getStringArray(R.array.aboutDialog)[0])
	        .setIcon(R.drawable.ic_launcher)
	        .setView(aboutTv)
	        .setPositiveButton("Right on", null)
	        .show();
			return true;
		}
		return false;
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
    	fareTv.setText("$"+routeResponse.routes.get(0).fare);
    	tableLayout.removeAllViews();
    	Log.v("DATE",new Date().toString());
    	long now = new Date().getTime();
    
    	for (int x=0;x<routeResponse.routes.size();x++){
    		
    		route thisRoute = routeResponse.routes.get(x);
        	TableRow tr = (TableRow) View.inflate(c, R.layout.tablerow, null);
        	tr.setPadding(0, 20, 0, 0);
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
    		
    		if(thisRoute.legs.size() == 1){
    			legLayout.setPadding(0, 10, 0, 0); // Address detination train and ETA not aligning 
    		}
    		
    		tr.addView(legLayout);
    		
    		TextView arrivalTimeTv = (TextView) View.inflate(c, R.layout.tabletext, null);
    		//arrivalTimeTv.setPadding(30, 0, 0, 0);
    		arrivalTimeTv.setTextSize(36);
    		Log.v("DEPART_DATE",thisRoute.departureDate.toString());
    		
    		// Don't report a train that may JUST be leaving with a negative ETA
    		long eta;
        	if(thisRoute.departureDate.getTime()-now < 0){
        		eta = 0;
        	}
        	else{
        		eta = thisRoute.departureDate.getTime()-now;
        	}
        		
    		arrivalTimeTv.setText(String.valueOf(eta/(1000*60)));
    		new ViewCountDownTimer(arrivalTimeTv, eta, 60*1000).start();
    		tr.addView(arrivalTimeTv);
    		tr.setTag(thisRoute);
    		tableLayout.addView(tr);
    		tr.setOnLongClickListener(new OnLongClickListener(){

				@Override
				public boolean onLongClick(View arg0) {
					usherRoute = (route)arg0.getTag();
					new AlertDialog.Builder(c)
	                .setTitle("Route Guidance")
	                .setMessage(getString(R.string.service_prompt))
	                .setPositiveButton(R.string.service_start_button, new DialogInterface.OnClickListener() {
	                    
	                    public void onClick(DialogInterface dialog, int which) {
	                    	Intent i = new Intent(c, UsherService.class);
	                    	//i.putExtra("departure", ((leg)usherRoute.legs.get(0)).boardStation);
	                    	Log.v("SERVICE","Starting");
	                    	startService(i);
	                    
	                    }

					 })
	                .setNeutralButton("Cancel", null)
	                .show();
					return true; // consumed the long click
				}
    			
    		});
    		tr.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					int index = tableLayout.indexOfChild(arg0); // index of clicked view. Expanded view will always be +1
					route thisRoute = (route) arg0.getTag();
					if (!thisRoute.isExpanded){ // if route not expanded
						thisRoute.isExpanded = true;
						LinearLayout routeDetail = (LinearLayout) View.inflate(c, R.layout.routedetail, null);
						TextView arrivalTv = (TextView) View.inflate(c, R.layout.tabletext, null);
						SimpleDateFormat curFormater = new SimpleDateFormat("h:mm a"); 
						//arrivalTv.setTextColor(0xFFC9C7C8);
						arrivalTv.setText("arrives "+curFormater.format(thisRoute.arrivalDate));
						arrivalTv.setTextSize(20);
						routeDetail.addView(arrivalTv);
						if(thisRoute.bikes){
							ImageView bikeIv = (ImageView) View.inflate(c, R.layout.bikeimage, null);
							routeDetail.addView(bikeIv);
						}
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
    		LinearLayout specialSchedule = (LinearLayout)View.inflate(c, R.layout.specialschedulelayout, null);
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
    		tableLayout.addView(specialSchedule, tableLayout.getChildCount());
    	}
    }
    
    //CALLED-BY: updateUI()
    //Updates the UI with data from a etdResponse
    public void displayEtdResponse(etdResponse etdResponse){
    	fareTv.setText("");
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
		
		TableRow tr = (TableRow) View.inflate(c, R.layout.tablerow_right, null);
		LinearLayout destinationRow = (LinearLayout) View.inflate(c, R.layout.destination_row, null);
		//TextView timeTv =(TextView) View.inflate(c, R.layout.tabletext, null);
		int numAlt = 0;
		for(int x=0;x<etdResponse.etds.size();x++){
			if (etdResponse.etds.get(x) == null)
				break;
			etd thisEtd = (etd)etdResponse.etds.get(x);
			if (thisEtd.destination != lastDestination){ // new train destination
				numAlt = 0;
				tr = (TableRow) View.inflate(c, R.layout.tablerow_right, null);
				tr.setPadding(0, 0, 10, 0);
				destinationRow = (LinearLayout) View.inflate(c, R.layout.destination_row, null);
				TextView destinationTv = (TextView) View.inflate(c, R.layout.destinationlayout, null);
				if(x==0)
					destinationTv.setPadding(0, 0, 0, 0);
				//bullet.setWidth(200);
				//destinationTv.setPadding(0, 0, 0, 0);
				destinationTv.setTextSize(28);
				destinationTv.setText(thisEtd.destination);
				TextView timeTv = (TextView) View.inflate(c, R.layout.tabletext, null);
				timeTv.setText(String.valueOf(thisEtd.minutesToArrival));
				timeTv.setSingleLine(false);
				timeTv.setTextSize(36);
				//timeTv.setPadding(30, 0, 0, 0);
				int counterTime = thisEtd.minutesToArrival * 60*1000;
	    		new ViewCountDownTimer(timeTv, counterTime, 60*1000).start();
				//text.setWidth(120);
	    		destinationRow.addView(destinationTv);
				//tr.addView(destinationTv);
				tr.addView(timeTv);
				tr.setTag(thisEtd);
				tableLayout.addView(destinationRow);
				tableLayout.addView(tr);
				tr.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						int index = tableLayout.indexOfChild(arg0); // index of clicked view. Expanded view will always be +1
						etd thisEtd = (etd) arg0.getTag();
						if (!thisEtd.isExpanded){ // if route not expanded
							thisEtd.isExpanded = true;
							LinearLayout routeDetail = (LinearLayout) View.inflate(c, R.layout.routedetail, null);
							TextView platformTv = (TextView) View.inflate(c, R.layout.tabletext, null);
							platformTv.setPadding(0, 0, 0, 0);
							platformTv.setText("platform "+thisEtd.platform);
							platformTv.setTextSize(20);
							routeDetail.addView(platformTv);
							if(thisEtd.bikes){
								ImageView bikeIv = (ImageView) View.inflate(c, R.layout.bikeimage, null);
								routeDetail.addView(bikeIv);
							}
							tableLayout.addView(routeDetail, index+1);
						}
						else{
							thisEtd.isExpanded = false;
							tableLayout.removeViewAt(index+1);
						}
						
					}
	    		});
			}
			else{ // append next trains arrival time to existing destination display
				//timeTv.append(String.valueOf(", "+thisEtd.minutesToArrival));
				numAlt++;
				TextView nextTimeTv =(TextView) View.inflate(c, R.layout.tabletext, null);
				//nextTimeTv.setTextSize(36-(5*numAlt));
				nextTimeTv.setTextSize(36);
				nextTimeTv.setText(String.valueOf(thisEtd.minutesToArrival));
				//nextTimeTv.setPadding(30, 0, 0, 0);
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
    
    private void validateInputAndDoRequest(){
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
    
    @Override
    public void onPause(){
    	super.onPause();
    	editor.putString("state", (originTextView.getText() + "|" + destinationTextView.getText()).toString());
    }
    
    public void onResume(){
    	super.onResume();
    	if(prefs.contains("state")){
        	//state= originTextView | destinationTextView
        	String[] s = prefs.getString("state", "|").split("|");
        	originTextView.setText(s[0]);
        	destinationTextView.setText(s[1]);
        	validateInputAndDoRequest();
        }
    }
    
}