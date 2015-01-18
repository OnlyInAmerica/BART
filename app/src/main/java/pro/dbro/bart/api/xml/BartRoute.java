package pro.dbro.bart.api.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Response from BART Routes
 * Api Response
 * <p/>
 * e.g: http://api.bart.gov/api/route.aspx?cmd=routes&key=MW9S-E7SL-26DU-VV8V
 */
@Root(strict = false, name = "root")
public class BartRoute extends BartApiResponse {

    @Element(name = "name")
    private String name;

    @Element(name = "abbr")
    private String abbreviation;

    @Element(name = "color")
    private String hexColor;

    @Element(name = "routeID")
    private String routeId;

    public String toString() {
        return name;
    }

    public String getOriginAbbreviation() {
        return abbreviation.split("-")[0];
    }

    public String getDestinationAbbreviation() {
        return abbreviation.split("-")[1];
    }

    public String getHexColor() {
        return hexColor;
    }

    public String getRouteId() {
        return routeId;
    }
}
