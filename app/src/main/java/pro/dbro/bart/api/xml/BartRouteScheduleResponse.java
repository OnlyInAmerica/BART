package pro.dbro.bart.api.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Response from BART Route Schedule. e.g: A list of all trains for a given Route
 * Api Response
 * <p/>
 * e.g: http://api.bart.gov/api/sched.aspx?cmd=routesched&route=2&l=1&key=MW9S-E7SL-26DU-VV8V
 */
@Root(strict = false, name = "root")
public class BartRouteScheduleResponse extends BartApiResponse {

    @Element(name = "sched_num")
    private int scheduleNum;

    @Element(name = "date")
    private String date;

    @ElementList(name = "route", entry = "train")
    private List<BartTrain> trains;

    public int getScheduleNum() {
        return scheduleNum;
    }

    public String getDate() {
        return date;
    }

    public List<BartTrain> getTrains() {
        return trains;
    }
}
