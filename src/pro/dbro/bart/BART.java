package pro.dbro.bart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class BART {
	// *****  BART API Data  *****
	public final static String API_ROOT = "http://api.bart.gov/api/";
	// The public BART API key is: MW9S-E7SL-26DU-VV8V
	// To obtain your own key, see http://api.bart.gov/api/register.aspx
	public final static String API_KEY= SECRETS.BART_API_KEY;
	
	// *****  BART Route ETA Threshold  *****
	// After a day's service has concluded, BART will return scheduled trains for the next day 
	// Don't display these in the standard routeResponse display
	public final static long ETA_THRESHOLD_MS = 1000*60*60; // Don't display trains more than an hour out
	
	// *****  BART Station Data  *****
	// The following in-line data structure declarations are generated by a simple python file and BART station lists.
	// TODO: use BART's station feed: http://api.bart.gov/api/stn.aspx?cmd=stns&key=MW9S-E7SL-26DU-VV8V
	// to periodically check for new stations and adjust these data structures accordingly.
	
	// String array of plain-text station names
	// Use: AutoComplete behavior on origin/destination TextView inputs matches against these strings
	static final String[] STATIONS = new String[] {
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
	
	// String array of BART API station abbreviations
	
	static final String[] STATION_CODES = new String[] {
		"12th","16th","19th","24th","ashb","balb","bayf","cast","civc","cols","colm","conc","daly","dbrk","dubl","deln",
		"plza","embr","frmt","ftvl","glen","hayw","lafy","lake","mcar","mlbr","mont","nbrk","ncon","orin","pitt","phil",
		"powl","rich","rock","sbrn","sfia","sanl","shay","ssan","ucty","wcrk","wdub","woak"
	};
	
	// HashMap of BART API station abbreviations to lat/lon
	static final HashMap<String, String> STATION_LOCATION_MAP = new HashMap<String, String>(){
		{
			put("12th", "37.803664,-122.271604");put("16th", "37.765062,-122.419694");put("19th", "37.80787,-122.269029");
			put("24th", "37.752254,-122.418466");put("ashb", "37.853024,-122.26978");put("balb", "37.72198087,-122.4474142");
			put("bayf", "37.697185,-122.126871");put("cast", "37.690754,-122.075567");put("civc", "37.779528,-122.413756");
			put("cols", "37.754006,-122.197273");put("colm", "37.684638,-122.466233");put("conc", "37.973737,-122.029095");
			put("daly", "37.70612055,-122.4690807");put("dbrk", "37.869867,-122.268045");put("dubl", "37.701695,-121.900367");
			put("deln", "37.925655,-122.317269");put("plza", "37.9030588,-122.2992715");put("embr", "37.792976,-122.396742");
			put("frmt", "37.557355,-121.9764");put("ftvl", "37.774963,-122.224274");put("glen", "37.732921,-122.434092");
			put("hayw", "37.670399,-122.087967");put("lafy", "37.893394,-122.123801");put("lake", "37.797484,-122.265609");
			put("mcar", "37.828415,-122.267227");put("mlbr", "37.599787,-122.38666");put("mont", "37.789256,-122.401407");
			put("nbrk", "37.87404,-122.283451");put("ncon", "38.003275,-122.024597");put("orin", "37.87836087,-122.1837911");
			put("pitt", "38.018914,-121.945154");put("phil", "37.928403,-122.056013");put("powl", "37.784991,-122.406857");
			put("rich", "37.936887,-122.353165");put("rock", "37.844601,-122.251793");put("sbrn", "37.637753,-122.416038");
			put("sfia", "37.6159,-122.392534");put("sanl", "37.72261921,-122.1613112");put("shay", "37.63479954,-122.0575506");
			put("ssan", "37.664174,-122.444116");put("ucty", "37.591208,-122.017867");put("wcrk", "37.905628,-122.067423");
			put("wdub", "37.699759,-121.928099");put("woak", "37.80467476,-122.2945822");
		}
	};
	
	//  HashMap of plain-text station names related to BART API abbreviations.
	//  Use: Convert plain text station names in origin/destination TextView inputs to BART API station string representations
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
	
	//  HashMap of BART API station abbreviations to plain-text station names
	//  Use: Convert stations in a BART API response to plain-text for display. 
	//  This map is only used to convert BART API response stations to plain-text for display
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
	
	// Irregular etd Train Name - > bart terminal station abbreviation
	// list of all trainHeadStation values that aren't actually stations
	// i.e: Daly City/Millbrae, SFO/Milbrae
	// TODO: Make this a resource in /values 
	static final HashMap<String, String> KNOWN_SILLY_TRAINS = new HashMap<String, String>(){
		{
			put("SFIA/Millbrae", "mlbr");// SFIA is sfia
			put("Millbrae/Daly City", "mlbr"); //Daly City is daly
		}
	};
	
	// Return BART API abbreviation of nearest station given lat,lon
	static public String findNearestStation(double lat1, double lon1){
		double min_distance = 99; // miles
		String nearestStation = "";
		for(int x = 0; x < STATION_CODES.length;x++){
			double lon2 = Double.valueOf(STATION_LOCATION_MAP.get(STATION_CODES[x]).split(",")[1]);
			double lat2 = Double.valueOf(STATION_LOCATION_MAP.get(STATION_CODES[x]).split(",")[0]);
			double theta = lon1 - lon2;
			double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
			dist = Math.acos(dist);
			dist = rad2deg(dist);
			dist = dist * 60 * 1.1515;
			if (dist < min_distance){
				min_distance = dist;
				nearestStation = STATION_CODES[x];
			}
		}
		
		return nearestStation;
	}
	
	static private double deg2rad(double deg) {
		  return (deg * Math.PI / 180.0);
	}

	
	static private double rad2deg(double rad) {
		  return (rad * 180 / Math.PI);
	}


}
