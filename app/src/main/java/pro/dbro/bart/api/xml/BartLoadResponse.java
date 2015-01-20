package pro.dbro.bart.api.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import java.util.List;

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

}
