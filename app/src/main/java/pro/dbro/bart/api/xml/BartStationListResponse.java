package pro.dbro.bart.api.xml;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Response from BART Station List
 * Api Response
 * <p/>
 * e.g: http://api.bart.gov/api/stn.aspx?cmd=stns&key=MW9S-E7SL-26DU-VV8V
 */
@Root(strict = false, name = "root")
public class BartStationListResponse extends BartApiResponse {

    @ElementList(name = "stations")
    private List<BartStation> stations;

    private DualHashBidiMap<String, String> stationNameToCode;

    public String toString() {
        return String.format("%d stations", stations.size());
    }

    public DualHashBidiMap<String, String> getStationNameToCodeMap() {
        if (stationNameToCode == null) {
            stationNameToCode = new DualHashBidiMap<>();
            for (BartStation station : stations) {
                stationNameToCode.put(station.getName(), station.getAbbreviation());
            }
        }
        return stationNameToCode;
    }

}
