package pro.dbro.bart.api.xml;

import android.support.annotation.Nullable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

import rx.Observable;

/**
 * Created by davidbrodsky on 1/22/15.
 */
@Root(strict = false, name = "train")
public class BartTrain {

    @Attribute(name = "index")
    private int index;

    @ElementList(entry = "stop", inline=true)
    private List<BartStop> stops;

    public int getIndex() {
        return index;
    }

    public List<BartStop> getStops() {
        return stops;
    }

    public @Nullable BartStop getStop(String station) {
        return Observable.from(stops)
                         .filter(stop -> stop.getStation().equals(station))
                         .toBlocking()
                         .singleOrDefault(null);
    }

//    @ElementList(name = "stop")
//    List<BartStop> stops;
}
