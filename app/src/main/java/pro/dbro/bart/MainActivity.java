package pro.dbro.bart;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import pro.dbro.bart.api.BartClient;
import pro.dbro.bart.api.xml.BartApiResponse;
import pro.dbro.bart.holdr.Holdr_ActivityMain;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.exceptions.OnErrorThrowable;
import rx.schedulers.Schedulers;


public class MainActivity extends Activity {
    private String TAG = getClass().getSimpleName();

    private Holdr_ActivityMain holdr;
    private BartClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        holdr = new Holdr_ActivityMain(findViewById(R.id.container));

        BartClient.getInstance()
                  .subscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(client -> {
                      this.client = client;
                      setupAutocomplete(client);
                  });

        Observable.merge(WidgetObservable.text(holdr.departureEntry),
                         WidgetObservable.text(holdr.destinationEntry))
                  .map(onTextChangeEvent -> doRequestForInputs(holdr.departureEntry.getText(),
                                                               holdr.destinationEntry.getText()))
                  .retry()
                  .subscribeOn(AndroidSchedulers.mainThread())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(new Observer<Observable<? extends BartApiResponse>>() {
                                 @Override
                                 public void onCompleted() {
                                     Log.i(TAG, "onComplete");
                                 }

                                 @Override
                                 public void onError(Throwable e) {
                                    Log.i(TAG, "onError ");
                                     // retry() seems to block this ever happening
                                 }

                                 @Override
                                 public void onNext(Observable<? extends BartApiResponse> observable) {
                                    Log.i(TAG, "onNext " + observable.getClass());
                                 }
                             }
                  );
    }

    private void setupAutocomplete(BartClient client) {
        ArrayList<String> stationList = new ArrayList<>();
        stationList.addAll(client.getStationNames());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, stationList);
        holdr.departureEntry.setAdapter(adapter);
        holdr.destinationEntry.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Observable<? extends BartApiResponse> doRequestForInputs(CharSequence departureInput,
                                                                     CharSequence destinationInput) {
        if (!TextUtils.isEmpty(departureInput)) {
            if (!TextUtils.isEmpty(destinationInput)) {
                return client.getRouteResponse(departureInput.toString(),
                                               destinationInput.toString());
            } else {
                return client.getEtdResponse(departureInput.toString());
            }
        }

        throw new IllegalStateException("No input values");
        //return Observable.error(OnErrorThrowable.from(new IllegalStateException(("No input values"))));
    }
}
