//package pro.dbro.bart.api;
//
//import android.content.Context;
//import android.net.Uri;
//import android.util.Log;
//
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.Volley;
//import com.google.common.eventbus.EventBus;
//
//import java.util.Formatter;
//import java.util.Locale;
//
//import static com.google.common.base.Preconditions.checkArgument;
//import static com.google.common.base.Preconditions.checkNotNull;
//
///**
// * Created by davidbrodsky on 2/11/14.
// */
//public class BartApiClient {
//    public final static String API_ROOT = "http://api.bart.gov/api/";
//    private static final boolean VERBOSE = true;
//    private static final String TAG = "BartApiClient";
//    private static RequestQueue mRequestQueue;
//    private static EventBus mEventBus;                  // EventBus to post results to
//
//    private static RequestQueue getRequestQueue(Context context) {
//        if (mRequestQueue == null)
//            mRequestQueue = Volley.newRequestQueue(context);
//        return mRequestQueue;
//    }
//
//    //////////////////////
//    //    PUBLIC API
//    //////////////////////
//
//    public static void setEventBus(EventBus eventBus) {
//        mEventBus = eventBus;
//    }
//
//
//    public static void getLoadResponse(Context context, BartLeg leg) {
//        checkNotNull(leg);
//
//        SimpleXmlRequest<BartLoadResponse> loadRequest
//                = new SimpleXmlRequest<BartLoadResponse>(Request.Method.GET,
//                craftBartApiUrl(REQUEST.LOAD, leg), BartLoadResponse.class,
//                new Response.Listener<BartLoadResponse>() {
//                    @Override
//                    public void onResponse(BartLoadResponse response) {
//                        Log.i(TAG, "Success! " + response);
//                        BartApiResponseProcessor.processLoadResponse(response);
//                        if (mEventBus != null) mEventBus.post(response);
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.i(TAG, "Error " + error);
//                        if (mEventBus != null) mEventBus.post(error);
//                    }
//                }
//        );
//
//        getRequestQueue(context).add(loadRequest);
//    }
//
//
//    public static void getEtdResponse(Context context, String originAbbreviation) {
//        checkArgument(BART.REVERSE_STATION_MAP.containsKey(originAbbreviation));
//
//        SimpleXmlRequest<BartEtdResponse> etdRequest
//                = new SimpleXmlRequest<BartEtdResponse>(Request.Method.GET,
//                craftBartApiUrl(REQUEST.ETD, originAbbreviation), BartEtdResponse.class,
//                new Response.Listener<BartEtdResponse>() {
//                    @Override
//                    public void onResponse(BartEtdResponse response) {
//                        Log.i(TAG, "Success! " + response);
//                        BartApiResponseProcessor.processEtdResponse(response);
//                        if (mEventBus != null) mEventBus.post(response);
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.i(TAG, "Error " + error);
//                        if (mEventBus != null) mEventBus.post(error);
//                    }
//                }
//        );
//
//        getRequestQueue(context).add(etdRequest);
//
//    }
//
//    public static void getRouteResponse(Context context, String originAbbreviation, String destinationAbbreviation) {
//        checkArgument(BART.REVERSE_STATION_MAP.containsKey(originAbbreviation) && BART.REVERSE_STATION_MAP.containsKey(destinationAbbreviation));
//        SimpleXmlRequest<BartRouteResponse> routeRequest
//                = new SimpleXmlRequest<BartRouteResponse>(Request.Method.GET,
//                craftBartApiUrl(REQUEST.ROUTE, originAbbreviation, destinationAbbreviation), BartRouteResponse.class,
//                new Response.Listener<BartRouteResponse>() {
//                    @Override
//                    public void onResponse(BartRouteResponse response) {
//                        Log.i(TAG, "Success! " + response);
//                        BartApiResponseProcessor.processRouteResponse(response);
//                        if (mEventBus != null) mEventBus.post(response);
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.i(TAG, "Error " + error);
//                        if (mEventBus != null) mEventBus.post(error);
//                    }
//                }
//        );
//
//        getRequestQueue(context).add(routeRequest);
//
//    }
//
//    //////////////////////
//    //  END PUBLIC API
//    //////////////////////
//
//    private static String craftBartApiUrl(REQUEST type, BartLeg leg) {
//        Uri.Builder uriBuilder = Uri.parse(BART.API_ROOT)
//                .buildUpon();
//
//        Formatter formatter = new Formatter(Locale.US);
//
//        String loadParam = new StringBuilder()
//                .append(leg.getOriginAbbreviation())
//                .append(formatter.format("%02d", Integer.parseInt(leg.getLine().substring(leg.getLine().indexOf(" ")).trim())))
//                .append(leg.getTrainIndex())
//                .toString();
//
//        switch (type) {
//            case LOAD:
//                uriBuilder.appendPath("sched.aspx")
//                        .appendQueryParameter("cmd", "load")
//                        .appendQueryParameter("ld1", loadParam);
//                break;
//        }
//        uriBuilder.appendQueryParameter("key", BART.API_KEY);
//        if (VERBOSE) Log.i(TAG, "bart request url: " + uriBuilder.build().toString());
//        return uriBuilder.build().toString();
//    }
//
//    private static String craftBartApiUrl(REQUEST type, String originAbbreviation) {
//        checkArgument(type == REQUEST.ETD);
//        return craftBartApiUrl(type, originAbbreviation, null);
//    }
//
//    private static String craftBartApiUrl(REQUEST type, String originAbbreviation, String destinationAbbreviation) {
//        Uri.Builder uriBuilder = Uri.parse(BART.API_ROOT)
//                .buildUpon();
//
//        switch (type) {
//            case ETD:
//                uriBuilder.appendPath("etd.aspx")
//                        .appendQueryParameter("cmd", "etd")
//                        .appendQueryParameter("orig", originAbbreviation);
//                break;
//            case ROUTE:
//                uriBuilder.appendPath("sched.aspx")
//                        .appendQueryParameter("cmd", "depart")
//                        .appendQueryParameter("orig", originAbbreviation)
//                        .appendQueryParameter("dest", destinationAbbreviation);
//                break;
//        }
//        uriBuilder.appendQueryParameter("key", BART.API_KEY);
//        if (VERBOSE) Log.i(TAG, "bart request url: " + uriBuilder.build().toString());
//        return uriBuilder.build().toString();
//    }
//
//    public static enum REQUEST {
//        ETD,        // Station Real-Time Departure
//        ROUTE,      // QuickPlanner Route
//        LOAD        // Historical Load
//    }
//
//}
