package pro.dbro.bart.api.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by davidbrodsky on 2/11/14.
 */
@Root(strict = false, name = "leg")
public class BartLeg extends BartDateTimeObject {
    public static final int NO_LOAD_DATA = -100;

    @Attribute(name = "order")
    private int order;

    @Attribute(name = "transfercode")
    private String transferCode;

    @Attribute(name = "origin")
    private String originAbbreviation;

    @Attribute(name = "destination")
    private String destinationAbbreviation;

    @Attribute(name = "line")
    private String line;

    @Attribute(name = "bikeflag")
    private boolean bikesAllowed;

    @Attribute(name = "trainHeadStation")
    private String trainHeadStation;

    @Attribute(name = "trainIdx")
    private int trainIndex;

    private int load;

    public BartLeg() {
        load = NO_LOAD_DATA;
    }

    public int getLoad() {
        return load;
    }

    public void setLoad(int load) {
        this.load = load;
    }

    public int getOrder() {
        return order;
    }

    public String getTransferCode() {
        return transferCode;
    }

    public String getOriginAbbreviation() {
        return originAbbreviation;
    }

    public String getDestinationAbbreviation() {
        return destinationAbbreviation;
    }

    public String getLine() {
        return line;
    }

    public boolean areBikesAllowed() {
        return bikesAllowed;
    }

    public String getTrainHeadStation() {
        return trainHeadStation;
    }

    public int getTrainIndex() {
        return trainIndex;
    }
}
