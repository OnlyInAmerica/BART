package pro.dbro.bart.api.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Response from BART Real-Time
 * Estimated Time of Departure API Call
 * <p>
 * e.g: http://api.bart.gov/api/etd.aspx?cmd=etd&orig=12th&key=MW9S-E7SL-26DU-VV8V
 */
@Root(strict = false, name = "root")
public class BartEtdResponse {

    @Element(name = "date")
    private String requestDate;

    @Element(name = "time")
    private String requestTime;

    @Element(name = "station")
    private BartStation station;

    public String toString() {
        return "date " + requestDate + " time: " + requestTime + " Station: " + station;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public BartStation getStation() {
        return station;
    }

    public List<BartEtd> getEtds() {
        return station.getEtds();
    }
}
