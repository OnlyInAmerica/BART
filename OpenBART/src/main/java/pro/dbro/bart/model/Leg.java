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

import java.util.Date;

public class Leg {
	// Below data is populated based on the BART schedule
	public Date boardTime;
	public String boardStation;
	public Date disembarkTime;
	public String disembarkStation;
	public String trainHeadStation;
	public String transferCode;
	public boolean bikes;
	
	// Below data is populated based on real-time BART data
	public Etd originEtd;
	public Etd destinationEtd;
}
