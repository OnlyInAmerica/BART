package pro.dbro.bart.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public class StripeDrawable extends Drawable {
    private final Paint mPaint;
    private int[] mColors;

    public StripeDrawable(int[] colors) {
        mPaint = new Paint();
        mColors = colors;
    }

    @Override
    public void draw(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);

        for (int x = 0; x < mColors.length; x++) {
            mPaint.setColor(mColors[x]);
            canvas.drawRect(0f, (x) * (1f / mColors.length) * canvas.getHeight(), canvas.getWidth(), (x + 1) * (1f / mColors.length) * canvas.getHeight(), mPaint);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setAlpha(int arg0) {
    }

    @Override
    public void setColorFilter(ColorFilter arg0) {
    }
}