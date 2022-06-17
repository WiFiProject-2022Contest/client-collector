package wifilocation.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstimateFragment extends Fragment {
    Context context;
    WifiManager wm;
    Button buttonEstimate;
    TextView textResultEstimate;
    List<WiFiItem> databaseAllData;
    List<WiFiItem> userData;

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
        context = getActivity().getApplicationContext();
        wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifi_receiver, filter);

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_estimate, container, false);
        buttonEstimate = rootView.findViewById(R.id.buttonEstimate);
        textResultEstimate = rootView.findViewById(R.id.textResultEstimate);

        // DB 전체 다 받아오기
        RetrofitAPI retrofit_api = RetrofitClient.getRetrofitAPI();
        retrofit_api.getData(-1, -1).enqueue(new Callback<List<WiFiItem>>() {
            @Override
            public void onResponse(Call<List<WiFiItem>> call, Response<List<WiFiItem>> response) {
                databaseAllData = response.body();
                Toast.makeText(context, "Database loaded.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<WiFiItem>> call, Throwable t) {
                Toast.makeText(context, "Database failed.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonEstimate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wm.startScan();
                Toast.makeText(context, "Scan started.", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        context.unregisterReceiver(wifi_receiver);
    }

    private void scanSuccess() {
        List<ScanResult> results = wm.getScanResults();
        Toast.makeText(context, "Scan finished.", Toast.LENGTH_SHORT).show();
        userData = new ArrayList<WiFiItem>();
        for (ScanResult result : results) {
            userData.add(new WiFiItem(0, 0, result.SSID, result.BSSID, result.level, result.frequency, null, null));
        }

        double[] estimatedPosition = PositioningAlgorithm.run(userData, databaseAllData);
        textResultEstimate.setText(String.format("(%s, %s)", estimatedPosition[0], estimatedPosition[1]));
        Toast.makeText(context, "Estimation finished.", Toast.LENGTH_SHORT).show();
    }

    private void scanFailure() {
        Toast.makeText(context, "Scan failed.", Toast.LENGTH_SHORT).show();
        wm.getScanResults();
    }
}