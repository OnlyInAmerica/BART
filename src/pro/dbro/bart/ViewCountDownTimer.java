package pro.dbro.bart;

import android.os.CountDownTimer;
import android.widget.TextView;

public class ViewCountDownTimer extends CountDownTimer {
	
	TextView textView;

	public ViewCountDownTimer(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		// TODO Auto-generated constructor stub
	}
	public ViewCountDownTimer(TextView tv, long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		textView = tv;
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		textView.setText("0");
	}

	@Override
	public void onTick(long millisUntilFinished) {
		// TODO Auto-generated method stub
		textView.setText( String.valueOf((millisUntilFinished) / ( 1000 *60) ) );

	}

}
