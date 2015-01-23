package pro.dbro.bart;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import pro.dbro.bart.api.BartApiResponseProcessor;
import pro.dbro.bart.api.BartClient;
import pro.dbro.bart.api.xml.BartEtd;
import pro.dbro.bart.api.xml.BartEtdResponse;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

// TODO Need to vibrate every time a train passes the usherDidAlert threshold, not just on first train
// TODO Bugs in tracking estimate. getEstimate(0) may not always be the first
// TODO Cleanup

/**
 * BartService owns the client responsible for Bart API calls.
 * While bound to by an activity OR in Usher mode, this service
 * maintains a map of the current Bart environment proximal to the user.
 *
 * Created by davidbrodsky on 11/4/14.
 */
public class BartService extends Service {
    public final String TAG = getClass().getSimpleName();

    private BartClient client;
    private BartServiceBinder mBinder;
    private Subscription subscription;
    private BartEtdResponse usherResponse;
    private BartEtd usherEtd;

    private boolean bound;
    private boolean ushering;
    private int usherTimeAlert;
    private boolean usherDidAlert;

    private final int NOTIFICATION_ID = 2357;
    private final int INTENT_STOP_USHER = 1468;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
    }

    private void startUsher(final String stationCode,
                            final String destinationStationCode,
                            int alertM) {

        usherDidAlert = false;
        usherTimeAlert = alertM;
        ushering = true;
        final int updateViewIntervalS = 1;
        unsubscribe();
        subscription = Observable.timer(0, updateViewIntervalS, TimeUnit.SECONDS)
                .filter(time -> client != null)
                .map(time -> time % 60 == 0) // Refresh API request every X updateViewInterval
                .flatMap(refreshRequest -> {
                    if (refreshRequest) {
                        Log.d(TAG, "Usher refreshing response");
                        return client.getEtd(stationCode);
                    } else
                        return Observable.just(null);
                })
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response != null && response.getEtdByDestination(destinationStationCode) != null) {
                        usherResponse = response;
                        usherEtd = response.getEtdByDestination(destinationStationCode);
                    } else if (usherResponse != null) {
                        BartApiResponseProcessor.pruneEtdResponse(usherResponse);
                    }
                    if (usherEtd != null)
                        showOrUpdateNotification(usherResponse.getStation().getName(), usherEtd);
                }, throwable -> throwable.printStackTrace());
    }

    private void stopUsher() {
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(NOTIFICATION_ID);
        unsubscribe();
        ushering = false;

        if (!bound) {
            Log.d(TAG, "StopUsher called while service not bound, shutting down");
            stopSelf();
        }
    }

    private void unsubscribe() {
        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
    }

    private void showOrUpdateNotification(String stationName, BartEtd etd) {

        int nextDepartureS = Math.max(0, (int) etd.getEstimates().get(0).getDeltaSecondsEstimate());

        Intent closeIntent = new Intent(this, BartService.class);
        closeIntent.putExtra("type", INTENT_STOP_USHER);
        PendingIntent closePendingIntent =
                PendingIntent.getService(
                        this,
                        0,
                        closeIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_bart)
                .setContentTitle("Next train in " +
                        makeTimeString(nextDepartureS))
                .setContentText(etd.getDestination() + " arriving at " + stationName)
                .setPriority(Notification.PRIORITY_MAX)
                .addAction(R.drawable.ic_close, "Stop", closePendingIntent);

        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle(builder);
        if (usherDidAlert) {
            style.bigText(etd.getDestination() + " arriving at " + stationName + "\nGet going!");
        } else {
            style.bigText(etd.getDestination() + " arriving at " + stationName + "\nWill vibrate when " + usherTimeAlert + "m away");
        }

        builder.setStyle(style);

        Intent resultIntent = new Intent(this, MainActivity.class);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent clickPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(clickPendingIntent);

        if (nextDepartureS < usherTimeAlert * 60 && nextDepartureS > 0 && !usherDidAlert) {
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            // Three 100ms buzzes
            v.vibrate(new long[] { 250, 100, 250, 100, 250, 100 }, -1);
            usherDidAlert = true;
        } else if (nextDepartureS == 0) usherDidAlert = false;

        mNotifyMgr.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        bound = true;
        if (mBinder == null) mBinder = new BartServiceBinder();
        Log.i(TAG, "Bind service");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Unbind service");
        bound = false;
        return false; // no call to onRebind needed
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        int startResult = super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "got start command with startId " + startId);
        if (intent != null && intent.getIntExtra("type", 0) == INTENT_STOP_USHER) {
            Log.d(TAG, "stopping service");
            stopUsher();
        }
        return startResult;
    }

    /** Binder through which Activities can interact with this Service */
    public class BartServiceBinder extends Binder {

//        public ChatService getService() {
//            // Return this instance of LocalService so clients can call public methods
//            return ChatService.this;
//        }

//        public void connect() {
//            mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(CONNECT));
//        }
//
//        public void sendPublicMessageFromPrimaryIdentity(String message) {
//            mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(SEND_MESSAGEE, message));
//        }

        public Observable<BartClient> getClient() {
            return BartClient.getInstance()
                    .cache()
                    .doOnNext(client -> BartService.this.client = client);
        }

        public void startUsher(String departureCode, String trainHeadCode, int alertM) {
            BartService.this.startUsher(departureCode, trainHeadCode, alertM);
        }

        public void shutdown() {
//            mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(SHUTDOWN));
        }
    }

//    /** Handler that processes Messages on a background thread */
//    private final class BackgroundThreadHandler extends Handler {
//        public BackgroundThreadHandler(Looper looper) {
//            super(looper);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case CONNECT:
//                    Log.i(TAG, "handling connect");
//                    mApp.makeAvailable();
//                    break;
//                case SEND_MESSAGEE:
//                    mApp.sendPublicMessageFromPrimaryIdentity((String) msg.obj);
//                    break;
//                case SHUTDOWN:
//                    Log.i(TAG, "handling shutdown");
//                    mApp.makeUnavailable();
//
//                    // Stop the service using the startId, so that we don't stop
//                    // the service in the middle of handling another job
//                    stopSelf(msg.arg1);
//                    break;
//            }
//        }
//    }

    /**
     * @return a String of form "MM:SS" from a raw ms value
     */
    private static String makeTimeString(long timeS) {
        long second = timeS % 60;
        long minute = (timeS / 60) % 60;

        return String.format("%01d:%02d", minute, second);
    }
}