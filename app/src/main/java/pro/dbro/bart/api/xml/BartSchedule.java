package pro.dbro.bart.api.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by davidbrodsky on 2/11/14.
 */
@Root(strict = false, name = "schedule")
public class BartSchedule {

    @Element(name = "date")
    private String requestDate;

    @Element(name = "time")
    private String requestTime;

    @ElementList(name = "request", entry = "trip")
    private List<BartTrip> trips;

    public String getRequestDate() {
        return requestDate;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public List<BartTrip> getTrips() {
        return trips;
    }
}
