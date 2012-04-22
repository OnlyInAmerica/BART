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

import java.util.ArrayList;
import java.util.Date;

public class route {
	public String fare;
	public Date departureDate;
	public Date arrivalDate;
	public ArrayList legs;
	public boolean bikes = false; // if all legs allow bikes, route flagged
	public boolean isExpanded = false; // is corresponding view expanded?
	
	public route(){
		legs = new ArrayList(3);  // Any point-point on BART should be navigable with 3 legs
	}
	
	public leg addLeg(){
		leg newLeg = new leg();
		legs.add(newLeg);
		return newLeg;
	}
	
	public leg getLastLeg(){
		return (leg)legs.get(legs.size()-1);
	}
	
	@Override
	public String toString(){
		if (legs.size() > 1){
			return ((leg)legs.get(0)).trainHeadStation + " to " + ((leg)legs.get(0)).trainHeadStation;
		}
		else{
			return ((leg)legs.get(0)).trainHeadStation;
		}
		
	}

}
