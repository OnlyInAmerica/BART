package pro.dbro.bart.api.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by davidbrodsky on 2/11/14.
 */
@Root(strict = false, name = "etd")
public class BartEtd {

    @Element(name = "destination")
    private String destination;

    @Element(name = "abbreviation")
    private String abbreviation;

    @ElementList(entry = "estimate", inline = true)
    private List<BartEstimate> estimates;

    public String getDestinationAbbreviation() {
        return abbreviation;
    }

    public String getDestination() {
        return destination;
    }

    public List<BartEstimate> getEstimates() {
        return estimates;
    }

    public String toString() {
        return "Etd " + destination;
    }

    public boolean sameStationAs(BartEtd other) {
        return abbreviation.equals(other.abbreviation);
    }

}
