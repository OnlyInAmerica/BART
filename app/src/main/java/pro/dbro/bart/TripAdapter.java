package pro.dbro.bart;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

import pro.dbro.bart.api.BartApiResponseProcessor;
import pro.dbro.bart.api.xml.BartLeg;
import pro.dbro.bart.api.xml.BartScheduleResponse;
import pro.dbro.bart.api.xml.BartTrip;
import pro.dbro.bart.drawable.StripeDrawable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;

/**
 * Created by davidbrodsky on 1/16/15.
 */
public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private BartScheduleResponse response;
    private ResponseRefreshListener listener;

    public static class TripViewHolder extends RecyclerView.ViewHolder {

        public View colorBand;
        public TextView name;
        public TextView etds;

        private static Typeface typeface;

        public TripViewHolder(View itemView) {
            super(itemView);
            colorBand = itemView.findViewById(R.id.color_band);
            name      = (TextView) itemView.findViewById(R.id.name);
            etds      = (TextView) itemView.findViewById(R.id.etds);

        }
    }

    public TripAdapter(@NonNull BartScheduleResponse response,
                       @NonNull RecyclerView host,
                       @NonNull ResponseRefreshListener listener) {

        this.listener = listener;
        this.response = response;

        // Keep views up-to-date
        ViewObservable.bindView(host, Observable.timer(30, 30, TimeUnit.SECONDS))
                      .observeOn(AndroidSchedulers.mainThread())
                      .subscribeOn(AndroidSchedulers.mainThread())
                      .subscribe(time -> {
                          if (!BartApiResponseProcessor.updateScheduleResponse(response)) {
                              listener.refreshRequested(this.response);
                          }
                          notifyDataSetChanged();
                          Log.i("Update", "timer tick " + time);
                      });

    }

    public void updateResponse(@NonNull BartScheduleResponse newResponse) {
        this.response = newResponse;
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
        BartTrip trip = response.getTrips().get(position);

        int startTransferSpan = 0;
        int endTransferSpan = 0;
        int[][] transferSpans = new int[trip.getLegs().size()-1][2];
        String name = "Take ";
        for(int x = 0; x < trip.getLegs().size(); x++) {
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
            holder.etds.setText(String.valueOf(trip.getLegs().get(0).getOriginAsRelativeSec() / 60));
        } catch (ParseException e) {
            holder.etds.setText("?");
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return response.getTrips().size();
    }
}
