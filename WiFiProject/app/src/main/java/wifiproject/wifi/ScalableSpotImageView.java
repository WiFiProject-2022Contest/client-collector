package wifilocation.wifi;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

public class ScalableSpotImageView extends SubsamplingScaleImageView {

    private Bitmap bitmap_spot;
    float x = -1, y = -1;

    public ScalableSpotImageView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ScalableSpotImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        bitmap_spot = BitmapFactory.decodeResource(getResources(), R.drawable.spot);
        bitmap_spot = Bitmap.createScaledBitmap(bitmap_spot, 30, 30, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap_spot != null && x != -1 && y != -1)
            canvas.drawBitmap(bitmap_spot, x, y, null);
    }

    public void moveSpot(float x, float y) {
        this.x = x;
        this.y = y;
        invalidate();
    }
}
