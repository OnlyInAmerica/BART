package pro.dbro.bart.api;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseIntArray;

import com.mobprofs.retrofit.converters.SimpleXmlConverter;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import pro.dbro.bart.api.xml.BartEtdResponse;
import pro.dbro.bart.api.xml.BartLeg;
import pro.dbro.bart.api.xml.BartLoadResponse;
import pro.dbro.bart.api.xml.BartQuickPlannerResponse;
import pro.dbro.bart.api.xml.BartRoute;
import pro.dbro.bart.api.xml.BartRoutesResponse;
import pro.dbro.bart.api.xml.BartStationListResponse;
import pro.dbro.bart.api.xml.BartTrain;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.Observable;

/**
 * Created by davidbrodsky on 1/14/15.
 */
public class BartClient {
    public final String TAG = getClass().getSimpleName();

    private BartService service;
    private BartStationListResponse stations;
    private BartRoutesResponse routes;

    public static Observable<BartClient> getInstance() {
        BartClient client = new BartClient();

        return client.service.getStations()
                             .flatMap(stationResponse -> {
                                 client.stations = stationResponse;
                                 return client.service.getRoutes();
                             })
                             .map(routeResponse -> {
                                client.routes = routeResponse;
                                return client;
                             });
    }

    private BartClient() {

        RequestInterceptor apiKeyInterceptor =
                request -> request.addEncodedQueryParam("key", "MHKJ-JSY2-EQ25-9AUK");

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://api.bart.gov/api")
                .setConverter(new SimpleXmlConverter())
                .setRequestInterceptor(apiKeyInterceptor)
                .build();

        service = restAdapter.create(BartService.class);
    }

    public Set<String> getStationNames() {
        return stations.getStationNameToCodeMap().keySet();
    }

    public Observable<BartLoadResponse> getLoad(@NonNull List<BartLeg> legs) {
        Formatter formatter = new Formatter(Locale.US);
        formatter.flush();
        String[] legCodes = new String[3];
        for (BartLeg leg : legs) {
            legCodes[legs.indexOf(leg)] = leg.getOriginAbbreviation() + formatter.format("%02d%02d",
                                          Integer.parseInt(leg.getLine().substring(leg.getLine().indexOf(" ")).trim()),
                                          leg.getTrainIndex());
        }
        return service.getLegLoad(legCodes[0], legCodes[1], legCodes[2], "w"); // "w" weekday only active mode
    }


    /**
     * Get a {@link pro.dbro.bart.api.xml.BartQuickPlannerResponse} response for the given stations.
     *
     * Both parameters may be given as BART abbreviations or plain names.
     * e.g: "DBRK" or "Downtown Berkeley"
     *
     * @param departureStation departureStation station name or abbreviation
     * @param destinationStation destination station name or abbreviation
     */
    public Observable<BartQuickPlannerResponse> getRoute(@NonNull String departureStation,
                                                     @NonNull String destinationStation) {

        String departureCode   = stations.getStationNameToCodeMap().getKey(departureStation) != null ?
                                 departureStation :
                                 stations.getStationNameToCodeMap().get(departureStation);

        String destinationCode = stations.getStationNameToCodeMap().getKey(destinationStation) != null ?
                                 destinationStation :
                                 stations.getStationNameToCodeMap().get(destinationStation);

        if (departureCode == null || destinationCode == null) {
            String error = String.format("getRoute given unknown station name: %s",
                                         departureCode == null ? departureStation : destinationStation);
            Log.e(TAG, error);
            throw new IllegalArgumentException(error);
            //return Observable.error(new IllegalArgumentException(error));
        }

        return Observable.zip(service.getEtdResponse(departureCode)
                                     .map(BartApiResponseProcessor::processEtdResponse),

                              service.getScheduleResponse(departureCode, destinationCode),

                              (etdResponse, routeResponse) -> {
                                  BartApiResponseProcessor.processScheduleResponse(routeResponse,
                                          etdResponse,
                                          stations.getStationNameToCodeMap(),
                                          routes.getRoutes());
                                  return routeResponse;
                              }
        );
    }

    /**
     * Get a {@link pro.dbro.bart.api.xml.BartEtdResponse} response for the given stations.
     *
     * @param station departureStation station name or abbreviation. e.g: "DBRK" or "Downtown Berkeley"
     */
    public Observable<BartEtdResponse> getEtd(@NonNull String station) {

        String stationCode = stations.getStationNameToCodeMap().getKey(station) != null ?
                             station :
                             stations.getStationNameToCodeMap().get(station);
        if (stationCode == null) {
            String error = String.format("getEtd given unknown station name: %s", station);
            Log.e(TAG, error);
            throw new IllegalArgumentException(error);
            //return Observable.error(OnErrorThrowable.from(new IllegalArgumentException(error)));
        }

        return service.getEtdResponse(stationCode)
                      .map(BartApiResponseProcessor::processEtdResponse);
    }

    /**
     * Get projected load for a route during the current day of Bart Service.
     *
     * @param routeID the 'line' attribute in a trip leg. e.g "ROUTE 2"
     */
    public Observable<Object> getRouteLoad(String stationCode, String routeID) {

        int routeNum = Observable.from(routes.getRoutes())
                                 .filter(route -> route.getRouteId().equals(routeID))
                                 .map(BartRoute::getRouteNum)
                                 .toBlocking()
                                 .single();

        return service.getRouteSchedule(routeNum)
               .flatMap(schedule -> Observable.from(schedule.getTrains()))
               .buffer(3)
               .flatMap(trains -> {
                   String[] loadCodes = new String[3];
                   for (BartTrain train : trains) {
                       loadCodes[trains.indexOf(train)] = String.format("%s%02d%02d", stationCode, routeNum, train.getIndex());
                   }
                   return service.getLegLoad(loadCodes[0], loadCodes[1], loadCodes[2], "w");
               })
               .flatMap(response -> Observable.from(response.getLoads()))
               .reduce(new SparseIntArray(), (map, load) -> {
                   ((SparseIntArray) map).put(load.getTrainId(), load.getLoad());
                   return map;
               });

    }

}
