package pro.dbro.bart.api.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Response from BART QuickPlanner
 * Api Response
 * <p/>
 * e.g: http://api.bart.gov/api/sched.aspx?cmd=depart&orig=24th&dest=rock&key=MW9S-E7SL-26DU-VV8V
 */
@Root(strict = false, name = "root")
public class BartQuickPlannerResponse extends BartApiResponse {

    @Element(name = "origin")
    private String originAbbreviation;

    @Element(name = "destination")
    private String destinationAbbreviation;

    @Element(name = "sched_num")
    private int scheduleNum;

    @Element(name = "schedule")
    private BartSchedule schedule;

    public String getOriginAbbreviation() {
        return originAbbreviation;
    }

    public String getDestinationAbbreviation() {
        return destinationAbbreviation;
    }

    public int getScheduleNum() {
        return scheduleNum;
    }

    public BartSchedule getSchedule() {
        return schedule;
    }

    public List<BartTrip> getTrips(){
        return schedule.getTrips();
    }

    public String toString() {
        return originAbbreviation + " - " + destinationAbbreviation;
    }

}
