package pro.dbro.bart.api;

import android.util.Log;

import com.mobprofs.retrofit.converters.SimpleXmlConverter;

import pro.dbro.bart.api.xml.BartEtdResponse;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by davidbrodsky on 1/14/15.
 */
public class BartClient {

    private BartService service;

    public BartClient() {

        RequestInterceptor apiKeyInterceptor =
                request -> request.addEncodedQueryParam("key", "MHKJ-JSY2-EQ25-9AUK");

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://api.bart.gov/api")
                .setConverter(new SimpleXmlConverter())
                .setRequestInterceptor(apiKeyInterceptor)
                .build();

        service = restAdapter.create(BartService.class);
    }

    public void getStation(String stationCode) {
        service.getEtdResponse(stationCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((BartEtdResponse response) -> Log.i("API", response.toString()));
    }
}
