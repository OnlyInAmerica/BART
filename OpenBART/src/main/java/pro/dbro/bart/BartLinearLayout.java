package pro.dbro.bart;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
	// On any screen touch outside input
	public boolean onInterceptTouchEvent(MotionEvent me){
		// On  any layout touch outside of the text inputs, set focus to layout (thus hiding soft keyboard)
		// and pass the touch event downstream
		// Thus, the text inputs can still respond and take focus if needed
		// BUT touches with no clear intent remove focus from the text views and remove keyboard
		if(me.getAction() == me.ACTION_DOWN && (findViewById(R.id.originTv).hasFocus() || findViewById(R.id.destinationTv).hasFocus())){
			// If the touch occurs in the area of the text inputs:
			if(!isPointInsideView(me.getRawX(), me.getRawY(), (findViewById(R.id.inputLinearLayout)))){
				TheActivity.hideSoftKeyboard(this);
				this.requestFocus();
				//if(isPointInsideView(me.getRawX(), me.getRawY(), findViewById(R.id.map)) || isPointInsideView(me.getRawX(), me.getRawY(), (findViewById(R.id.reverse))) )
				//	return false; // allow direct touch of map and reverse buttons from text editing
				return false; // pass all touches downstream
			}
		}
		
		// each following event (up to and including the final up) 
		// will be delivered first here and then to the target's onTouchEvent().
		//return false - tablelayout views animate
		// return true - no touches get passed
		return false;
	}
	
	public boolean onTouchEvent(MotionEvent me){
		return false;
		
	}
	
	/**
	 * Determines if given points are inside view
	 * @param x - x coordinate of point
	 * @param y - y coordinate of point
	 * @param view - view object to compare
	 * @return true if the points are within view bounds, false otherwise
	 */
	private boolean isPointInsideView(float x, float y, View view){
	    int location[] = new int[2];
	    view.getLocationOnScreen(location);
	    int viewX = location[0];
	    int viewY = location[1];

	    //point is inside view bounds
	    if(( x > viewX && x < (viewX + view.getWidth())) &&
	            ( y > viewY && y < (viewY + view.getHeight()))){
	        return true;
	    } else {
	        return false;
	    }
	}

}
