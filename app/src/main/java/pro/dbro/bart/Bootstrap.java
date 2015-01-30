package pro.dbro.bart;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import pro.dbro.bart.api.BartClient;
import pro.dbro.bart.api.xml.BartLoadResponse;
import pro.dbro.bart.api.xml.BartRoute;
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

        // 1.25.2015 : The Bart Route Schedule endpoint currently fails for the following routes
        // 1.29.2015 : Seems resolved
        //final Set<Integer> unavailableRoutes = new HashSet<Integer>() {{ add(5); add(6); add(7); add(8);}};

        final AtomicInteger delayMultiplier = new AtomicInteger(0);

        Log.d(TAG, "Bootstrapping route load data");
        // NOTE : Too excited to figure out variable delay, manually pulled routes one by one :)
        Observable.just(20)
        //Observable.from(client.getRoutesNumbers())
                .subscribeOn(Schedulers.io())
                //.filter(routeNum -> !unavailableRoutes.contains(routeNum))
                  //.limit(1)
                  .flatMap(routeNum -> {
                      Log.d(TAG, String.format("Requesting load for route %d", routeNum));
                      return client.getRouteLoad(context, routeNum, false);
                  })
                  .subscribe(nullCursor -> {
                      Log.d(TAG, String.format("%d loads fetched.", BartClient.loadItems.size()));
                  }, throwable -> {
                      Log.d(TAG, "onError: ");
                      throwable.printStackTrace();
                  });
    }
}
