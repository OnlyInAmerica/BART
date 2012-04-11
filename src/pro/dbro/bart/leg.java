package pro.dbro.bart;

import java.util.Date;

public class leg {
	// Below data is populated based on the BART schedule
	public Date boardTime;
	public String boardStation;
	public Date disembarkTime;
	public String disembarkStation;
	public String trainHeadStation;
	public String transferCode;
	public boolean bikes;
	
	// Below data is populated based on real-time BART data
	public etd originEtd;
	public etd destinationEtd;
}
