package pro.dbro.bart.api.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by davidbrodsky on 2/11/14.
 */
@Root(strict = false, name = "trip")
public class BartTrip extends BartDateTimeObject{

    @Attribute(name = "origin")
    private String originAbbreviation;

    @Attribute(name = "destination")
    private String destinationAbbreviation;

    @Attribute(name = "fare")
    private double fare;

    @Attribute(name = "clipper")
    private double clipper;

    @ElementList(entry = "leg", inline = true)
    private List<BartLeg> legs;

    private int maxLoad;

    public List<BartLeg> getLegs() {
        return legs;
    }

    public double getClipper() {
        return clipper;
    }

    public double getFare() {
        return fare;
    }

    public String getDestinationAbbreviation() {
        return destinationAbbreviation;
    }

    public String getOriginAbbreviation() {
        return originAbbreviation;
    }

    public int getMaxLoad() {
        return maxLoad;
    }

    public void setMaxLoad(int maxLoad) {
        this.maxLoad = maxLoad;
    }
}
