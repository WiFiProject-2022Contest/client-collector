package wifilocation.wifi;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.ArrayList;
import java.util.HashMap;

public class SpotImageView extends SubsamplingScaleImageView {

    private ArrayList<Float> xs = new ArrayList<Float>();
    private ArrayList<Float> ys = new ArrayList<Float>();
    private Bitmap bitmap_spot;

    public SpotImageView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SpotImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        bitmap_spot = BitmapFactory.decodeResource(getResources(), R.drawable.spot);
        bitmap_spot = Bitmap.createScaledBitmap(bitmap_spot, 30, 30, true);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int size = xs.size();
        if(size == ys.size()) {
            for (int i = 0; i < size; i++) {
                canvas.drawBitmap(bitmap_spot, xs.get(i), ys.get(i), null);
            }
        }
        invalidate();
    }

    public void setSpot(float x, float y) {
        xs.add(x);  ys.add(y);
    }
}
