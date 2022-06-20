package wifilocation.wifi;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.ArrayList;

public class SpotImageView extends SubsamplingScaleImageView {

    private ArrayList<Float> xs = new ArrayList<Float>();
    private ArrayList<Float> ys = new ArrayList<Float>();
    private Bitmap bitmap_green_spot;
    private Bitmap bitmap_red_spot;
    private final PointF MAP_SIZE = new PointF(100, 50);

    public SpotImageView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SpotImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        bitmap_green_spot = BitmapFactory.decodeResource(getResources(), R.drawable.green_spot);
        bitmap_green_spot = Bitmap.createScaledBitmap(bitmap_green_spot, 30, 30, true);
        bitmap_red_spot = BitmapFactory.decodeResource(getResources(), R.drawable.red_spot);
        bitmap_red_spot = Bitmap.createScaledBitmap(bitmap_red_spot, 30, 30, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap_red_spot, (float)getWidth() / 2 - 15, (float)getHeight() / 2 - 30, null);
        int size = xs.size();
        if(size == ys.size()) {
            for (int i = 0; i < size; i++) {
                PointF pos = sourceToViewCoord(xs.get(i), ys.get(i));
                canvas.drawBitmap(bitmap_green_spot, pos.x, pos.y, null);
            }
        }
    }

    public void setSpot(ArrayList<WiFiItem> items) {
        setScaleAndCenter(0.0f, getCenter());
        xs.clear(); ys.clear();
        for (WiFiItem item : items) {
            xs.add(item.getX());
            ys.add(item.getY());
        }
        invalidate();
    }

    public PointF sourceToMeter(PointF pos) {
        PointF size = viewToSourceCoord(getWidth(), getHeight());
        float width_ratio = pos.x / size.x;
        float height_ratio = pos.y / size.y;
        return new PointF(MAP_SIZE.x * width_ratio, MAP_SIZE.y * height_ratio);
    }
}
