package pro.dbro.bart.api.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
 * BART Load Response
 *
 * Represents a load at a given station and route's train.
 * e.g: http://api.bart.gov/api/sched.aspx?cmd=load&ld1=DBRK0454&key=MW9S-E7SL-26DU-VV8V
 */
@Root(strict = false, name = "leg")
public class BartLoad {

    @Attribute(name = "load")
    private int load;

    @Attribute(name = "station")
    private String stationAbbreviation;

    @Attribute(name = "trainId")
    private int trainId;

    @Attribute(name = "route")
    private int routeId;

    private String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String toString() {
        return "station: " + stationAbbreviation + " load: " + load;
    }

    public int getLoad() {
        return load;
    }

    public String getStationAbbreviation() {
        return stationAbbreviation;
    }

    public int getTrainId() {
        return trainId;
    }

    public int getRouteId() {
        return routeId;
    }

    public static String getLoadDescription(int load) {
        switch(load) {

            case 1:
                return "Light";
            case 2:
                return "Medium";
            case 3:
                return "Heavy";
            default:
                return "Unknown";

        }
    }

}
