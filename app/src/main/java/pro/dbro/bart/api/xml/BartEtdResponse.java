package pro.dbro.bart.api.xml;

import android.util.Log;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Response from BART Real-Time
 * Estimated Time of Departure API Call
 * <p>
 * e.g: http://api.bart.gov/api/etd.aspx?cmd=etd&orig=12th&key=MW9S-E7SL-26DU-VV8V
 */
@Root(strict = false, name = "root")
public class BartEtdResponse extends BartApiResponse {

    private static final SimpleDateFormat BART_DATE_PARSER = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a z", Locale.US);

    static {
        BART_DATE_PARSER.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
    }

    private static Date getDateFromBartDateTime(String bartDate, String bartTime) throws ParseException {
        return BART_DATE_PARSER.parse(String.format("%s %s", bartDate, bartTime));
    }

    @Element(name = "date")
    private String requestDate;

    @Element(name = "time")
    private String requestTime;

    @Element(name = "station")
    private BartStation station;

    public String toString() {
        return "date " + requestDate + " time: " + requestTime + " Station: " + station;
    }

    public Date getRequestDate() throws ParseException {
        return getDateFromBartDateTime(requestDate, requestTime);
    }

    public BartStation getStation() {
        return station;
    }

    public List<BartEtd> getEtds() {
        return station.getEtds();
    }

    public BartEtd getEtdByDestination(String station) {
        for (BartEtd etd : getEtds()) {
            if (etd.getDestinationAbbreviation().equals(station)) {
                Log.d("BartEtdResonse", "Found etd for " + station);
                return etd;
            }
        }
        Log.d("BartEtdResonse", "Could not find etd for " + station);
        return null;
    }
}
