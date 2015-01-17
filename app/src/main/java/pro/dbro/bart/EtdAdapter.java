package pro.dbro.bart;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import pro.dbro.bart.api.xml.BartEstimate;
import pro.dbro.bart.api.xml.BartEtd;

/**
 * Created by davidbrodsky on 1/16/15.
 */
public class EtdAdapter extends RecyclerView.Adapter<EtdAdapter.EtdViewHolder> {

    private List<BartEtd> etds;

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

    public EtdAdapter(@NonNull List<BartEtd> etds) {
        this.etds = etds;
    }

    public void swapEtds(@NonNull List<BartEtd> etds) {
        this.etds = etds;
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
        BartEtd etd = etds.get(position);

        holder.name.setText(etd.getDestination());

        int color = Color.parseColor(etd.getEstimates().get(0).getHexColor());
        holder.colorBand.setBackgroundColor(color);

        StringBuilder etdBuilder = new StringBuilder();
        for (BartEstimate estimate : etd.getEstimates()) {
            etdBuilder.append(estimate.getDeltaMinutesEstimate());
            etdBuilder.append(", ");
        }
        etdBuilder.delete(etdBuilder.length()-2, etdBuilder.length());
        holder.etds.setText(etdBuilder.toString());
    }

    @Override
    public int getItemCount() {
        return etds.size();
    }
}
