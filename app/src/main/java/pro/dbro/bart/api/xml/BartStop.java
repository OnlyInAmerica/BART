package pro.dbro.bart.api.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by davidbrodsky on 1/23/15.
 */
@Root(strict = false, name = "stop")
public class BartStop {

    @Attribute(name = "station")
    private String station;

    @Attribute(name = "origTime")
    private String origTime;

    public String getStation() {
        return station;
    }

    public String getOrigTime() {
        return origTime;
    }
}
