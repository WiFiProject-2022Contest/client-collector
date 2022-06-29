package wifilocation.wifi.estimate;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
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

import com.davemorrissey.labs.subscaleview.ImageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import wifilocation.wifi.MainActivity;
import wifilocation.wifi.R;
import wifilocation.wifi.customviews.SpotImageView;
import wifilocation.wifi.model.PushResultModel;
import wifilocation.wifi.model.WiFiItem;
import wifilocation.wifi.serverconnection.RetrofitAPI;
import wifilocation.wifi.serverconnection.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstimateFragment extends Fragment {
    Context context;
    WifiManager wm;
    BluetoothManager bm;
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    ScanSettings bluetoothLeScanSettings;
    ScanCallback bluetoothLeScanCallback;

    Button buttonUpdateAllDatabase;
    Button buttonEstimate;
    Button buttonPushEstimationResult;
    TextView editTextRealX;
    TextView editTextRealY;
    TextView textResultEstimateWiFi2G;
    TextView textResultEstimateWiFi5G;
    TextView textResultEstimateBLE;
    TextView textEstimateReason;

    SpotImageView imageview_map3;
    List<WiFiItem> databaseAllData = null;
    EstimatedResult estimatedResultWiFi2G;
    EstimatedResult estimatedResultWiFi5G;
    EstimatedResult estimatedResultBLE;

    CountDownLatch scanTaskCount;
    boolean bleScanRequired = false;

    private BroadcastReceiver wifi_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
            if (success) {
                scanSuccess();
            } else {
                scanFailure();
            }

            scanTaskCount.countDown();
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
        bm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bm.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(3000).build();

        bluetoothLeScanCallback = new ScanCallback() {
            /*
            @Override
            public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
                super.onScanResult(callbackType, result);
            }
             */

            @Override
            public void onBatchScanResults(List<android.bluetooth.le.ScanResult> results) {
                super.onBatchScanResults(results);

                if (!bleScanRequired) {
                    return;
                }
                try {
                    List<WiFiItem> userData = new ArrayList<>();

                    for (android.bluetooth.le.ScanResult scanResult : results) {
                        String SSID = scanResult.getScanRecord().getDeviceName();
                        String BSSID = scanResult.getDevice().getAddress();
                        int level = scanResult.getRssi();
                        int frequency = 0;

                        userData.add(new WiFiItem(0, 0, SSID, BSSID, level, frequency, MainActivity.uuid, MainActivity.building, "BLE"));
                    }

                    bleScanRequired = false;
                    bluetoothLeScanner.stopScan(bluetoothLeScanCallback);

                    estimatedResultBLE = PositioningAlgorithm.run(userData, databaseAllData, MainActivity.building, MainActivity.ssid, MainActivity.uuid, "BLE", 2);
                }
                catch (SecurityException e) {
                    estimatedResultBLE = null;

                    Toast.makeText(context, "블루투스 권한 실패", Toast.LENGTH_SHORT).show();
                    return;
                }
                finally {
                    scanTaskCount.countDown();
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Toast.makeText(context, "BLE scan failed", Toast.LENGTH_SHORT).show();

                scanTaskCount.countDown();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifi_receiver, filter);

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_estimate, container, false);
        imageview_map3 = rootView.findViewById(R.id.imageViewMap3);
        imageview_map3.setImage(ImageSource.resource(R.drawable.skku_example));

        buttonUpdateAllDatabase = rootView.findViewById(R.id.buttonUpdateAllDatabase);
        buttonEstimate = rootView.findViewById(R.id.buttonEstimate);
        buttonPushEstimationResult = rootView.findViewById(R.id.buttonPushEstimationResult);

        editTextRealX = rootView.findViewById(R.id.editTextRealX);
        editTextRealY = rootView.findViewById(R.id.editTextRealY);
        textResultEstimateWiFi2G = rootView.findViewById(R.id.textResultEstimateWiFi2G);
        textResultEstimateWiFi5G = rootView.findViewById(R.id.textResultEstimateWiFi5G);
        textResultEstimateBLE = rootView.findViewById(R.id.textResultEstimateBLE);
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
                if (scanTaskCount != null) {
                    // Release previous task
                    while (scanTaskCount.getCount() > 0) {
                        scanTaskCount.countDown();
                    }
                }
                scanTaskCount = new CountDownLatch(2);

                ScanResultTask scanResultTask = new ScanResultTask();
                scanResultTask.execute();
            }
        });

        buttonPushEstimationResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EstimatedResult copy2G = null;
                EstimatedResult copy5G = null;
                EstimatedResult copyBLE = null;

                if (estimatedResultWiFi2G != null) {
                    copy2G = new EstimatedResult(estimatedResultWiFi2G);
                }

                if (estimatedResultWiFi5G != null) {
                    copy5G = new EstimatedResult(estimatedResultWiFi5G);
                }

                if (estimatedResultBLE != null) {
                    copyBLE = new EstimatedResult(estimatedResultBLE);
                }

                for (EstimatedResult er : new EstimatedResult[] {copy2G, copy5G, copyBLE}) {
                    if (er == null) {
                        continue;
                    }

                    try {
                        double x = Double.parseDouble(editTextRealX.getText().toString());
                        double y = Double.parseDouble(editTextRealY.getText().toString());
                        er.setPositionRealX(x);
                        er.setPositionRealY(y);
                    }
                    catch (NumberFormatException e) {
                        er.setPositionRealX(-1);
                        er.setPositionRealY(-1);
                    }
                    catch (NullPointerException e) {
                        continue;
                    }

                    RetrofitAPI retrofit_api = RetrofitClient.getRetrofitAPI();
                    retrofit_api.postData(er).enqueue(new Callback<PushResultModel>() {
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

        List<WiFiItem> userData = new ArrayList<>();
        for (ScanResult result : results) {
            userData.add(new WiFiItem(0, 0, result.SSID, result.BSSID, result.level, result.frequency, MainActivity.uuid, MainActivity.building, "WiFi"));
        }

        estimatedResultWiFi2G = PositioningAlgorithm.run(userData, databaseAllData, MainActivity.building, MainActivity.ssid, MainActivity.uuid, "WiFi", 2);
        estimatedResultWiFi5G = PositioningAlgorithm.run(userData, databaseAllData, MainActivity.building, MainActivity.ssid, MainActivity.uuid, "WiFi", 5);
    }

    private void scanFailure() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, "Scan failed.", Toast.LENGTH_SHORT).show();
            }
        });

        wm.getScanResults();
    }

    private class ScanResultTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (databaseAllData == null) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, "You should get database first.", Toast.LENGTH_SHORT).show();
                    }
                });
                return null;
            }

            wm.startScan();

            try {
                bluetoothAdapter.enable();
                while (!bluetoothAdapter.isEnabled()) {
                    Thread.sleep(100);
                }

                bleScanRequired = true;
                //bluetoothLeScanner.flushPendingScanResults(bluetoothLeScanCallback);
                bluetoothLeScanner.startScan(new ArrayList<ScanFilter>(), bluetoothLeScanSettings, bluetoothLeScanCallback);
            }
            catch (SecurityException e) {
                Toast.makeText(context, "블루투스 권한 실패", Toast.LENGTH_SHORT).show();
                scanTaskCount.countDown();
            }
            catch (Exception e) {
                Toast.makeText(context, "블루투스 실패", Toast.LENGTH_SHORT).show();
                scanTaskCount.countDown();
            }

            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, "Scan started.", Toast.LENGTH_SHORT).show();
                }
            });

            try {
                scanTaskCount.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }

            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    ArrayList<PointF> result = new ArrayList<PointF>();
                    if (estimatedResultWiFi2G != null) {
                        textResultEstimateWiFi2G.setText(String.format("(%s, %s)", String.format("%.2f", estimatedResultWiFi2G.getPositionEstimatedX()), String.format("%.2f", estimatedResultWiFi2G.getPositionEstimatedY())));
                        result.add(new PointF((float)estimatedResultWiFi2G.getPositionEstimatedX(), (float)estimatedResultWiFi2G.getPositionEstimatedY()));
                    } else {
                        textResultEstimateWiFi2G.setText("Out of Service");
                    }
                    if (estimatedResultWiFi5G != null) {
                        textResultEstimateWiFi5G.setText(String.format("(%s, %s)", String.format("%.2f", estimatedResultWiFi5G.getPositionEstimatedX()), String.format("%.2f", estimatedResultWiFi5G.getPositionEstimatedY())));
                        result.add(new PointF((float)estimatedResultWiFi5G.getPositionEstimatedX(), (float)estimatedResultWiFi5G.getPositionEstimatedY()));
                    } else {
                        textResultEstimateWiFi5G.setText("Out of Service");
                    }
                    if (estimatedResultBLE != null) {
                        textResultEstimateBLE.setText(String.format("(%s, %s)", String.format("%.2f", estimatedResultBLE.getPositionEstimatedX()), String.format("%.2f", estimatedResultBLE.getPositionEstimatedY())));
                        result.add(new PointF((float)estimatedResultBLE.getPositionEstimatedX(), (float)estimatedResultBLE.getPositionEstimatedY()));
                    } else {
                        textResultEstimateBLE.setText("Out of Service");
                    }
                    imageview_map3.setEstimateSpot(result);

                    textEstimateReason.setText(MainActivity.uuid + "\n" + MainActivity.building + ", " + MainActivity.ssid + "\n");
                    textEstimateReason.setText(textEstimateReason.getText() + "\nWiFi 2Ghz\n");
                    if (estimatedResultWiFi2G != null) {
                        textEstimateReason.setText(textEstimateReason.getText() + estimatedResultWiFi2G.getEstimateReason().toString());
                    }
                    textEstimateReason.setText(textEstimateReason.getText() + "\nWiFi 5Ghz\n");
                    if (estimatedResultWiFi5G != null) {
                        textEstimateReason.setText(textEstimateReason.getText() + estimatedResultWiFi5G.getEstimateReason().toString());
                    }
                    textEstimateReason.setText(textEstimateReason.getText() + "\nBLE\n");
                    if (estimatedResultBLE != null) {
                        textEstimateReason.setText(textEstimateReason.getText() + estimatedResultBLE.getEstimateReason().toString());
                    }

                    Toast.makeText(context, "Estimation finished.", Toast.LENGTH_SHORT).show();
                }
            });

            return null;
        }
    }
}