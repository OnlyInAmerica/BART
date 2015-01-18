package pro.dbro.bart.api;

import pro.dbro.bart.api.xml.BartEtdResponse;
import pro.dbro.bart.api.xml.BartLoadResponse;
import pro.dbro.bart.api.xml.BartRoutesResponse;
import pro.dbro.bart.api.xml.BartScheduleResponse;
import pro.dbro.bart.api.xml.BartStationListResponse;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by davidbrodsky on 1/13/15.
 */
public interface BartService {

    @GET("/stn.aspx?cmd=stns")
    Observable<BartStationListResponse> getStations();

    @GET("/route.aspx?cmd=routes")
    Observable<BartRoutesResponse> getRoutes();

    @GET("/stn.aspx?cmd=load")
    Observable<BartLoadResponse> getLegLoad(@Query("ld1") String legCode);

    @GET("/etd.aspx?cmd=etd")
    Observable<BartEtdResponse> getEtdResponse(@Query("orig") String originCode);

    @GET("/sched.aspx?cmd=depart")
    Observable<BartScheduleResponse> getScheduleResponse(@Query("orig") String originCode,
                                                         @Query("dest") String destCode);
}
