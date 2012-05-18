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
	
	public route removeLastRoute(){
		route toRemove = (route) routes.get(routes.size()-1);
		routes.remove(routes.size()-1);
		return toRemove;
	}
	
	public String toString(){
		return originStation + " to " + destinationStation + " on " + date.toString() + " routes: " + routes.toString();
	}

}
