package pro.dbro.bart;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import pro.dbro.bart.api.BartApiResponseProcessor;
import pro.dbro.bart.api.xml.BartEstimate;
import pro.dbro.bart.api.xml.BartEtd;
import pro.dbro.bart.api.xml.BartEtdResponse;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;

/**
 * Created by davidbrodsky on 1/16/15.
 */
public class EtdAdapter extends RecyclerView.Adapter<EtdAdapter.EtdViewHolder> {
    private final String TAG = getClass().getSimpleName();

    private final int RESPONSE_REFRESH_INTERVAL_S = 60;

    private BartEtdResponse response;
    private List<BartEtd> items;
    private ResponseRefreshListener listener;
    private static Subscription subscription;

    public static class EtdViewHolder extends RecyclerView.ViewHolder {

        public View colorBand;
        public TextView name;
        public TextView etds;

        private static Typeface typeface;

        public EtdViewHolder(View itemView) {
            super(itemView);
            colorBand = itemView.findViewById(R.id.color_band);
            name      = (TextView) itemView.findViewById(R.id.name);
            etds      = (TextView) itemView.findViewById(R.id.etds);

//            if (typeface == null)
//                typeface = Typeface.createFromAsset(itemView.getContext().getAssets(), "Dotmatrx.ttf");
//
//            name.setTypeface(typeface);
//            etds.setTypeface(typeface);
        }
    }

    public EtdAdapter(@NonNull BartEtdResponse response,
                      @NonNull RecyclerView host,
                      @NonNull ResponseRefreshListener listener) {

        this.listener = listener;
        this.response = response;
        this.items = new ArrayList<>();

        Observable.timer(0, 80, TimeUnit.MILLISECONDS)
                  .limit(response.getEtds().size())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribeOn(AndroidSchedulers.mainThread())
                  .subscribe(time -> {
                      items.add(response.getEtds().get(time.intValue()));
                      notifyItemInserted(time.intValue());
                      Log.i(TAG, "Notifying initial add");
                  });

        // Keep views up-to-date
        final int updateViewIntervalS = 30;
        unsubscribe();
        subscription = ViewObservable.bindView(host, Observable.timer(updateViewIntervalS, updateViewIntervalS, TimeUnit.SECONDS))
                      .observeOn(AndroidSchedulers.mainThread())
                      .subscribeOn(AndroidSchedulers.mainThread())
                      .subscribe(time -> {
                          try {
                              if (BartApiResponseProcessor.pruneEtdResponse(this.response) ||
                                  TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - response.getRequestDate().getTime()) > RESPONSE_REFRESH_INTERVAL_S) {
                                  this.listener.refreshRequested(this.response);
                              }
                          } catch (ParseException e) {
                              e.printStackTrace();
                              this.listener.refreshRequested(this.response);
                          }

                          notifyItemRangeChanged(0, response.getEtds().size() - 1);
                          Log.i(TAG, "timer tick " + time);
                      });
    }

    public void destroy() {
        unsubscribe();
    }

    private void unsubscribe() {
        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
    }

    public void updateResponse(@NonNull BartEtdResponse newResponse) {
        Log.i(TAG, "Updating response");
        List<BartEtd> newItems = newResponse.getEtds();
        this.response = newResponse;

        ArrayList<Integer> indexesToRemove = new ArrayList<>();
        ArrayList<Integer> indexesToAdd = new ArrayList<>();

        for (int x = 0; x < items.size(); x++) {
            boolean foundMatch = false;
            for(int y = 0; y < newItems.size(); y++) {
                if (items.get(x).sameStationAs(newItems.get(y))) {
                    items.set(x, newItems.get(y));
                    notifyItemChanged(x);
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) indexesToRemove.add(x);
        }

        for (int y = 0; y < newItems.size(); y++) {
            boolean foundMatch = false;
            for(int x = 0; x < items.size(); x++) {
                if (newItems.get(y).sameStationAs(items.get(x))) {
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) indexesToAdd.add(y);
        }

        for (Integer indexToAdd : indexesToAdd) {
            items.add(newItems.get(indexToAdd));
            notifyItemInserted(items.size() - 1);
        }

        for (Integer indexToRemove : indexesToRemove) {
            items.remove(indexToRemove.intValue());
            notifyItemRemoved(indexToRemove);
        }
    }

    @Override
    public EtdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.etd_recyclerview_item, parent, false);
        return new EtdViewHolder(v);
    }

    @Override
    public void onBindViewHolder(EtdViewHolder holder, int position) {
        BartEtd etd = items.get(position);

        holder.name.setText(etd.getDestination());

        int color = Color.parseColor(etd.getEstimates().get(0).getHexColor());
        holder.colorBand.setBackgroundColor(color);

        StringBuilder etdBuilder = new StringBuilder();
        for (BartEstimate estimate : etd.getEstimates()) {
            etdBuilder.append(Math.round(estimate.getDeltaSecondsEstimate() / 60f));
            etdBuilder.append(", ");
        }
        etdBuilder.delete(etdBuilder.length()-2, etdBuilder.length());
        holder.etds.setText(etdBuilder.toString());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
