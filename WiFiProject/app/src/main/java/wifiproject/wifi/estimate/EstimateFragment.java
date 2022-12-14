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
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import wifilocation.wifi.customviews.SpotImageView;
import wifilocation.wifi.database.DatabaseHelper;
import wifilocation.wifi.model.WiFiItem;

public class EstimateFragment extends Fragment {
    Context context;
    WifiManager wm;
    BluetoothManager bm;
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    ScanSettings bluetoothLeScanSettings;
    ScanCallback bluetoothLeScanCallback;
    BeaconManager beaconManager;
    RangeNotifier rangeNotifier;
    Region beaconRegion;

    Button buttonLoadAllDatabase;
    Button buttonEstimate;
    Button buttonPushEstimationResult;
    TextView editTextRealX;
    TextView editTextRealY;
    TextView textResultEstimateWiFi2G;
    TextView textResultEstimateWiFi5G;
    TextView textResultEstimateBeacon;
    TextView textResultEstimateBLE;
    TextView textEstimateReason;

    SpotImageView imageview_map3;
    List<WiFiItem> databaseAllWiFiData = null;
    List<WiFiItem> databaseAllBleData = null;
    List<WiFiItem> scannedItems;
    EstimatedResult estimatedResultWiFi2G;
    EstimatedResult estimatedResultWiFi5G;
    EstimatedResult estimatedResultBLE;
    EstimatedResult estimatedResultBeacon;
    PositioningFilter filterWiFi2G;
    PositioningFilter filterWiFi5G;
    PositioningFilter filterBLE;
    PositioningFilter filterBeacon;
    List<EstimatedResult> estimatedResultAllWiFi2G;
    List<EstimatedResult> estimatedResultAllWiFi5G;
    List<EstimatedResult> estimatedResultAllBLE;
    List<EstimatedResult> estimatedResultAllBeacon;

    int resultCountThreshold = 4;
    int resultCount;
    boolean bleScanRequired = false;
    boolean beaconScanRequired = false;

    final static double standardRecordDistance = 8;

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
                        for (WiFiItem elem : userData) {
                            if (BSSID.equals(elem.getBSSID()) && elem.getMethod().equals("BLE")) {
                                alreadyExists = true;
                                break;
                            }
                        }
                        if (alreadyExists) {
                            continue;
                        }
                        userData.add(new WiFiItem(0, 0, SSID, BSSID, level, frequency, MainActivity.uuid, MainActivity.building, "BLE"));
                    }

                    bleScanRequired = false;
                    bluetoothLeScanner.stopScan(bluetoothLeScanCallback);

                    estimatedResultBLE = PositioningAlgorithm.run(userData, databaseAllBleData, MainActivity.building, MainActivity.bleName, MainActivity.uuid, "BLE", 2, standardRecordDistance);
                    handleEstimationResult(userData, 1);
                }
                catch (SecurityException e) {
                    estimatedResultBLE = null;

                    Toast.makeText(context, "???????????? ?????? ??????", Toast.LENGTH_SHORT).show();
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
                if (!beaconScanRequired || beacons.size() == 0) {
                    return;
                }

                List<WiFiItem> userData = new ArrayList<>();
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
                    for (WiFiItem elem : userData) {
                        if (BSSID.equals(elem.getBSSID()) && elem.getMethod().equals("iBeacon")) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    if (alreadyExists) {
                        continue;
                    }

                    userData.add(new WiFiItem(0, 0, SSID, BSSID, level, distance, MainActivity.uuid, MainActivity.building, "iBeacon"));
                }

                estimatedResultBeacon = PositioningAlgorithm.run(userData, databaseAllBleData, MainActivity.building, MainActivity.bleName, MainActivity.uuid, "iBeacon", 2, standardRecordDistance);
                handleEstimationResult(userData, 1);

                beaconScanRequired = false;
                beaconManager.stopRangingBeacons(region);
            }
        };
        beaconRegion = new Region("iBeaconEstimate", null, null, null);

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_estimate, container, false);
        imageview_map3 = rootView.findViewById(R.id.imageViewMap3);
        switch (MainActivity.building) {
            case "Library5F":
                imageview_map3.setImage(ImageSource.resource(R.drawable.skku_library_5f));
                break;
            case "Library3F":
                imageview_map3.setImage(ImageSource.resource(R.drawable.skku_library_3f));
                break;
            case "WiFiLocation2F":
                imageview_map3.setImage(ImageSource.resource(R.drawable.wifilocation_gimpo_2f_ice_temp_mezzanine_bottom));
                break;
            case "WiFiLocation3FTop":
                imageview_map3.setImage(ImageSource.resource(R.drawable.wifilocation_gimpo_3f_room_temp_mezzanine_top));
                break;
            case "WiFiLocation3F":
                imageview_map3.setImage(ImageSource.resource(R.drawable.wifilocation_gimpo_3f_room_temp_mezzanine_bottom));
                break;
            default:
                break;
        }

        buttonLoadAllDatabase = rootView.findViewById(R.id.buttonLoadAllDatabase);
        buttonEstimate = rootView.findViewById(R.id.buttonEstimate);
        buttonPushEstimationResult = rootView.findViewById(R.id.buttonPushEstimationResult);

        editTextRealX = rootView.findViewById(R.id.editTextRealX);
        editTextRealY = rootView.findViewById(R.id.editTextRealY);
        textResultEstimateWiFi2G = rootView.findViewById(R.id.textResultEstimateWiFi2G);
        textResultEstimateWiFi5G = rootView.findViewById(R.id.textResultEstimateWiFi5G);
        textResultEstimateBLE = rootView.findViewById(R.id.textResultEstimateBLE);
        textResultEstimateBeacon = rootView.findViewById(R.id.textResultEstimateBeacon);
        textEstimateReason = rootView.findViewById(R.id.textEstimateReason);
        textEstimateReason.setMovementMethod(new ScrollingMovementMethod());

        buttonLoadAllDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocal();
            }
        });

        buttonEstimate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scannedItems = new ArrayList<>();
                resultCount = 0;

                if (databaseAllWiFiData == null || databaseAllBleData == null) {
                    Toast.makeText(context, "You should get database first.", Toast.LENGTH_SHORT).show();
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
                }
                catch (SecurityException e) {
                    Toast.makeText(context, "???????????? ?????? ??????", Toast.LENGTH_SHORT).show();
                }
                catch (InterruptedException e) {
                    Toast.makeText(context, "???????????? ????????????", Toast.LENGTH_SHORT).show();
                }

                try {
                    beaconScanRequired = true;
                    beaconManager.startRangingBeacons(beaconRegion);
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    resultCount += 1;
                    //Toast.makeText(context, "?????? ?????? ?????? ??????", Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(context, "Scan started.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonPushEstimationResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float x;
                float y;

                try {
                    x = Float.parseFloat(editTextRealX.getText().toString());
                    y = Float.parseFloat(editTextRealY.getText().toString());
                }
                catch (NumberFormatException e) {
                    x = -1;
                    y = -1;
                }

                for (WiFiItem item : scannedItems) {
                    if (item == null) {
                        continue;
                    }

                    try {
                        item.setX(x);
                        item.setY(y);
                        if (!item.getBuilding().endsWith("-Est")) {
                            item.setBuilding(item.getBuilding() + "-Est");
                        }
                    }
                    catch (NullPointerException e) {
                        continue;
                    }
                }

                List<EstimatedResult> estimatedDataSet = new ArrayList<>();
                if (estimatedResultAllWiFi2G != null) {
                    estimatedDataSet.addAll(estimatedResultAllWiFi2G);
                }
                if (estimatedResultAllWiFi5G != null) {
                    estimatedDataSet.addAll(estimatedResultAllWiFi5G);
                }
                if (estimatedResultBLE != null) {
                    estimatedDataSet.add(estimatedResultBLE);
                }
                if (estimatedResultBeacon != null) {
                    estimatedDataSet.add(estimatedResultBeacon);
                }

                for (EstimatedResult er : estimatedDataSet) {
                    if (er == null) {
                        continue;
                    }

                    try {
                        er.setPositionRealX(x);
                        er.setPositionRealY(y);
                    }
                    catch (NullPointerException e) {
                        continue;
                    }
                }

                pushLocal(scannedItems, estimatedDataSet);
            }
        });

        scannedItems = new ArrayList<>();

        filterWiFi2G = new PositioningFilter();
        filterWiFi5G = new PositioningFilter();
        filterBLE = new PositioningFilter();
        filterBeacon = new PositioningFilter();

        getLocal();

        return rootView;
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

    private void getLocal() {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        databaseAllWiFiData = dbHelper.searchFromWiFiInfo(MainActivity.building, MainActivity.ssid, null, null, null, null, null);
        databaseAllBleData = dbHelper.searchFromWiFiInfo(MainActivity.building, MainActivity.bleName, null, null, null, null, null);
        dbHelper.close();

        Toast.makeText(context, "Local database loaded.", Toast.LENGTH_SHORT).show();
    }

    private void pushLocal(List<WiFiItem> scannedItemsForPost, List<EstimatedResult> estimatedResultsForPost) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        dbHelper.insertIntoWiFiInfo(scannedItemsForPost, 1);
        dbHelper.insertIntoFingerprint(estimatedResultsForPost, 1);
        dbHelper.close();

        Toast.makeText(context, "Local database push", Toast.LENGTH_SHORT).show();
    }

    private void scanSuccess() {
        // ?????? ???????????? ???????????? ??????
        List<ScanResult> results = wm.getScanResults();

        List<WiFiItem> userData = new ArrayList<>();
        for (ScanResult result : results) {
            userData.add(new WiFiItem(0, 0, result.SSID, result.BSSID, result.level, result.frequency, MainActivity.uuid, MainActivity.building, "WiFi"));
        }

        estimatedResultWiFi2G = PositioningAlgorithm.run(userData, databaseAllWiFiData, MainActivity.building, MainActivity.ssid, MainActivity.uuid, "WiFi", 2, standardRecordDistance);
        estimatedResultWiFi2G = filterWiFi2G.run(estimatedResultWiFi2G, userData.get(userData.size() - 1).getDate().getTime());
        estimatedResultWiFi5G = PositioningAlgorithm.run(userData, databaseAllWiFiData, MainActivity.building, MainActivity.ssid, MainActivity.uuid, "WiFi", 5, standardRecordDistance);
        estimatedResultWiFi5G = filterWiFi5G.run(estimatedResultWiFi5G, userData.get(userData.size() - 1).getDate().getTime());

        int[] infoK = {3, 9, 2};
        int[] infoMinValidAPNum = {1, 2, 1};
        int[] infoMinDbm = {-65, -40, 5};
        estimatedResultAllWiFi2G = PositioningAlgorithm.runRange(userData, databaseAllWiFiData, MainActivity.building, MainActivity.ssid, MainActivity.uuid, "WiFi", 2, standardRecordDistance,
                                                                infoK, infoMinValidAPNum, infoMinDbm);
        estimatedResultAllWiFi5G = PositioningAlgorithm.runRange(userData, databaseAllWiFiData, MainActivity.building, MainActivity.ssid, MainActivity.uuid, "WiFi", 5, standardRecordDistance,
                                                                infoK, infoMinValidAPNum, infoMinDbm);

        handleEstimationResult(userData, 2);
    }

    private void scanFailure() {
        Toast.makeText(context, "Scan failed.", Toast.LENGTH_SHORT).show();

        wm.getScanResults();
    }

    private void handleEstimationResult(List<WiFiItem> userData, int plus) {
        scannedItems.addAll(userData);
        resultCount += plus;

        if (resultCount < resultCountThreshold) {
            return;
        }

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
        if (estimatedResultBeacon != null) {
            textResultEstimateBeacon.setText(String.format("(%s, %s)", String.format("%.2f", estimatedResultBeacon.getPositionEstimatedX()), String.format("%.2f", estimatedResultBeacon.getPositionEstimatedY())));
            result.add(new PointF((float)estimatedResultBeacon.getPositionEstimatedX(), (float)estimatedResultBeacon.getPositionEstimatedY()));
        } else {
            textResultEstimateBeacon.setText("Out of Service");
        }
        imageview_map3.setEstimateSpot(result);

        textEstimateReason.setText(MainActivity.uuid + "\n" + MainActivity.building + ", " + MainActivity.ssid + ", " + MainActivity.bleName + "\n");
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
        textEstimateReason.setText(textEstimateReason.getText() + "\niBeacon\n");
        if (estimatedResultBeacon != null) {
            textEstimateReason.setText(textEstimateReason.getText() + estimatedResultBeacon.getEstimateReason().toString());
        }

        Toast.makeText(context, "Estimation finished.", Toast.LENGTH_SHORT).show();
    }
}