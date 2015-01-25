package pro.dbro.bart.api.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import java.util.List;

import rx.Observable;

/**
 * BART Load Response
 * <p>
 * e.g: http://api.bart.gov/api/sched.aspx?cmd=load&ld1=DBRK0454&key=MW9S-E7SL-26DU-VV8V
 */
@Root(strict = false, name = "root")
public class BartLoadResponse {

    @Path("load")
    @ElementList(name = "request", entry="leg")
    private List<BartLoad> loads;

    public String toString() {
        return "load";
    }

    public List<BartLoad> getLoads() {
        return loads;
    }

    public void attachTimeToLoads(List<BartTrain> trains) {
        for (BartTrain train : trains) {
            for (BartLoad load : loads) {
                if (load.getTrainId() == train.getIndex()) {
                    load.setTime(train.getStop(load.getStationAbbreviation()).getOrigTime());
                    break;
                }
            }
        }
    }

}
