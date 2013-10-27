package pro.dbro.bart;

import android.os.Bundle;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.BubbleIconFactory;

/**
 * Created by davidbrodsky on 10/27/13.
 */
public class BARTMapFragment extends SupportMapFragment {

    public void onCreate(Bundle savedInstanceState){
        BubbleIconFactory bif = new BubbleIconFactory(getActivity());
        LatLng pos = new LatLng(0,0);
        getMap().addMarker(new MarkerOptions()
            .icon(BitmapDescriptorFactory.fromBitmap(bif.makeIcon("hey there!")))
            .position(pos)
            .title("title")
        );
    }
}
