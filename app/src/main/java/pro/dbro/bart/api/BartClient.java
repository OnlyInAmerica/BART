package pro.dbro.bart.api;

import android.support.annotation.NonNull;
import android.util.Log;

import com.mobprofs.retrofit.converters.SimpleXmlConverter;

import org.apache.commons.collections4.BidiMap;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import pro.dbro.bart.api.xml.BartEtdResponse;
import pro.dbro.bart.api.xml.BartLeg;
import pro.dbro.bart.api.xml.BartLoadResponse;
import pro.dbro.bart.api.xml.BartRouteResponse;
import pro.dbro.bart.api.xml.BartStation;
import pro.dbro.bart.api.xml.BartStationListResponse;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.OnErrorThrowable;
import rx.schedulers.Schedulers;

/**
 * Created by davidbrodsky on 1/14/15.
 */
public class BartClient {
    public final String TAG = getClass().getSimpleName();

    private BartService service;
    private BartStationListResponse stations;

    public static Observable<BartClient> getInstance() {
        BartClient client = new BartClient();

        return client.service.getStations()
                             .map(stationResponse -> {
                                 client.stations = stationResponse;
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

    public Observable<BartLoadResponse> getLoad(@NonNull BartLeg leg) {
        Formatter formatter = new Formatter(Locale.US);
        String legCode = leg.getOriginAbbreviation() + formatter.format("%02d",
                Integer.parseInt(leg.getLine().substring(leg.getLine().indexOf(" ")).trim())) + leg.getTrainIndex();

        return service.getLegLoad(legCode);
    }


    public Observable<BartRouteResponse> getRoute(@NonNull String departureName,
                                                  @NonNull String destinationName) {

        String departureCode   = stations.getStationNameToCodeMap().get(departureName.toLowerCase());
        String destinationCode = stations.getStationNameToCodeMap().get(destinationName.toLowerCase());

        if (departureCode == null || destinationCode == null) {
            String error = String.format("getEtd given unknown station name: %s",
                                         departureCode == null ? departureName : destinationName);
            Log.e(TAG, error);
            throw new IllegalArgumentException(error);
            //return Observable.error(new IllegalArgumentException(error));
        }

        return Observable.zip(service.getEtdResponse(departureCode)
                                     .map(BartApiResponseProcessor::processEtdResponse),

                              service.getRouteResponse(departureCode, destinationCode),

                              (etdResponse, routeResponse) -> {
                                  BartApiResponseProcessor.processRouteResponse(routeResponse, etdResponse);
                                  return routeResponse;
                              }
        );
    }

    public Observable<BartEtdResponse> getEtd(@NonNull String stationName) {

        String stationCode = stations.getStationNameToCodeMap().get(stationName);
        if (stationCode == null) {
            String error = String.format("getEtd given unknown station name: %s", stationName);
            Log.e(TAG, error);
            throw new IllegalArgumentException(error);
            //return Observable.error(OnErrorThrowable.from(new IllegalArgumentException(error)));
        }

        return service.getEtdResponse(stationCode)
                      .map(BartApiResponseProcessor::processEtdResponse);
    }

}
