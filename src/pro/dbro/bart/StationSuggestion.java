package pro.dbro.bart;

public class StationSuggestion {
	
	public String station;
	public String type;
	
	public StationSuggestion(String station, String type){
		this.station = station;
		this.type = type;
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
