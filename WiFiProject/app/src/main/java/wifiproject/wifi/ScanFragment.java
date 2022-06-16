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
        imageview_map.setImage(ImageSource.resource(R.drawable.skku_example));
        final GestureDetector gesture_detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (imageview_map.isReady()) {
                    PointF s_coord = imageview_map.viewToSourceCoord(e.getX(), e.getY());
                    edittext_x.setText(String.valueOf(s_coord.x));
                    edittext_y.setText(String.valueOf(s_coord.y));
                    imageview_map.moveSpot(s_coord.x, s_coord.y);
                }
                return super.onSingleTapConfirmed(e);
            }
        });
        imageview_map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gesture_detector.onTouchEvent(motionEvent);
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
                wm.startScan();
            }
        });

        Button button_push = rootview.findViewById(R.id.buttonPush);
        button_push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RetrofitAPI retrofit_api = RetrofitClient.getRetrofitAPI();
                retrofit_api.postData(Float.parseFloat(edittext_x.getText().toString()), Float.parseFloat(edittext_y.getText().toString()),
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
                        }
                        else {
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

    private void scanSuccess() {
        List<ScanResult> results = wm.getScanResults();
        ArrayList<WiFiItem> items = new ArrayList<WiFiItem>();
        for (ScanResult result : results) {
//            if (!result.SSID.equalsIgnoreCase("WiFiLocation@PDA")) continue;
            Log.v("***", result.BSSID);
            items.add(new WiFiItem(result.SSID, result.BSSID, result.level, result.frequency, GetDevicesUUID(context), "skku"));
        }
        wifiitem_adpater.setItems(items);
        recyclerview_scanned.setAdapter(wifiitem_adpater);
    }

    private void scanFailure() {
        Toast.makeText(context, "Scan failed.", Toast.LENGTH_SHORT).show();
        wm.getScanResults();
    }

    private String GetDevicesUUID(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getActivity().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        Log.d("UUID_generater", deviceId);
        return deviceId;
    }
}