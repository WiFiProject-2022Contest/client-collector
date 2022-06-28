package wifilocation.wifi.customviews;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import wifilocation.wifi.R;

public class ScalableSpotImageView extends SubsamplingScaleImageView {

    private Bitmap bitmap_spot;
    private final PointF MAP_SIZE = new PointF(100, 50);

    public ScalableSpotImageView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ScalableSpotImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        bitmap_spot = BitmapFactory.decodeResource(getResources(), R.drawable.red_spot);
        bitmap_spot = Bitmap.createScaledBitmap(bitmap_spot, 30, 30, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap_spot, (float)getWidth() / 2 - 15, (float)getHeight() / 2 - 30, null);
    }

    public PointF sourceToMeter(PointF s_coord) {
        PointF size = new PointF(getSWidth(), getSHeight());
        float width_ratio = s_coord.x / size.x;
        float height_ratio = s_coord.y / size.y;
        return new PointF(MAP_SIZE.x * width_ratio, MAP_SIZE.y * height_ratio);
    }
}
