package pro.dbro.bart;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import pro.dbro.bart.api.BartApiResponseProcessor;
import pro.dbro.bart.api.xml.BartScheduleResponse;
import pro.dbro.bart.api.xml.BartTrip;
import pro.dbro.bart.drawable.StripeDrawable;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;

/**
 * Created by davidbrodsky on 1/16/15.
 */
public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {
    private final String TAG = getClass().getSimpleName();

    private static final SimpleDateFormat HUMAN_DATE_PRINTER = new SimpleDateFormat("hh:mm", Locale.US);

    static {
        HUMAN_DATE_PRINTER.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
    }

    private BartScheduleResponse response;
    private List<BartTrip> items;
    private ResponseRefreshListener listener;
    private static Subscription subscription;

    public static class TripViewHolder extends RecyclerView.ViewHolder {

        public View colorBand;
        public TextView name;
        public TextView etds;
        public TextView arrival;

        public TripViewHolder(View itemView) {
            super(itemView);
            colorBand = itemView.findViewById(R.id.color_band);
            name      = (TextView) itemView.findViewById(R.id.name);
            etds      = (TextView) itemView.findViewById(R.id.etds);
            arrival   = (TextView) itemView.findViewById(R.id.arrival);
        }
    }

    public TripAdapter(@NonNull BartScheduleResponse response,
                       @NonNull RecyclerView host,
                       @NonNull ResponseRefreshListener listener) {

        this.listener = listener;
        this.response = response;
        this.items = new ArrayList<>();

        Observable.timer(0, 80, TimeUnit.MILLISECONDS)
                .limit(response.getTrips().size())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> {
                    items.add(response.getTrips().get(time.intValue()));
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
                          if (BartApiResponseProcessor.pruneScheduleResponse(this.response)) {
                              this.listener.refreshRequested(this.response);
                          }
                          notifyDataSetChanged();
                          Log.i(TAG, "timer tick " + time);
                      });

    }

    public void destroy() {
        unsubscribe();
    }

    private void unsubscribe() {
        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
    }

    public void updateResponse(@NonNull BartScheduleResponse newResponse) {
        this.response = newResponse;
        this.items = newResponse.getTrips();
        notifyDataSetChanged();
    }

    @Override
    public TripViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.route_recyclerview_item, parent, false);
        return new TripViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TripViewHolder holder, int position) {
        BartTrip trip = items.get(position);

        int startTransferSpan;
        int endTransferSpan;
        int[][] transferSpans = new int[trip.getLegs().size()-1][2];
        String name = "Take ";
        for(int x = 0; x < trip.getLegs().size(); x++)   {
            if (x > 0) {
                startTransferSpan = name.length();
                name += "\ntransfer at " + trip.getLegs().get(x).getOrigin() + "\n";
                endTransferSpan = name.length();
                name +="to ";
                transferSpans[x-1] = new int[] {startTransferSpan, endTransferSpan};

            }
            name += trip.getLegs().get(x).getTrainHeadStation();
        }

        Spannable nameSpannable = new SpannableString(name);
        for(int[] span : transferSpans) {
            TextAppearanceSpan tas = new TextAppearanceSpan(holder.name.getContext(), R.style.TransferText);
            nameSpannable.setSpan(tas, span[0], span[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        holder.name.setText(nameSpannable);

        if (trip.getLegs().size() == 1) {
            if (trip.getLegs().get(0).getHexColor() != null) {
                int color = Color.parseColor(trip.getLegs().get(0).getHexColor());
                holder.colorBand.setBackgroundColor(color);
            }
        } else {
            int[] colors = new int[trip.getLegs().size()];
            for(int x = 0; x < trip.getLegs().size(); x++) {
                colors[x] = Color.parseColor(trip.getLegs().get(x).getHexColor());
            }
            StripeDrawable drawable = new StripeDrawable(colors);
            holder.colorBand.setBackground(drawable);
        }

        try {
            holder.etds.setText(String.valueOf(Math.round(trip.getLegs().get(0).getOriginAsRelativeSec() / 60f)));
            holder.arrival.setText("Arrives " + HUMAN_DATE_PRINTER.format(trip.getDestAsDate()));
        } catch (ParseException e) {
            holder.etds.setText("?");
            holder.arrival.setText("");
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
