package wifilocation.wifi.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.davemorrissey.labs.subscaleview.ImageSource;

import java.util.ArrayList;
import java.util.List;

import wifilocation.wifi.MainActivity;
import wifilocation.wifi.model.PushResultModel;
import wifilocation.wifi.R;
import wifilocation.wifi.model.WiFiItem;
import wifilocation.wifi.model.WiFiItemAdapter;
import wifilocation.wifi.customviews.ScalableSpotImageView;
import wifilocation.wifi.database.DatabaseHelper;
import wifilocation.wifi.serverconnection.RetrofitAPI;
import wifilocation.wifi.serverconnection.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanFragment extends Fragment {
    RecyclerView recyclerview_scanned;
    WiFiItemAdapter wifiitem_adpater = new WiFiItemAdapter();
    ScalableSpotImageView imageview_map;
    WifiManager wm;
    BluetoothManager bm;
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    ScanSettings bluetoothLeScanSettings;
    ScanCallback bluetoothLeScanCallback;
    Context context;
    EditText edittext_x, edittext_y;

    ArrayList<WiFiItem> items;
    boolean bleScanRequired = false;

    private BroadcastReceiver wifi_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Build.VERSION.SDK_INT <= 22) {
                scanSuccess();
            } else {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    scanFailure();
                }
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
        switch (MainActivity.building) {
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

                float target_x, target_y;
                try {
                    target_x = Float.parseFloat(edittext_x.getText().toString());
                    target_y = Float.parseFloat(edittext_y.getText().toString());
                } catch (Exception e) {
                    return;
                }

                try {
                    for (android.bluetooth.le.ScanResult scanResult : results) {
                        String SSID = scanResult.getScanRecord().getDeviceName();
                        if (SSID == null) {
                            SSID = scanResult.getDevice().getName();
                        }
                        if (SSID == null) {
                            SSID = "";
                        }

                        String BSSID = scanResult.getDevice().getAddress();
                        int level = scanResult.getRssi();
                        int frequency = 0;

                        boolean alreadyExists = false;
                        for (WiFiItem elem : items) {
                            if (BSSID.equals(elem.getBSSID())) {
                                alreadyExists = true;
                                break;
                            }
                        }
                        if (alreadyExists) {
                            continue;
                        }
                        items.add(new WiFiItem(target_x, target_y, SSID, BSSID, level, frequency, MainActivity.uuid, MainActivity.building, "BLE"));
                    }

                    wifiitem_adpater.setItems(items);
                    recyclerview_scanned.setAdapter(wifiitem_adpater);

                    Toast.makeText(context, "BLE scan success", Toast.LENGTH_SHORT).show();

                    bleScanRequired = false;
                    bluetoothLeScanner.stopScan(bluetoothLeScanCallback);
                } catch (SecurityException e) {
                    Toast.makeText(context, "블루투스 권한 실패", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Toast.makeText(context, "BLE scan failed", Toast.LENGTH_SHORT).show();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifi_receiver, filter);
        Button button_scan = rootview.findViewById(R.id.buttonScan);
        button_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                items = new ArrayList<>();

                try {
                    Float.parseFloat(edittext_x.getText().toString());
                    Float.parseFloat(edittext_y.getText().toString());
                } catch (Exception e) {
                    Toast.makeText(context, "올바른 형식의 좌표 입력 필요", Toast.LENGTH_SHORT).show();
                    return;
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
                } catch (SecurityException e) {
                    Toast.makeText(context, "블루투스 권한 실패", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(context, "블루투스 스캔 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button button_push = rootview.findViewById(R.id.buttonPush);
        button_push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pushLocal();

                /* AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle("데이터베이스 선택");
                alertDialogBuilder.setCancelable(true);
                alertDialogBuilder.setPositiveButton("서버", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        pushRemote();
                    }
                });
                alertDialogBuilder.setNegativeButton("로컬", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        pushLocal();
                    }
                });
                alertDialogBuilder.setNeutralButton("모두", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        pushRemote();
                        pushLocal();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show(); */
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

        float target_x, target_y;
        try {
            target_x = Float.parseFloat(edittext_x.getText().toString());
            target_y = Float.parseFloat(edittext_y.getText().toString());
        } catch (Exception e) {
            return;
        }
        for (ScanResult result : results) {
//            if (!result.SSID.equalsIgnoreCase("WiFiLocation@PDA")) continue;
            items.add(new WiFiItem(target_x, target_y, result.SSID, result.BSSID, result.level, result.frequency, MainActivity.uuid, MainActivity.building, "WiFi"));
        }
        wifiitem_adpater.setItems(items);
        recyclerview_scanned.setAdapter(wifiitem_adpater);
    }

    private void scanFailure() {
        Toast.makeText(context, "WiFi Scan failed.", Toast.LENGTH_SHORT).show();
        wm.getScanResults();
    }

    private void pushRemote() {
        RetrofitAPI retrofit_api = RetrofitClient.getRetrofitAPI();

        retrofit_api.postDataWiFiItem(wifiitem_adpater.getItems()).enqueue(new Callback<PushResultModel>() {
            @Override
            public void onResponse(Call<PushResultModel> call, Response<PushResultModel> response) {
                PushResultModel r = response.body();
                if (r.getSuccess().equals("true")) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "서버에 PUSH 성공", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "서버에 PUSH 실패", Toast.LENGTH_SHORT).show();
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

    private void pushLocal() {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        dbHelper.insertIntoWiFiInfo(wifiitem_adpater.getItems(), 1);
        Toast.makeText(context, "push 성공!", Toast.LENGTH_SHORT).show();
    }
}