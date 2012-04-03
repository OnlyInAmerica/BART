package pro.dbro.bart;

import java.util.ArrayList;
import java.util.Date;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

public class ViewCountDownTimer extends CountDownTimer {
	
	ArrayList timerViews;

	public ViewCountDownTimer(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		// TODO Auto-generated constructor stub
	}
	public ViewCountDownTimer(ArrayList tViews, long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		timerViews = tViews;
		
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub

		sendMessage(2);
	}

	@Override
	public void onTick(long millisUntilFinished) {
		// TODO Auto-generated method stub
		long now = new Date().getTime(); // do this better
		
		for(int x=0;x<timerViews.size();x++){
			// eta - now = ms till arrival
			long eta = (Long) ((TextView)timerViews.get(x)).getTag();
			if(eta - now < 0){
				eta = 0;
			}
			else{
				eta = eta - now;
			}
			((TextView)timerViews.get(x)).setText(String.valueOf((eta) / ( 1000 *60)));
		}
	}
	
	private void sendMessage(int status) { // 0 = service stopped , 1 = service started, 2 = refresh view with call to bartApiRequest()
  	  Log.d("sender", "View countdown expired");
  	  Intent intent = new Intent("service_status_change");
  	  // You can also include some extra data.
  	  intent.putExtra("status", status);
  	  LocalBroadcastManager.getInstance(TheActivity.c).sendBroadcast(intent);
  	}

}
