package wifilocation.wifi;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.ArrayList;

public class SpotImageView extends SubsamplingScaleImageView {

    private ArrayList<PointF> positions = new ArrayList<PointF>();
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

    public PointF sourceToMeter(PointF pos) {
        PointF size = viewToSourceCoord(getWidth(), getHeight());
        float width_ratio = pos.x / size.x;
        float height_ratio = pos.y / size.y;
        return new PointF(MAP_SIZE.x * width_ratio, MAP_SIZE.y * height_ratio);
    }

    public PointF meterToSourceCoord(PointF m_coord) {
        PointF size = new PointF(getSWidth(), getSHeight());
        float width_ratio = m_coord.x / MAP_SIZE.x;
        float height_ratio = m_coord.y / MAP_SIZE.y;
        return new PointF(size.x * width_ratio, size.y * height_ratio);
    }
}
