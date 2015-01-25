package pro.dbro.bart;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import pro.dbro.bart.api.BartClient;
import pro.dbro.bart.db.BartProvider;
import pro.dbro.bart.db.LoadColumns;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by davidbrodsky on 1/24/15.
 */
public class Bootstrap {
    public static final String TAG = "Bootstrap";

    public static void bootstrapLoadDatabase(Context context,
                                             BartClient client) throws InterruptedException {

        Set<String> stationCodes = client.getStationCodes();
        Set<Integer> routeIds = client.getRoutesNumbers();

        Log.d(TAG, "Preparing to perform " + stationCodes.size() * routeIds.size() + " load requests");
        Observable.from(stationCodes)
                  .flatMap(stationCode -> {
                      List<Pair<String, Integer>> stationRoutes = new ArrayList<Pair<String, Integer>>();
                      for (int routeId : routeIds) {
                          stationRoutes.add(new Pair<String, Integer>(stationCode, routeId));
                      }
                      return Observable.from(stationRoutes);
                  })
                .limit(1)
                .flatMap(pair -> {
                      Log.d(TAG, String.format("Requesting load for %s route %d", pair.first, pair.second));
                      return client.getRouteLoad(context, pair.first, pair.second, false);
                  })
                  .observeOn(Schedulers.io())
                  .subscribeOn(Schedulers.io())
                  .subscribe(nullCursor -> {
                      Log.d(TAG, "Did request");
                      Cursor result = context.getContentResolver().query(BartProvider.Load.LOAD,
                              null,
                              null,
                              null,
                              LoadColumns.train + " ASC");
                      if (result != null && result.moveToFirst()) {
                          Log.d(TAG, "Got load points " + result.getCount());
                          result.close();
                      } else {
                          Log.d(TAG, "cursor was null");
                      }
                  }, throwable -> {
                          throwable.printStackTrace();
                  });
//        client.getRouteLoad(context, station, String.format("ROUTE %d",routeId), false);
//        Thread.sleep(2500);
//        Cursor result = context.getContentResolver().query(BartProvider.Load.LOAD,
//                null,
//                LoadColumns.station + " = ? AND " + LoadColumns.route + " = ?",
//                new String[] { station, String.valueOf(routeId) },
//                LoadColumns.train + " ASC");
//        if (result != null && result.moveToFirst()) {
//            Log.d(TAG, String.format("%d records inserted", result.getCount()));
//            result.close();
//        } else {
//            Log.d(TAG, "No records found for request");
//        }

    }
}
