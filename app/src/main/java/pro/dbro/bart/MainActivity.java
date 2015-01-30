package pro.dbro.bart;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import pro.dbro.bart.api.BartClient;
import pro.dbro.bart.api.xml.BartApiResponse;
import pro.dbro.bart.api.xml.BartEtdResponse;
import pro.dbro.bart.api.xml.BartLoad;
import pro.dbro.bart.api.xml.BartQuickPlannerResponse;
import pro.dbro.bart.db.BartProvider;
import pro.dbro.bart.db.LoadColumns;
import pro.dbro.bart.holdr.Holdr_ActivityMain;
import rx.Observable;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;


public class MainActivity extends Activity implements BartApiDelegate, ServiceConnection {
    private String TAG = getClass().getSimpleName();

    private Holdr_ActivityMain holdr;
    private BartService.BartServiceBinder binder;
    private Subscription subscription;

    private boolean mServiceBound = false;  // Are we bound to the ChatService?

    private View.OnFocusChangeListener inputFocusListener = (inputTextView, hasFocus) -> {
        if (inputTextView.getTag(R.id.textview_memory) != null &&
            !hasFocus &&
            TextUtils.isEmpty(((TextView) inputTextView).getText())) {
                ((TextView)inputTextView).setText(inputTextView.getTag(R.id.textview_memory).toString());
        }
        else if (hasFocus && !TextUtils.isEmpty(((TextView) inputTextView).getText())) {
            inputTextView.setTag(R.id.textview_memory, ((TextView) inputTextView).getText());
            ((TextView) inputTextView).setText("");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        holdr = new Holdr_ActivityMain(findViewById(R.id.container));
        holdr.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        holdr.recyclerView.setItemAnimator(new DefaultItemAnimator());

        holdr.departureEntry.setOnFocusChangeListener(inputFocusListener);
        holdr.destinationEntry.setOnFocusChangeListener(inputFocusListener);

        holdr.reverse.setOnClickListener(view -> swapInputs());

        setActionBar(holdr.toolbar);

        subscription = AppObservable.bindActivity(this,
                Observable.merge(WidgetObservable.text(holdr.departureEntry),
                                 WidgetObservable.text(holdr.destinationEntry)))

                .throttleLast(10, TimeUnit.MILLISECONDS)

                .distinctUntilChanged(textChangedEvent ->
                        holdr.departureEntry.getText().hashCode() ^
                                holdr.destinationEntry.getText().hashCode())

                .flatMap(onTextChangeEvent ->
                        doRequestForInputs(holdr.departureEntry.getText(),
                                holdr.destinationEntry.getText()))

                .retry()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    Log.i(TAG, "onNext " + response.getClass());
                    displayResponse(response);
                }, throwable -> Log.i(TAG, throwable.getMessage()));

    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mServiceBound) {
            startAndBindToService();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        saveInput();
        if (mServiceBound) {
            unBindService();
        }
    }

    private void startAndBindToService() {
        Log.i(TAG, "Starting service");
        Intent intent = new Intent(this, BartService.class);
        startService(intent);
        bindService(intent, this, 0);
    }

    private void unBindService() {
        unbindService(this);
    }

    private void swapInputs() {
        String departureEntry = holdr.departureEntry.getText().toString();

        holdr.departureEntry.setText(holdr.destinationEntry.getText());
        holdr.destinationEntry.setText(departureEntry);

        ObjectAnimator.ofFloat(holdr.reverse, "rotation", 0f, 180f).start();
        holdr.recyclerView.requestFocus();
    }

    private void restorePreviousInput() {
        SharedPreferences prefs = getSharedPreferences("app", Context.MODE_PRIVATE);
        holdr.departureEntry.setText(prefs.getString("orig", ""));
        holdr.destinationEntry.setText(prefs.getString("dest", ""));
        holdr.departureEntry.dismissDropDown();
        holdr.destinationEntry.dismissDropDown();
    }

    private void saveInput() {
        getSharedPreferences("app", Context.MODE_PRIVATE).edit()
                .putString("orig", holdr.departureEntry.getText().toString())
                .putString("dest", holdr.destinationEntry.getText().toString())
                .apply();
    }

    private void setupAutocomplete(BartClient client) {
        ArrayList<String> stationList = new ArrayList<>();
        stationList.addAll(client.getStationNames());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, stationList);
        holdr.departureEntry.setAdapter(adapter);
        holdr.destinationEntry.setAdapter(adapter);
    }

    private void displayResponse(BartApiResponse response) {
        if (response instanceof BartEtdResponse) {
            if (getCurrentFocus() != holdr.destinationEntry)
                hideSoftKeyboard(holdr.departureEntry);
            BartEtdResponse etdResponse = (BartEtdResponse) response;
            if (etdResponse.getEtds() != null && etdResponse.getEtds().size() != 0) {
                if (holdr.recyclerView.getAdapter() instanceof EtdAdapter) {
                    ((EtdAdapter) holdr.recyclerView.getAdapter()).updateResponse(etdResponse);
                } else {
                    if (holdr.recyclerView.getAdapter() != null) ((TripAdapter) holdr.recyclerView.getAdapter()).destroy();
                    holdr.recyclerView.setAdapter(new EtdAdapter(etdResponse, holdr.recyclerView, MainActivity.this));
                }
            } else
                notifyNoTrips();
        }
        else if (response instanceof BartQuickPlannerResponse) {
            hideSoftKeyboard(holdr.destinationEntry);
            BartQuickPlannerResponse routeResponse = (BartQuickPlannerResponse) response;
            if (routeResponse.getTrips() != null && routeResponse.getTrips().size() != 0) {
                if (holdr.recyclerView.getAdapter() instanceof TripAdapter) {
                    ((TripAdapter) holdr.recyclerView.getAdapter()).updateResponse(routeResponse);
                } else {
                    if (holdr.recyclerView.getAdapter() != null) ((EtdAdapter) holdr.recyclerView.getAdapter()).destroy();
                    holdr.recyclerView.setAdapter(new TripAdapter(routeResponse, holdr.recyclerView, this));
                }
            } else
                notifyNoTrips();
        }
        if (holdr.recyclerView.getAdapter().getItemCount() > 0)
            holdr.recyclerView.smoothScrollToPosition(0);
    }

    private void notifyNoTrips() {
        // TODO
        Toast.makeText(this, "No more trains available tonight", Toast.LENGTH_LONG).show();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    public void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
    }

    private void hideSoftKeyboard (View view) {
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    private Observable<? extends BartApiResponse> doRequestForInputs(CharSequence departureInput,
                                                                     CharSequence destinationInput) {

        if (!TextUtils.isEmpty(departureInput)) {
            if (!TextUtils.isEmpty(destinationInput)) {
                return binder.getClient()
                             .flatMap(client -> client.getRoute(departureInput.toString(),
                                     destinationInput.toString()));
            } else {
                return binder.getClient()
                             .flatMap(client -> client.getEtd(departureInput.toString()));
            }
        }

        throw new IllegalStateException("No input values");
        //return Observable.error(OnErrorThrowable.from(new IllegalStateException(("No input values"))));
    }

    @Override
    public void refreshRequested(BartApiResponse oldResponse) {
        if (!mServiceBound) return;

        if (oldResponse instanceof BartEtdResponse) {
            binder.getClient()
                  .flatMap(client -> client.getEtd(((BartEtdResponse) oldResponse).getStation().getName()))
                  .subscribe(this::displayResponse);
        }
        else if (oldResponse instanceof BartQuickPlannerResponse) {
            binder.getClient()
                  .flatMap(client -> client.getRoute(((BartQuickPlannerResponse) oldResponse).getOriginAbbreviation(),
                          ((BartQuickPlannerResponse) oldResponse).getDestinationAbbreviation()))
                  .subscribe(this::displayResponse);
        }
    }

    @Override
    public void loadRequested(String departureStation, String routeId) {
        if (!mServiceBound) return;

        Cursor cursor = getContentResolver().query(BartProvider.Load.LOAD,
                                   null,
                                   LoadColumns.route + " = ? AND " + LoadColumns.station + " = ?",
                                   new String[] {routeId.split(" ")[1], departureStation},
                                   LoadColumns.train + " ASC");

        if (cursor != null && cursor.getCount() > 0) {
            ArrayList<Entry> entryList = new ArrayList<>(cursor.getCount());
            ArrayList<String> xVals = new ArrayList<>();
            int idx = 0;
            while (cursor.moveToNext()) {
                entryList.add(new Entry((float) cursor.getInt(cursor.getColumnIndex(LoadColumns.load)), idx));
                xVals.add(cursor.getString(cursor.getColumnIndex(LoadColumns.time)));
                idx++;
            }
            cursor.close();

            LineDataSet loadSet = new LineDataSet(entryList, "data");
            loadSet.setDrawCircles(false);
            loadSet.setDrawCubic(true);
            loadSet.setDrawFilled(true);
            loadSet.setColor(getResources().getColor(R.color.bartBlue));
            loadSet.setLineWidth(5);
            LineData data = new LineData(xVals, loadSet);

            if (holdr.recyclerView.getAdapter() instanceof TripAdapter)
                ((TripAdapter) holdr.recyclerView.getAdapter()).setLoadData(data);
            else
                Log.w(TAG, "Load data ready but current adapter not TripAdapter");
        }
    }

    @Override
    public void usherRequested(String departureStation, String trainHeadStation) {
        showUsherDialog(departureStation, trainHeadStation);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i(TAG, "onServiceConnected");
        binder = (BartService.BartServiceBinder) service;
        mServiceBound = true;

        binder.getClient()
              .observeOn(AndroidSchedulers.mainThread())
              .subscribeOn(AndroidSchedulers.mainThread())
              .subscribe(client -> {
                  Log.d(TAG, "Got bart client");
                  setupAutocomplete(client);
                  restorePreviousInput();
              }, throwable -> {Log.e(TAG, "Fucckkk"); throwable.printStackTrace();});
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(TAG, "onServiceDisconnected");
        mServiceBound = false;
        binder = null;
    }

    private void showUsherDialog(String departureStation, String trainHeadStation) {

            final NumberPicker picker = new NumberPicker(this);
            picker.setMaxValue(120);
            picker.setMinValue(2);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(picker)
                    .setTitle("Watch " + departureStation)
                    .setPositiveButton("Ok", (dialog, which) -> binder.startUsher(departureStation, trainHeadStation, picker.getValue()))
                    .show();
    }
}
