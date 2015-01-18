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

import java.util.concurrent.TimeUnit;

import pro.dbro.bart.api.BartApiResponseProcessor;
import pro.dbro.bart.api.xml.BartEstimate;
import pro.dbro.bart.api.xml.BartEtd;
import pro.dbro.bart.api.xml.BartEtdResponse;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;

/**
 * Created by davidbrodsky on 1/16/15.
 */
public class EtdAdapter extends RecyclerView.Adapter<EtdAdapter.EtdViewHolder> {

    private BartEtdResponse response;
    private ResponseRefreshListener listener;

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

        // Keep views up-to-date
        ViewObservable.bindView(host, Observable.timer(30, 30, TimeUnit.SECONDS))
                      .observeOn(AndroidSchedulers.mainThread())
                      .subscribeOn(AndroidSchedulers.mainThread())
                      .subscribe(time -> {
                          if (!BartApiResponseProcessor.updateEtdResponse(response)) {
                              listener.refreshRequested(this.response);
                          }
                          notifyDataSetChanged();
                          Log.i("Update", "timer tick " + time);
                      });
    }

    public void updateResponse(@NonNull BartEtdResponse newResponse) {
        this.response = newResponse;
        notifyDataSetChanged();
    }

    @Override
    public EtdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.etd_recyclerview_item, parent, false);
        return new EtdViewHolder(v);
    }

    @Override
    public void onBindViewHolder(EtdViewHolder holder, int position) {
        BartEtd etd = response.getEtds().get(position);

        holder.name.setText(etd.getDestination());

        int color = Color.parseColor(etd.getEstimates().get(0).getHexColor());
        holder.colorBand.setBackgroundColor(color);

        StringBuilder etdBuilder = new StringBuilder();
        for (BartEstimate estimate : etd.getEstimates()) {
            etdBuilder.append(estimate.getDeltaSecondsEstimate() / 60);
            etdBuilder.append(", ");
        }
        etdBuilder.delete(etdBuilder.length()-2, etdBuilder.length());
        holder.etds.setText(etdBuilder.toString());
    }

    @Override
    public int getItemCount() {
        return response.getEtds().size();
    }
}
