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


package pro.dbro.bart.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

//Serializable so it can be sent via broadcast messenger
public class EtdResponse implements Serializable{
	
	public String tmpDestination;  //hacky - temp store destination for following estimates
	public String station;
	public Date date;
	public ArrayList etds;
	public String message;
	
	public String toString(){
		return station + "date: "+ date.toString() + " etds: " + etds;
	}
	
	public EtdResponse(){
		this.etds = new ArrayList();
	}
	public void etdResponse(String station, Date date, ArrayList etds){
		this.station = station;
		this.date = date;
		this.etds = etds;
	}
	
	public Etd addEtd(){
		Etd newEtd = new Etd();
		etds.add(newEtd);
		return newEtd;
	}
	public Etd lastEtd(){
		return (Etd) etds.get(etds.size()-1);
	}
	public String lastDestination(){
		return ((Etd)etds.get(etds.size()-1)).destination;
	}

}
