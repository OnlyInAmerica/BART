package pro.dbro.bart.api;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.mobprofs.retrofit.converters.SimpleXmlConverter;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import pro.dbro.bart.api.xml.BartEtdResponse;
import pro.dbro.bart.api.xml.BartLeg;
import pro.dbro.bart.api.xml.BartLoad;
import pro.dbro.bart.api.xml.BartLoadResponse;
import pro.dbro.bart.api.xml.BartQuickPlannerResponse;
import pro.dbro.bart.api.xml.BartRoute;
import pro.dbro.bart.api.xml.BartRoutesResponse;
import pro.dbro.bart.api.xml.BartStationListResponse;
import pro.dbro.bart.api.xml.BartStop;
import pro.dbro.bart.api.xml.BartTrain;
import pro.dbro.bart.db.BartProvider;
import pro.dbro.bart.db.LoadColumns;
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

    public List<BartRoute> getRoutes() {
        return routes.getRoutes();
    }

    public Set<Integer> getRoutesNumbers() {
        return Observable.from(routes.getRoutes())
                         .map(route -> route.getRouteNum())
                         .reduce(new HashSet<Integer>(), (set, routeNum) -> {
                             ((Set)set).add(routeNum);
                             return set;
                         })
                         .toBlocking()
                         .single();
    }

    public Set<String> getStationNames() {
        return stations.getStationNameToCodeMap().keySet();
    }

    public Set<String> getStationCodes() {
        return stations.getStationNameToCodeMap().values();
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
     * Get projected load for a complete route during the current day of Bart Service.
     * This will fetch and cache load for every station along the route.
     *
     * @param routeNum Bart route number. Valid routes are 1-8, 11-12 and 19-20
     * @param returnQuery whether to return a Cursor pointing to the retrieved load items
     */
    public Observable<Cursor> getRouteLoad(@NonNull final Context context,
                                           final int routeNum,
                                           final boolean returnQuery) {

        Log.d(TAG, "getting Load for route " + routeNum);

        return service.getRouteSchedule(routeNum)
               .flatMap(schedule -> Observable.from(schedule.getTrains()))
               .flatMap(train -> Observable.zip(
                       Observable.from(train.getStops()),
                       Observable.just(train).repeat(train.getStops().size()),
                       (stop, _train) -> new Pair<BartTrain, BartStop>(_train, stop)))
               .filter(trainStop -> !TextUtils.isEmpty(trainStop.second.getOrigTime()))
               .buffer(3)   // Confirm all stations and trains present here
               .flatMap(trainStops -> getTrainStopLoad(trainStops, routeNum))
               .flatMap(response -> Observable.from(response.getLoads()))
               .reduce(new ArrayList<>(), (list, load) -> {

                   if (TextUtils.isEmpty(load.getTime())) {
                       Log.wtf(TAG, String.format("No Time Matched for route %d train %d station %s", load.getRouteId(),
                               load.getTrainId(), load.getStationAbbreviation()));
                   }
                   //Log.d(TAG, String.format("Got load for %s route %d train %d", load.getStationAbbreviation(), load.getRouteId(), load.getTrainId()));

                   ContentValues values = new ContentValues(5);
                   values.put(LoadColumns.station, load.getStationAbbreviation());
                   values.put(LoadColumns.route, load.getRouteId());
                   values.put(LoadColumns.train, load.getTrainId());
                   values.put(LoadColumns.load, load.getLoad());
                   values.put(LoadColumns.time, load.getTime());

                   list.add(values);
                   return list;
               })
               .map(list -> {
                   Log.d(TAG, "Got reduced load list");
                   ContentValues[] values = new ContentValues[list.size()];
                   int inserted = context.getContentResolver().bulkInsert(BartProvider.Load.LOAD, list.toArray(values));
                   //Log.d(TAG, String.format("Inserted %d loads for %s %s", inserted, stationCode, routeNum));
                   if (!returnQuery) return null; // TODO Is this kosher?
                   return context.getContentResolver().query(BartProvider.Load.LOAD,
                           null,
                           LoadColumns.route + " = ?",
                           new String[]{String.valueOf(routeNum)},
                           LoadColumns.train + " ASC");
               });

    }

    public static final HashSet<String> loadItems = new HashSet<>();

    /**
     * Get loading data for up to three Train / Stop pairs along a given route.
     *
     * @param trainStops a Pair describing the desired stop and train
     * @param route Bart route number. Valid routes are 1-8, 11-12 and 19-20
     */
    private Observable<BartLoadResponse> getTrainStopLoad(@NonNull final List<Pair<BartTrain, BartStop>> trainStops,
                                                          final int route) {
        String[] loadCodes = new String[3];
        for(int x = 0; x < Math.min(loadCodes.length, trainStops.size()); x++) {
            loadCodes[x] = String.format("%s%02d%02d",
                                         trainStops.get(x).second.getStation(),
                                         route,
                                         trainStops.get(x).first.getIndex());
            boolean success = loadItems.add(loadCodes[x]);
            if (!success) Log.wtf(TAG, String.format("Set already contained load for %s", loadCodes[x]));

        }
        Log.d(TAG, String.format("Getting load for %s %s %s", loadCodes[0], loadCodes[1], loadCodes[2]));
        return service.getLegLoad(loadCodes[0], loadCodes[1], loadCodes[2], "w")
                      .map(loadResponse -> {
                          for (BartLoad load : loadResponse.getLoads()) {
                              for (Pair<BartTrain, BartStop> trainStop : trainStops) {
                                  if (load.getStationAbbreviation().equals(trainStop.second.getStation()) &&
                                      trainStop.first.getIndex() == load.getTrainId()) {
                                        load.setTime(trainStop.second.getOrigTime());
                                  }
                              }
                          }
//                          Log.d(TAG, "Got load for " + loadStations.toString() + " alongside " + trainString.toString());
                          return loadResponse;
                      });
    }

}
