package wifilocation.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ScanFragment extends Fragment {
    RecyclerView recyclerview_scanned;
    WiFiItemAdapter wifiitem_adpater = new WiFiItemAdapter();
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
                retrofit_api.postData(Integer.parseInt(edittext_x.getText().toString()), Integer.parseInt(edittext_y.getText().toString()),
                        wifiitem_adpater.getItems()).enqueue(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                    }

                    @Override
                    public void onFailure(Call<JSONObject> call, Throwable t) {
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
            items.add(new WiFiItem(result.SSID, result.BSSID, result.level, result.frequency, (int) (System.currentTimeMillis() / 1000)));
        }
        wifiitem_adpater.setItems(items);
        recyclerview_scanned.setAdapter(wifiitem_adpater);
    }

    private void scanFailure() {
        Toast.makeText(context, "Scan failed.", Toast.LENGTH_SHORT).show();
        wm.getScanResults();
    }
}