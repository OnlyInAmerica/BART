package pro.dbro.bart;

import java.io.Serializable;

public class StationSuggestion implements Serializable{
	
	private int hits;
	public String station;
	public String type;
	
	public StationSuggestion(String station, String type){
		this.hits = 0;
		this.station = station;
		this.type = type;
	}
	
	public int addHit(){
		this.hits += 1;
		return this.hits;
	}
	
	public String toString(){
		return station;
	}
	
	public boolean equals(Object o){
		if(o instanceof StationSuggestion){
			if(((StationSuggestion) o).station.compareTo(this.station) == 0 && ((StationSuggestion)o).type.compareTo(this.type) == 0){
				return true;
			}
		}
		return false;
	}

}
