package pro.dbro.bart;

import java.util.List;

import pro.dbro.bart.api.xml.BartApiResponse;
import pro.dbro.bart.api.xml.BartLeg;

/**
 * Created by davidbrodsky on 1/18/15.
 */
public interface BartApiDelegate {
    public void refreshRequested(BartApiResponse oldResponse);
    public void loadRequested(String departureStation,
                              String routeId);
    public void usherRequested(String departureStation,
                               String trainHeadStation);
}