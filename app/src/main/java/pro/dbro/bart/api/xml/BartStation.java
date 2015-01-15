package pro.dbro.bart.api.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * BART Station XML Element present in
 * BartEtdResponse
 */
@Root(strict = false, name = "station")
public class BartStation {

    @Element(name = "name")
    private String name;

    @Element(name = "abbr")
    private String abbreviation;

    @ElementList(entry = "etd", inline = true)
    private List<BartEtd> etds;

    public List<BartEtd> getEtds() {
        return etds;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name + " etds: " + etds.size();
    }


}
