package pro.dbro.bart.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import pro.dbro.bart.api.xml.BartEtdResponse;
import pro.dbro.bart.api.xml.BartLoadResponse;
import pro.dbro.bart.api.xml.BartRouteScheduleResponse;
import pro.dbro.bart.api.xml.BartRoutesResponse;
import pro.dbro.bart.api.xml.BartScheduleResponse;
import pro.dbro.bart.api.xml.BartStationListResponse;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Bart API endpoints
 * Created by davidbrodsky on 1/13/15.
 */
public interface BartService {

    @GET("/stn.aspx?cmd=stns")
    Observable<BartStationListResponse> getStations();

    @GET("/route.aspx?cmd=routes")
    Observable<BartRoutesResponse> getRoutes();

    @GET("/sched.aspx?cmd=routesched")
    Observable<BartRouteScheduleResponse> getRouteSchedule(@Query("route") int route);

    @GET("/sched.aspx?cmd=load")
    Observable<BartLoadResponse> getLegLoad(@NonNull  @Query("ld1") String leg1Code,
                                            @Nullable @Query("ld2") String leg2Code,
                                            @Nullable @Query("ld3") String leg3Code,
                                            @NonNull  @Query("st") String schedule);

    @GET("/etd.aspx?cmd=etd")
    Observable<BartEtdResponse> getEtdResponse(@NonNull @Query("orig") String originCode);

    @GET("/sched.aspx?cmd=depart")
    Observable<BartScheduleResponse> getScheduleResponse(@NonNull @Query("orig") String originCode,
                                                         @NonNull @Query("dest") String destCode);
}
