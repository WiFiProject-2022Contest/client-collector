package wifilocation.wifi;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanFragment extends Fragment {
    RecyclerView recyclerview_scanned;
    WiFiItemAdapter wifiitem_adpater = new WiFiItemAdapter();
    ScalableSpotImageView imageview_map;
    WifiManager wm;
    Context context;
    EditText edittext_x, edittext_y;

    private BroadcastReceiver wifi_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
        if (success) {
            scanSuccess();
        } else {
            scanFailure();
        }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootview = (ViewGroup) inflater.inflate(R.layout.fragment_scan, container, false);
        context = getActivity().getApplicationContext();
        edittext_x = rootview.findViewById(R.id.editTextX);
        edittext_y = rootview.findViewById(R.id.editTextY);

        recyclerview_scanned = rootview.findViewById(R.id.RecyclerViewScanned);
        recyclerview_scanned.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        imageview_map = rootview.findViewById(R.id.imageViewMap);
        switch(MainActivity.building) {
            case "Library5F":
                imageview_map.setImage(ImageSource.resource(R.drawable.skku_example));
                break;
            case "wifilocation":
                imageview_map.setImage(ImageSource.resource(R.drawable.wifilocation_example));
                break;
            default:
                break;
        }
        imageview_map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (imageview_map.isReady()) {
                    PointF s_coord = imageview_map.getCenter();
                    PointF meter_coord = imageview_map.sourceToMeter(s_coord);

                    edittext_x.setText(String.valueOf(meter_coord.x));
                    edittext_y.setText(String.valueOf(meter_coord.y));
                }
                return false;
            }
        });

        wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifi_receiver, filter);
        Button button_scan = rootview.findViewById(R.id.buttonScan);
        button_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Float.parseFloat(edittext_x.getText().toString());
                    Float.parseFloat(edittext_y.getText().toString());
                } catch (Exception e) {
                    Toast.makeText(context, "올바른 형식의 좌표 입력 필요", Toast.LENGTH_SHORT).show();
                    return;
                }
                wm.startScan();
            }
        });

        Button button_push = rootview.findViewById(R.id.buttonPush);
        button_push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RetrofitAPI retrofit_api = RetrofitClient.getRetrofitAPI();

                retrofit_api.postData(wifiitem_adpater.getItems().get(0).getX(), wifiitem_adpater.getItems().get(0).getY(),
                        wifiitem_adpater.getItems()).enqueue(new Callback<PushResultModel>() {
                    @Override
                    public void onResponse(Call<PushResultModel> call, Response<PushResultModel> response) {
                        PushResultModel r = response.body();
                        if (r.getSuccess().equals("true")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "PUSH 성공", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "PUSH 실패", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<PushResultModel> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        });

        return rootview;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        context.unregisterReceiver(wifi_receiver);
    }

    private void scanSuccess() {
        List<ScanResult> results = wm.getScanResults();
        ArrayList<WiFiItem> items = new ArrayList<WiFiItem>();
        float target_x, target_y;
        try {
            target_x = Float.parseFloat(edittext_x.getText().toString());
            target_y = Float.parseFloat(edittext_y.getText().toString());
        } catch (Exception e) {
            return;
        }
        for (ScanResult result : results) {
//            if (!result.SSID.equalsIgnoreCase("WiFiLocation@PDA")) continue;
            items.add(new WiFiItem(target_x, target_y, result.SSID, result.BSSID, result.level, result.frequency, MainActivity.uuid, MainActivity.building));
        }
        wifiitem_adpater.setItems(items);
        recyclerview_scanned.setAdapter(wifiitem_adpater);
    }

    private void scanFailure() {
        Toast.makeText(context, "Scan failed.", Toast.LENGTH_SHORT).show();
        wm.getScanResults();
    }
}