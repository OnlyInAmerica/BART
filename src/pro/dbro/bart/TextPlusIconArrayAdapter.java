package pro.dbro.bart;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

//  Intended to be used on AutoCompleteTextViews for origin and destination station
//  Assume that objects is a 2d array of [String value, String type]. i.e: ["Downtown Berkeley", "nearby"], ["Downtown Berkeley", "recent"]
public class TextPlusIconArrayAdapter extends ArrayAdapter<StationSuggestion> implements Filterable{
	private ArrayList<StationSuggestion> stations;
	//necessary?
	private final Context context;
	
	private Filter mFilter;
	
	// Custom constructor ignores view resource id, since it will always be R.layout.dropdown_item
	public TextPlusIconArrayAdapter(Context context, ArrayList<StationSuggestion> values) {
	    super(context, R.layout.dropdown_item);
	    this.stations = values;
	    this.context = context; //necessary?
	}
	
	@Override
	public int getCount() {
	    return stations.size();
	}

	@Override
	public StationSuggestion getItem(int position) {
	    return (StationSuggestion)stations.get(position);
	}
	
	public Filter getFilter() {
	    if (mFilter == null) {
	        mFilter = new CustomFilter();
	    }
	    return mFilter;
	}
	
	// For the recent/nearby stations, don't perform any filtering
	// When search text entered
	// Eventually let this ArrayAdapter handle both station suggestions and 
	// all stations
	private class CustomFilter extends Filter {

	    @Override
	    protected FilterResults performFiltering(CharSequence constraint) {
	        FilterResults results = new FilterResults();
	        /*
	        if(constraint == null || constraint.length() == 0) {
	            ArrayList<String> list = new ArrayList<String>(mOrigionalValues);
	            results.values = list;
	            results.count = list.size();
	        } else {
	            ArrayList<String> newValues = new ArrayList<String>();
	            for(int i = 0; i < mOrigionalValues.size(); i++) {
	                String item = mOrigionalValues.get(i);
	                if(item.contains(constraint)) {
	                    newValues.add(item);
	                }
	            }
	            results.values = newValues;
	            results.count = newValues.size();
	        }       
			*/
	        results.values = stations;
	        results.count = stations.size();
	        return results;
	    }

	    @SuppressWarnings("unchecked")
	    @Override
	    protected void publishResults(CharSequence constraint,
	            FilterResults results) {
	        stations = (ArrayList<StationSuggestion>) results.values;
	        //Log.d("CustomArrayAdapter", String.valueOf(results.values));
	        //Log.d("CustomArrayAdapter", String.valueOf(results.count));
	        notifyDataSetChanged();
	    }

	}


	 @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
		 LayoutInflater inflater = (LayoutInflater) this.getContext()
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		 //Log.d("CustomArrayAdapter","getView");
		 View rowView = inflater.inflate(R.layout.dropdown_item, null, true);
		 TextView nameView = (TextView)rowView.findViewById(R.id.dropdowntext);
		 ImageView iconView = (ImageView)rowView.findViewById(R.id.dropdownicon);
		 
		 //  Assume objects is a 2d String array i.e: ["Downtown Berkeley", "nearby"]
		 //  Set nameView to station name
		 nameView.setText( ((StationSuggestion)this.getItem(position)).station );
		 //  Set iconView to icon representing type
		 if( ((StationSuggestion)this.getItem(position)).type.compareTo("nearby") == 0 ){
			 iconView.setImageResource(R.drawable.reticle);
		 }
		 else if( ((StationSuggestion)this.getItem(position)).type.compareTo("recent") == 0 ){
			 iconView.setImageResource(R.drawable.clock);
		 }
		 
		 return rowView;
	 }

}
