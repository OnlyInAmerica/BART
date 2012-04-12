package pro.dbro.bart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class routeResponse implements Serializable{
	public String originStation;
	public String destinationStation;
	public String specialSchedule;
	public Date date;
	public ArrayList<route> routes;
	
	public routeResponse(){
		routes = new ArrayList(3);  // Typically three routes are returned. Let's save some trivial memory!
	}
	
	public route addRoute(){
		route newRoute = new route();
		routes.add(newRoute);
		return newRoute;
	}
	
	public route getLastRoute(){
		return (route) routes.get(routes.size()-1);
	}

}
