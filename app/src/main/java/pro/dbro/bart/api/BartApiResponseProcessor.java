package pro.dbro.bart.api;

import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import pro.dbro.bart.api.xml.BartEstimate;
import pro.dbro.bart.api.xml.BartEtd;
import pro.dbro.bart.api.xml.BartEtdResponse;
import pro.dbro.bart.api.xml.BartRouteResponse;
import pro.dbro.bart.api.xml.BartTrip;

/**
 * Performs higher level functions on
 * BART API Responses
 * Created by davidbrodsky on 2/27/14.
 */
public class BartApiResponseProcessor {
    private static final boolean VERBOSE = false;
    private static final String TAG = "BartApiRespProc";

    /**
     * Process a BartEtdResponse.
     * <p/>
     * Replace "Leaving" estimates with "<1"
     *
     * @param source
     */
    public static BartEtdResponse processEtdResponse(BartEtdResponse source) {
        if (source.getEtds() == null || source.getEtds().size() == 0) {
            Log.d(TAG, "BartEtdResponse has no etds");
            return source;
        }
        for (BartEtd etd : source.getEtds()) {
            for (BartEstimate estimate : etd.getEstimates()) {
                if (estimate.getDeltaMinutesEstimate().equals("Leaving")) {
                    estimate.setDeltaMinutesEstimate("<1");
                }
            }
        }

        return source;
    }

    /**
     * Process a BartRouteResponse
     * <p/>
     *
     * Remove trips that have already begun.
     * Update trip departures with cached BartEtdResponse
     * if available
     *
     * @param routeResponse
     */
    public static void processRouteResponse(BartRouteResponse routeResponse,
                                            BartEtdResponse etdResponse) {

        // Remove Trips that have already departed the origin station
        Date now = new Date();
        List<BartTrip> trips = routeResponse.getTrips();
        ArrayList<BartTrip> tripsToRemove = new ArrayList<>();

        for (int x = 0; x < trips.size(); x++) {
            try {
                if (trips.get(x).getOriginAsDate().compareTo(now) < 0) {
                    if (VERBOSE) Log.i(TAG, "Removing old trip " + trips.get(x).getOriginAsDate());
                    tripsToRemove.add(trips.get(x));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        trips.removeAll(tripsToRemove);

        if (etdResponse != null && responsesLinked(etdResponse, routeResponse)) {
            // Update Route arrivals with Etd
            List<BartEtd> etds = etdResponse.getEtds();
            Collections.sort(trips);

            for (int x = 0; x < etds.size(); x++) {
                int estimatesMatched = 0;
                for (int y = 0; y < trips.size(); y++) {
                    if (trips.get(y).getLegs().get(0).getTrainHeadStation().compareTo(etds.get(x).getDestinationAbbreviation()) == 0) {
                        if (VERBOSE) Log.i(TAG, "Found etdResponse matching route");
                        Collections.sort(etds.get(x).getEstimates());
                        if (estimatesMatched < etds.get(x).getEstimates().size()) {
                            Date etdBasedOriginDate = etds.get(x).getEstimates().get(estimatesMatched).getDateEstimate();
                            trips.get(y).getLegs().get(0).adjustWithEstimatedOriginDate(etdBasedOriginDate);
                            estimatesMatched++;
                        }
                    }
                }
            }
        }
    }

    /**
     * Return whether the EtdResponse station
     * corresponds to the origin station of the RouteResponse.
     *
     * @param etdResp
     * @param routeResp
     * @return
     */
    private static boolean responsesLinked(BartEtdResponse etdResp, BartRouteResponse routeResp) {
        return (etdResp.getStation().getAbbreviation().compareTo(
                routeResp.getOriginAbbreviation()) == 0);
    }
//
//    /**
//     * Process a BartLoadResponse
//     * <p/>
//     *
//     * Generate a human-friendly String representation of
//     * the load int
//     *
//     * @param response
//     */
//    public static void processLoadResponse(BartLoadResponse response) {
//        String loadStr = null;
//        switch(response.getLoad()) {
//            case -1:
//                // For now treat -1 (Unknown) as Low Load
//                // so far this seems to be the case
//                loadStr = "Low crowding";
//                break;
//            case 1:
//                loadStr = "Low crowding";
//                break;
//            case 2:
//                loadStr = "Medium crowding";
//                break;
//            case 3:
//                loadStr = "High crowding";
//                break;
//            default:
//                loadStr = "Unknown crowding";
//        }
//        response.setLoadString(loadStr);
//    }
}