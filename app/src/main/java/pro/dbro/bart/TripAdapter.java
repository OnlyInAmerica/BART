package pro.dbro.bart;

import android.graphics.Color;
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

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import pro.dbro.bart.api.BartApiResponseProcessor;
import pro.dbro.bart.api.xml.BartLoad;
import pro.dbro.bart.api.xml.BartLoadResponse;
import pro.dbro.bart.api.xml.BartQuickPlannerResponse;
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

    private static final SimpleDateFormat HUMAN_DATE_PRINTER = new SimpleDateFormat("h:mm", Locale.US);

    static {
        HUMAN_DATE_PRINTER.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
    }

    private BartQuickPlannerResponse response;
    private List<BartTrip> items;
    private ArrayList<LineData> plotItems = new ArrayList<>();
    private BartApiDelegate listener;
    private static Subscription subscription;

    public static class TripViewHolder extends RecyclerView.ViewHolder {

        public View colorBand;
        public TextView name;
        public TextView etds;
        public TextView arrival;
        public ViewGroup container;
        public LineChart chart;

        public TripViewHolder(View itemView) {
            super(itemView);
            colorBand = itemView.findViewById(R.id.color_band);
            name      = (TextView) itemView.findViewById(R.id.name);
            etds      = (TextView) itemView.findViewById(R.id.etds);
            arrival   = (TextView) itemView.findViewById(R.id.arrival);
            container = (ViewGroup) itemView.findViewById(R.id.container);
            chart     = (LineChart) itemView.findViewById(R.id.chart);
        }
    }

    public TripAdapter(@NonNull BartQuickPlannerResponse response,
                       @NonNull RecyclerView host,
                       @NonNull BartApiDelegate listener) {

        this.listener = listener;
        this.response = response;
        this.items = new ArrayList<>();

        Observable.timer(0, 80, TimeUnit.MILLISECONDS)
                .limit(response.getTrips().size())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> {
                    Log.i(TAG, "Adding trip " + time + "  of " + response.getTrips().size());
                    items.add(response.getTrips().get(time.intValue()));
                    notifyItemInserted(time.intValue());
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
                          notifyItemRangeChanged(0, items.size() - 1);
                          Log.i(TAG, "timer tick " + time);
                      });

    }

    public void setLoadData(LineData lineData) {
        plotItems.add(0, lineData);
        notifyItemChanged(0);
    }

//    public void setLoadData(BartLoadResponse response) {
//        List<BartLoad> loads = response.getLoads();
//        if (loads == null) {
//            Log.d(TAG, "Load response had no loads");
//            return;
//        }
//        int maxLoad = -1;
//        int leadLoadTrainId = -100;
//        for (BartLoad load : loads) {
//            if (leadLoadTrainId == -100) leadLoadTrainId = load.getTrainId();
//            maxLoad = Math.max(maxLoad, load.getLoad());
//        }
//
//        for (BartTrip trip : items) {
//            if (trip.getLegs().get(0).getTrainIndex() == leadLoadTrainId) {
//                trip.setMaxLoad(maxLoad);
//                Log.d(TAG, String.format("Attached %s load to trip %d with train id %d",
//                        BartLoad.getLoadDescription(maxLoad),
//                        items.indexOf(trip),
//                        leadLoadTrainId));
//                notifyItemChanged(items.indexOf(trip));
//                break;
//            }
//        }
//
//    }

    public void destroy() {
        unsubscribe();
    }

    private void unsubscribe() {
        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
    }

    public void updateResponse(@NonNull BartQuickPlannerResponse newResponse) {
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
        Log.i(TAG, String.format("Train %d bound to pos %d", trip.getLegs().get(0).getTrainIndex(),
                                                             position));

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
            holder.etds.setText(String.valueOf(
                    Math.max(0, Math.round(trip.getLegs().get(0).getOriginAsRelativeSec() / 60f))));
            StringBuilder arrivalText = new StringBuilder();
            arrivalText.append("Arrives ");
            arrivalText.append(HUMAN_DATE_PRINTER.format(trip.getDestAsDate()));
            if (position < plotItems.size()) {
                holder.chart.getXLabels().setSpaceBetweenLabels(4);
                holder.chart.setPadding(0, 0, 0, 0);
                holder.chart.setDrawGridBackground(false);
                holder.chart.setDescription("Historical Train Crowding");
                //holder.chart.getXLabels().setSpaceBetweenLabels();
                holder.chart.setData(plotItems.get(position));
                holder.chart.setDrawBorder(false);
                holder.chart.setDrawMarkerViews(false);
                holder.chart.setDrawVerticalGrid(false);
                holder.chart.setDrawHorizontalGrid(false);
                holder.chart.setDrawXLabels(true);
                holder.chart.setBorderWidth(0);
                holder.chart.setDrawYLabels(false);
                holder.chart.setDrawYValues(false);
                holder.chart.setDrawLegend(false);
                holder.chart.setVisibility(View.VISIBLE);
            }
//            if (trip.getMaxLoad() != 0) {
//                arrivalText.append(" | ");
//                arrivalText.append(BartLoad.getLoadDescription(trip.getMaxLoad()));
//                arrivalText.append(" crowd");
//            }
            holder.arrival.setText(arrivalText.toString());
        } catch (ParseException e) {
            holder.etds.setText("?");
            holder.arrival.setText("");
            e.printStackTrace();
        }
        holder.container.setTag(position);
        holder.container.setOnClickListener(view ->
                listener.loadRequested(items.get((int)view.getTag()).getOriginAbbreviation(),
                                       items.get((int)view.getTag()).getLegs().get(0).getLine()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
