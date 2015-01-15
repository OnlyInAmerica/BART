package pro.dbro.bart.api.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
 * BART Load Response
 * <p>
 * e.g: http://api.bart.gov/api/sched.aspx?cmd=load&ld1=DBRK0454&key=MW9S-E7SL-26DU-VV8V
 */
@Root(strict = false, name = "root")
public class BartLoadResponse {

    @Path("load/request/leg")
    @Attribute(name = "load")
    private int load;

    @Path("load/request/leg")
    @Attribute(name = "station")
    private String stationAbbreviation;

    @Path("load/request/leg")
    @Attribute(name = "trainId")
    private int trainId;

    private String loadString;

    public String getLoadString() {
        return loadString;
    }

    public void setLoadString(String load) {
        loadString = load;
    }

    public String toString() {
        return "station: " + stationAbbreviation + " load: " + load;
    }

    public String getStationAbbreviation() {
        return stationAbbreviation;
    }

    public int getLoad() {
        return load;
    }

    public int getTrainId() {
        return trainId;
    }

}
