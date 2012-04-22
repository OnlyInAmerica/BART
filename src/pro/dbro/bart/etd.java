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

public class etd {
	
	public String destination;
	public int minutesToArrival;
	public int platform;
	public String direction;
	public boolean bikes;
	public String color;
	public boolean isExpanded = false; // Is corresponding view expanded?
	
	public String toString(){
		return destination + " in " + String.valueOf(minutesToArrival)+"m";
	}
	
	public void etd(){
		this.minutesToArrival = 0;

	}
	public void etd(String destination, int minutesToArrival, int platform, String direction, boolean bikes, String color){
		this.destination = destination;
		this.minutesToArrival = minutesToArrival;
		this.platform = platform;
		this.direction = direction;
		this.bikes = bikes;
		this.color = color;
	}

}
