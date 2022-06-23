package wifilocation.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.method.ScrollingMovementMethod;
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
    Button buttonUpdateAllDatabase;
    Button buttonEstimate;
    TextView textResultEstimateWiFi2G;
    TextView textResultEstimateWiFi5G;
    TextView textEstimateReason;

    List<WiFiItem> databaseAllData = null;
    StringBuilder estimateReason = new StringBuilder();

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getDatabaseAllData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        context = getActivity().getApplicationContext();
        wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifi_receiver, filter);

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_estimate, container, false);
        buttonUpdateAllDatabase = rootView.findViewById(R.id.buttonUpdateAllDatabase);
        buttonEstimate = rootView.findViewById(R.id.buttonEstimate);
        textResultEstimateWiFi2G = rootView.findViewById(R.id.textResultEstimateWiFi2G);
        textResultEstimateWiFi5G = rootView.findViewById(R.id.textResultEstimateWiFi5G);
        textEstimateReason = rootView.findViewById(R.id.textEstimateReason);
        textEstimateReason.setMovementMethod(new ScrollingMovementMethod());

        buttonUpdateAllDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDatabaseAllData();
            }
        });

        buttonEstimate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (databaseAllData == null) {
                    Toast.makeText(context, "You should get database first.", Toast.LENGTH_SHORT).show();
                    return;
                }

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

    private void getDatabaseAllData() {
        // DB 전체 다 받아오기
        RetrofitAPI retrofit_api = RetrofitClient.getRetrofitAPI();
        // TODO: SSID 설정
        retrofit_api.getData(MainActivity.building, MainActivity.ssid, null, null, null, null).enqueue(new Callback<List<WiFiItem>>() {
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
    }

    private void scanSuccess() {
        // 스캔 리스트를 받아오는 과정
        List<ScanResult> results = wm.getScanResults();
        Toast.makeText(context, "Scan finished.", Toast.LENGTH_SHORT).show();

        List<WiFiItem> userData = new ArrayList<>();
        for (ScanResult result : results) {
            userData.add(new WiFiItem(0, 0, result.SSID, result.BSSID, result.level, result.frequency, null, "Library5F"));
        }

        String targetBuilding = "Library5F";
        String targetSSID = "SKKU";

        estimateReason.setLength(0);
        estimateReason.append(targetBuilding + ", " + targetSSID + "\n\n");

        estimateReason.append("WiFi 2Ghz\n");
        double[] estimatedPositionWiFi2G = PositioningAlgorithm.run(userData, databaseAllData, targetBuilding, targetSSID, 2, estimateReason);
        estimateReason.append("\nWiFi 5Ghz\n");
        double[] estimatedPositionWiFi5G = PositioningAlgorithm.run(userData, databaseAllData, targetBuilding, targetSSID, 5, estimateReason);

        if (estimatedPositionWiFi2G != null) {
            textResultEstimateWiFi2G.setText(String.format("(%s, %s)", String.format("%.6f", estimatedPositionWiFi2G[0]), String.format("%.6f", estimatedPositionWiFi2G[1])));
        } else {
            textResultEstimateWiFi2G.setText("Out of Service");
        }
        if (estimatedPositionWiFi5G != null) {
            textResultEstimateWiFi5G.setText(String.format("(%s, %s)", String.format("%.6f", estimatedPositionWiFi5G[0]), String.format("%.6f", estimatedPositionWiFi5G[1])));
        } else {
            textResultEstimateWiFi5G.setText("Out of Service");
        }

        textEstimateReason.setText(estimateReason);
        Toast.makeText(context, "Estimation finished.", Toast.LENGTH_SHORT).show();
    }

    private void scanFailure() {
        Toast.makeText(context, "Scan failed.", Toast.LENGTH_SHORT).show();
        wm.getScanResults();
    }
}