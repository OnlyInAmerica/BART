package pro.dbro.bart;

import java.util.ArrayList;
import java.util.Date;

public class route {
	public double fare;
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

}
