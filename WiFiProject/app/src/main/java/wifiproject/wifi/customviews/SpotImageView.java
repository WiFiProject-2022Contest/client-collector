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

import java.util.ArrayList;

import wifilocation.wifi.MainActivity;
import wifilocation.wifi.R;
import wifilocation.wifi.model.WiFiItem;

public class SpotImageView extends SubsamplingScaleImageView {

    private ArrayList<PointF> positions = new ArrayList<PointF>();
    private Bitmap bitmap_green_spot;
    private Bitmap bitmap_red_spot;

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
        for (PointF m_coord : positions) {
            PointF s_coord = meterToSourceCoord(m_coord);
            PointF v_coord = sourceToViewCoord(s_coord);
            canvas.drawBitmap(bitmap_green_spot, v_coord.x, v_coord.y, null);
        }
    }

    public void setSpot(ArrayList<WiFiItem> items) {
        setScaleAndCenter(0.0f, getCenter());
        positions.clear();
        float x = -1, y = -1;
        for (WiFiItem item : items) {
            if (item.getX() == x && item.getY() == y) continue;
            x = item.getX();
            y = item.getY();
            positions.add(new PointF(x, y));
        }
        invalidate();
    }

    public void setEstimateSpot(ArrayList<PointF> result) {
        setScaleAndCenter(0.0f, getCenter());
        positions.clear();
        positions = result;
        invalidate();
    }

    public PointF sourceToMeter(PointF pos) {
        PointF size = viewToSourceCoord(getWidth(), getHeight());
        float width_ratio = pos.x / size.x;
        float height_ratio = pos.y / size.y;
        return new PointF(MainActivity.mapSize.x * width_ratio, MainActivity.mapSize.y * height_ratio);
    }

    public PointF meterToSourceCoord(PointF m_coord) {
        PointF size = new PointF(getSWidth(), getSHeight());
        float width_ratio = m_coord.x / MainActivity.mapSize.x;
        float height_ratio = m_coord.y / MainActivity.mapSize.y;
        return new PointF(size.x * width_ratio, size.y * height_ratio);
    }
}
