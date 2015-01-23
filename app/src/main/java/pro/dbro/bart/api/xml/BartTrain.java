package pro.dbro.bart.api.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Created by davidbrodsky on 1/22/15.
 */
@Root(strict = false, name = "train")
public class BartTrain {

    @Attribute(name = "index")
    private int index;

    public int getIndex() {
        return index;
    }

//    @ElementList(name = "stop")
//    List<BartStop> stops;
}
