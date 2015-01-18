package pro.dbro.bart.api.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Response from BART QuickPlanner
 * Api Response
 * <p/>
 * e.g: http://api.bart.gov/api/sched.aspx?cmd=depart&orig=24th&dest=rock&key=MW9S-E7SL-26DU-VV8V
 */
@Root(strict = false, name = "root")
public class BartRoutesResponse extends BartApiResponse {

    @ElementList(name = "routes")
    private List<BartRoute> routes;

    public String toString() {
        return routes.size() + " routes";
    }

    public List<BartRoute> getRoutes() {
        return routes;
    }

}
