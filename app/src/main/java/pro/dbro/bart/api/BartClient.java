package pro.dbro.bart.api;

import android.util.Log;

import com.mobprofs.retrofit.converters.SimpleXmlConverter;

import org.apache.commons.collections4.BidiMap;

import java.util.List;
import java.util.Set;

import pro.dbro.bart.api.xml.BartEtdResponse;
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

//        service.getStations()
//               .subscribeOn(Schedulers.io())
//               .observeOn(AndroidSchedulers.mainThread())
//               .subscribe(stations -> this.stations = stations);

        // TODO Don't return until stations are fetched. Perhaps use static
        // TODO getInstance() that returns Observable<BartClient>

    }

    public Set<String> getStationNames() {
        return stations.getStationNameToCodeMap().keySet();
    }


    public Observable<BartRouteResponse> getRouteResponse(String departureName,
                                                          String destinationName) {

        String departureCode   = stations.getStationNameToCodeMap().get(departureName.toLowerCase());
        String destinationCode = stations.getStationNameToCodeMap().get(destinationName.toLowerCase());

        if (departureCode == null || destinationCode == null) {
            String error = String.format("getEtdResponse given unknown station name: %s",
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

    public Observable<BartEtdResponse> getEtdResponse(String stationName) {

        String stationCode = stations.getStationNameToCodeMap().get(stationName);
        if (stationCode == null) {
            String error = String.format("getEtdResponse given unknown station name: %s", stationName);
            Log.e(TAG, error);
            throw new IllegalArgumentException(error);
            //return Observable.error(OnErrorThrowable.from(new IllegalArgumentException(error)));
        }

        return service.getEtdResponse(stationCode)
                      .map(BartApiResponseProcessor::processEtdResponse);
    }

}
