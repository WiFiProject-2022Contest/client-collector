package wifilocation.wifi.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
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

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.davemorrissey.labs.subscaleview.ImageSource;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import wifilocation.wifi.MainActivity;
import wifilocation.wifi.R;
import wifilocation.wifi.model.WiFiItem;
import wifilocation.wifi.model.WiFiItemAdapter;
import wifilocation.wifi.customviews.ScalableSpotImageView;
import wifilocation.wifi.database.DatabaseHelper;


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
    BeaconManager beaconManager;
    RangeNotifier rangeNotifier;
    Region beaconRegion;
    Context context;
    EditText edittext_x, edittext_y;

    ArrayList<WiFiItem> items;
    boolean bleScanRequired = false;
    boolean beaconScanRequired = false;

    private BroadcastReceiver wifi_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Build.VERSION.SDK_INT <= 22) {
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
            case "WiFiLocation3F":
                imageview_map.setImage(ImageSource.resource(R.drawable.wifilocation_gimpo_3f_room_temp_mezzanine_bottom));
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
                        SSID = SSID.replaceAll("[^\\u0020-\\u007e]", "");

                        String BSSID = scanResult.getDevice().getAddress();
                        int level = scanResult.getRssi();
                        int frequency = 0;

                        boolean alreadyExists = false;
                        for (WiFiItem elem : items) {
                            if (BSSID.equals(elem.getBSSID()) && elem.getMethod().equals("BLE")) {
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

        beaconManager = BeaconManager.getInstanceForApplication(context);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (!beaconScanRequired) {
                    return;
                }

                if (beacons.size() > 0) {
                    float target_x, target_y;
                    try {
                        target_x = Float.parseFloat(edittext_x.getText().toString());
                        target_y = Float.parseFloat(edittext_y.getText().toString());
                    } catch (Exception e) {
                        return;
                    }

                    for (Beacon beacon : beacons) {
                        String SSID = beacon.getBluetoothName();
                        if (SSID == null) {
                            SSID = "";
                        }
                        SSID = SSID.replaceAll("[^\\u0020-\\u007e]", "");

                        String BSSID = beacon.getBluetoothAddress();
                        int level = beacon.getRssi();
                        int distance = (int) Math.round(beacon.getDistance() * 1000);

                        boolean alreadyExists = false;
                        for (WiFiItem elem : items) {
                            if (BSSID.equals(elem.getBSSID()) && elem.getMethod().equals("iBeacon")) {
                                alreadyExists = true;
                                break;
                            }
                        }
                        if (alreadyExists) {
                            continue;
                        }

                        items.add(new WiFiItem(target_x, target_y, SSID, BSSID, level, distance, MainActivity.uuid, MainActivity.building, "iBeacon"));
                    }

                    wifiitem_adpater.setItems(items);
                    recyclerview_scanned.setAdapter(wifiitem_adpater);
                }
                beaconScanRequired = false;
                beaconManager.stopRangingBeacons(region);
            }
        };
        beaconRegion = new Region("iBeaconScan", null, null, null);

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
                    //.flushPendingScanResults(bluetoothLeScanCallback);
                    bluetoothLeScanner.startScan(new ArrayList<ScanFilter>(), bluetoothLeScanSettings, bluetoothLeScanCallback);

                    beaconScanRequired = true;
                    beaconManager.startRangingBeacons(beaconRegion);
                } catch (SecurityException e) {
                    Toast.makeText(context, "블루투스 권한 실패", Toast.LENGTH_SHORT).show();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {

                }
            }
        });

        Button button_push = rootview.findViewById(R.id.buttonPush);
        button_push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pushLocal();
            }
        });

        return rootview;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifi_receiver, filter);
        beaconManager.addRangeNotifier(rangeNotifier);
    }

    @Override
    public void onPause() {
        super.onPause();

        context.unregisterReceiver(wifi_receiver);
        try {
            //bluetoothLeScanner.flushPendingScanResults(bluetoothLeScanCallback);
            bluetoothLeScanner.stopScan(bluetoothLeScanCallback);
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
        beaconManager.stopRangingBeacons(beaconRegion);
        beaconManager.removeRangeNotifier(rangeNotifier);
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

    private void pushLocal() {
        DBInsert task = new DBInsert();
        task.execute();
    }

    private class DBInsert extends AsyncTask {
        DatabaseHelper dbHelper;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dbHelper = new DatabaseHelper(context);
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Toast.makeText(context, "push 성공!", Toast.LENGTH_SHORT).show();
            dbHelper.close();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            dbHelper.insertIntoWiFiInfo(wifiitem_adpater.getItems(), 1);
            return null;
        }
    }
}