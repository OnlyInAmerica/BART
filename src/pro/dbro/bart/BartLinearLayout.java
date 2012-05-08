package pro.dbro.bart;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BartLinearLayout extends LinearLayout {
	
	
	public BartLinearLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public BartLinearLayout(Context context, AttributeSet as){
		super(context, as);
	}
	
	public BartLinearLayout(Context context, AttributeSet attrs, int defStyle)
	{
	    super(context, attrs, defStyle);
	}   

	// Override onInterceptTouchEvent to allow unfocusing of text inputs
				// On any ScrollView touch
	public boolean onInterceptTouchEvent(MotionEvent me){

		if(me.getAction() == me.ACTION_DOWN){
			Log.v("Scroll Touch","touchedD");
			if(findViewById(R.id.originTv).hasFocus()){
				findViewById(R.id.originTv).clearFocus();
			}
			else if(findViewById(R.id.destinationTv).hasFocus()){
				findViewById(R.id.destinationTv).clearFocus();
			}
			this.requestFocus();
		}
		
		// each following event (up to and including the final up) 
		// will be delivered first here and then to the target's onTouchEvent().
		return false;
	}
	
	public boolean onTouchEvent(MotionEvent me){
		return false;
		
	}

}
