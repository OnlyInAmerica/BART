package pro.dbro.bart;

import java.util.ArrayList;
import java.util.Date;

public class routeResponse {
	public String originStation;
	public String destinationStation;
	public Date date;
	public ArrayList<route> routes;
	
	public routeResponse(){
		routes = new ArrayList();
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
