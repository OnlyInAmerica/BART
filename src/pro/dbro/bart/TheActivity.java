package pro.dbro.bart;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;
import com.thebuzzmedia.sjxp.rule.IRule.Type;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.app.AlertDialog;


public class TheActivity extends Activity {
	Context c;
	LinearLayout etdLayout;
	etdResponse etdResponse;
	String currentStation = "dbrk";
	String lastRequest="";
	Resources res;
	
	private static final String[] STATIONS = new String[] {
		"12th St. Oakland City Center","16th St. Mission (SF)","19th St. Oakland",
		"24th St. Mission (SF)","Ashby (Berkeley)","Balboa Park (SF)","Bay Fair (San Leandro)",
		"Castro Valley","Civic Center (SF)","Coliseum/Oakland Airport","Colma","Concord",
		"Daly City","Downtown Berkeley","Dublin/Pleasanton","El Cerrito del Norte","El Cerrito Plaza",
		"Embarcadero (SF)","Fremont","Fruitvale (Oakland)","Glen Park (SF)","Hayward","Lafayette",
		"Lake Merritt (Oakland)","MacArthur (Oakland)","Millbrae","Montgomery St. (SF)",
		"North Berkeley","North Concord/Martinez","Orinda","Pittsburg/Bay Point","Pleasant Hill",
		"Powell St. (SF)","Richmond","Rockridge (Oakland)","San Bruno","San Francisco Int'l Airport",
		"San Leandro","South Hayward","South San Francisco","Union City","Walnut Creek","West Oakland"
    };
	
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
		put("San Bruno", "sbrn");put("San Francisco Int'l Airport", "sfia");put("San Leandro", "sanl");put("South Hayward", "shay");
		put("South San Francisco", "ssan");put("Union City", "ucty");put("Walnut Creek", "wcrk");put("West Oakland", "woak");
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        etdLayout = (LinearLayout) findViewById(R.id.etdLayout);
        c = this;
        res = getResources();
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, STATIONS);
        AutoCompleteTextView departureTextView = (AutoCompleteTextView)
                findViewById(R.id.tv);
        departureTextView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View arg1, int position,
					long arg3) {
				AutoCompleteTextView departureTextView = (AutoCompleteTextView)
		                findViewById(R.id.tv);
				departureTextView.setThreshold(200);
				currentStation = STATION_MAP.get(parent.getItemAtPosition(position).toString());
				hideSoftKeyboard(arg1);
				//lastRequest = "etd";
				//new RequestTask((Activity)c).execute("http://api.bart.gov/api/etd.aspx?cmd=etd&orig="+currentStation+"&key=MW9S-E7SL-26DU-VV8V");
				// TEMP: For testing route function
				lastRequest = "route";
				String url = "http://api.bart.gov/api/sched.aspx?cmd=depart&orig=dbrk&dest="+currentStation+"&key=MW9S-E7SL-26DU-VV8V";
				new RequestTask((Activity)c).execute(url);
				Log.v("BART_API",url);
			}
        });
        
        departureTextView.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				AutoCompleteTextView departureTextView = (AutoCompleteTextView)
		                findViewById(R.id.tv);
				departureTextView.setThreshold(1);
				departureTextView.setText("");
				return false;
			}
        	
        });
        
        departureTextView.setAdapter(adapter);
    }
    
    private void hideSoftKeyboard (View view) {
        InputMethodManager imm = (InputMethodManager)c.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
      }
    
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
    
    public void updateUI(Object response){
    	TextView tv = (TextView) findViewById(R.id.tv);
    	if (response instanceof etdResponse){
    		etdResponse = (etdResponse) response;
    		tv.setText(response.toString());
    		displayEtdResponse();
    	}
    	else if (response instanceof routeResponse){
    		routeResponse routeResponse = (routeResponse) response;
    	}
    }
   
    public void displayEtdResponse(){
		etdLayout.removeAllViews();
		String lastDestination = "";
		TableRow tr = new TableRow(c);
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
				etdLayout.addView(tr);
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