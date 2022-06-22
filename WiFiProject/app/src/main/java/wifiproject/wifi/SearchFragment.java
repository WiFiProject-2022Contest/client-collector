package wifilocation.wifi;

import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {
    RecyclerView recyclerview_searched;
    SpotImageView imageview_map2;
    WiFiItemAdapter wifiitem_adapter = new WiFiItemAdapter();
    EditText edittext_x2, edittext_y2;
    String building = "";
    TextView textview_date;

    public void setBuilding(String building) {
        this.building = building;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootview = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);

        Context context = getActivity().getApplicationContext();
        edittext_x2 = rootview.findViewById(R.id.editTextX2);
        edittext_y2 = rootview.findViewById(R.id.editTextY2);

        recyclerview_searched = rootview.findViewById(R.id.RecyclerViewSearched);
        recyclerview_searched.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        imageview_map2 = rootview.findViewById(R.id.imageViewMap2);
        switch (building) {
            case "skku":
                imageview_map2.setImage(ImageSource.resource(R.drawable.skku_example));
                break;
            case "wifilocation":
                imageview_map2.setImage(ImageSource.resource(R.drawable.wifilocation_example));
                break;
            default:
                break;
        }
        imageview_map2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (imageview_map2.isReady()) {
                    PointF s_coord = imageview_map2.getCenter();
                    PointF meter_coord = imageview_map2.sourceToMeter(s_coord);

                    edittext_x2.setText(String.valueOf(meter_coord.x));
                    edittext_y2.setText(String.valueOf(meter_coord.y));
                }
                return false;
            }
        });

        TextView textview_date_from = rootview.findViewById(R.id.editTextDateFrom);
        textview_date_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textview_date = textview_date_from;
                DialogFragment datepicker = new DatePickerFragment();
                datepicker.show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        });
        TextView textview_date_to = rootview.findViewById(R.id.editTextDateTo);
        textview_date_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textview_date = textview_date_to;
                DialogFragment datepicker = new DatePickerFragment();
                datepicker.show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        });

        Button button_search = rootview.findViewById(R.id.buttonSearch);
        button_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Float target_x;
                Float target_y;
                try {
                    target_x = Float.parseFloat(edittext_x2.getText().toString());
                    target_y = Float.parseFloat(edittext_y2.getText().toString());
                } catch (Exception e) {
                    target_x = null;
                    target_y = null;
                }
                RetrofitAPI retrofit_api = RetrofitClient.getRetrofitAPI();
                retrofit_api.getData(target_x, target_y, textview_date_from.getText().toString(), textview_date_to.getText().toString()).enqueue(new Callback<List<WiFiItem>>() {
                    @Override
                    public void onResponse(Call<List<WiFiItem>> call, Response<List<WiFiItem>> response) {
                        ArrayList<WiFiItem> items = new ArrayList<WiFiItem>();
                        items.addAll(response.body());
                        wifiitem_adapter.setItems(items);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recyclerview_searched.setAdapter(wifiitem_adapter);
                                imageview_map2.setSpot(items);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<WiFiItem>> call, Throwable t) {
                    }
                });
            }
        });

        return rootview;
    }

    public void setDateText(String date) {
        textview_date.setText(date);
    }
}