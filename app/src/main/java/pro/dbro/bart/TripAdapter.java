package pro.dbro.bart;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.util.List;

import pro.dbro.bart.api.xml.BartLeg;
import pro.dbro.bart.api.xml.BartTrip;
import pro.dbro.bart.drawable.StripeDrawable;

/**
 * Created by davidbrodsky on 1/16/15.
 */
public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private List<BartTrip> trips;

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

    public TripAdapter(@NonNull List<BartTrip> trips) {
        this.trips = trips;
    }

    public void swapEtds(@NonNull List<BartTrip> trips) {
        this.trips = trips;
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
        BartTrip trip = trips.get(position);

        String name = "Take ";
        for(int x = 0; x < trip.getLegs().size(); x++) {
            if (x > 0) name += "\ntransfer at " + trip.getLegs().get(x).getOrigin() + "\nto ";
            name += trip.getLegs().get(x).getTrainHeadStation();
        }

        holder.name.setText(name);

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
        return trips.size();
    }
}
