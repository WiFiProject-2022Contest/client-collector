package wifilocation.wifi.search;

import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.davemorrissey.labs.subscaleview.ImageSource;

import java.util.ArrayList;
import java.util.List;

import wifilocation.wifi.MainActivity;
import wifilocation.wifi.R;
import wifilocation.wifi.model.WiFiItem;
import wifilocation.wifi.model.WiFiItemAdapter;
import wifilocation.wifi.customviews.SpotImageView;
import wifilocation.wifi.database.DatabaseHelper;
import wifilocation.wifi.serverconnection.RetrofitAPI;
import wifilocation.wifi.serverconnection.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {
    RecyclerView recyclerview_searched;
    SpotImageView imageview_map2;
    WiFiItemAdapter wifiitem_adapter = new WiFiItemAdapter();
    EditText edittext_x2, edittext_y2;
    TextView textview_date;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootview = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);

        context = getActivity().getApplicationContext();
        edittext_x2 = rootview.findViewById(R.id.editTextX2);
        edittext_y2 = rootview.findViewById(R.id.editTextY2);

        recyclerview_searched = rootview.findViewById(R.id.RecyclerViewSearched);
        recyclerview_searched.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        imageview_map2 = rootview.findViewById(R.id.imageViewMap2);
        switch (MainActivity.building) {
            case "Library5F":
                imageview_map2.setImage(ImageSource.resource(R.drawable.skku_library_5f));
                break;
            case "Library3F":
                imageview_map2.setImage(ImageSource.resource(R.drawable.skku_library_3f));
                break;
            case "WiFiLocation3F":
                imageview_map2.setImage(ImageSource.resource(R.drawable.wifilocation_gimpo_3f_room_temp_mezzanine_bottom));
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
                String from = textview_date_from.getText().toString();
                String to = textview_date_to.getText().toString();

                Float finalTarget_x = target_x;
                Float finalTarget_y = target_y;
                searchLocal(finalTarget_x, finalTarget_y, from, to);

                /* AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle("데이터베이스 선택");
                alertDialogBuilder.setCancelable(true);
                alertDialogBuilder.setPositiveButton("서버", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        searchRemote(finalTarget_x, finalTarget_y, from, to);
                    }
                });
                alertDialogBuilder.setNegativeButton("로컬", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        searchLocal(finalTarget_x, finalTarget_y, from, to);
                    }
                });
                alertDialogBuilder.setNeutralButton("모두", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        searchBoth(finalTarget_x, finalTarget_y, from, to);
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show(); */
            }
        });

        return rootview;
    }

    public void setDateText(String date) {
        textview_date.setText(date);
    }

    private List<WiFiItem> searchRemote(Float target_x, Float target_y, String from, String to) {
        RetrofitAPI retrofit_api = RetrofitClient.getRetrofitAPI();
        ArrayList<WiFiItem> items = new ArrayList<WiFiItem>();
        retrofit_api.getDataWiFiItem(MainActivity.building, MainActivity.ssid, target_x, target_y, from, to).enqueue(new Callback<List<WiFiItem>>() {
            @Override
            public void onResponse(Call<List<WiFiItem>> call, Response<List<WiFiItem>> response) {
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
        return items;
    }

    private List<WiFiItem> searchLocal(Float target_x, Float target_y, String from, String to) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        ArrayList<WiFiItem> items = new ArrayList<WiFiItem>();
        items.addAll(dbHelper.searchFromWiFiInfo(MainActivity.building, MainActivity.ssid, target_x, target_y, from.equals("") ? null : from, to.equals("") ? null : to, null));
        wifiitem_adapter.setItems(items);
        recyclerview_searched.setAdapter(wifiitem_adapter);
        imageview_map2.setSpot(items);

        Toast.makeText(context, "로컬 서치 완료", Toast.LENGTH_SHORT).show();
        return items;
    }

    private void searchBoth(Float target_x, Float target_y, String from, String to) {
        List<WiFiItem> items_ = searchRemote(target_x, target_y, from, to);
        items_.addAll(searchLocal(target_x, target_y, from, to));
        ArrayList<WiFiItem> items = new ArrayList<WiFiItem>();
        items.addAll(items_);
        wifiitem_adapter.setItems(items);
        recyclerview_searched.setAdapter(wifiitem_adapter);
        imageview_map2.setSpot(items);
    }
}