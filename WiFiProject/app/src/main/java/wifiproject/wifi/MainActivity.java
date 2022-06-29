package wifilocation.wifi;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.tabs.TabLayout;

import java.util.UUID;

import wifilocation.wifi.estimate.EstimateFragment;
import wifilocation.wifi.scan.ScanFragment;
import wifilocation.wifi.search.SearchFragment;

public class MainActivity extends AppCompatActivity {
    public ScanFragment scan_fragment;
    public SearchFragment search_fragment;
    public EstimateFragment estimate_fragment;
    public int now_fragment = 1; // 1은 scan, 2는 search

    public static String building = "Library5F";
    public static String ssid = "SKKU";
    public static String bleName = "";
    public static String uuid;

    String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermission();
        initView();

        uuid = getDevicesUUID(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_top, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        scan_fragment = new ScanFragment();
        search_fragment = new SearchFragment();

        int cur_id = item.getItemId();
        switch (cur_id) {
            case R.id.map_skku:
                building = "Library5F";
                ssid = "SKKU";
                bleName = "";
                break;
            case R.id.map_wifilocation:
                building = "wifilocation";
                ssid = "WiFiLocation@PDA";
                bleName = "";
                break;
            default:
                break;
        }
        if (now_fragment == 1) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, scan_fragment).commit();
        } else if (now_fragment == 2) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, search_fragment).commit();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getPermission() {
        // 권한 체크하고 권한 요청
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, 1000);
        }
    }

    private void initView() {
        scan_fragment = new ScanFragment();
        search_fragment = new SearchFragment();
        estimate_fragment = new EstimateFragment();

        now_fragment = 1;
        getSupportFragmentManager().beginTransaction().replace(R.id.container, scan_fragment).commit();

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("스캔"));
        tabs.addTab(tabs.newTab().setText("검색"));
        tabs.addTab(tabs.newTab().setText("측정"));

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    now_fragment = 1;
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, scan_fragment).commit();
                } else if (position == 1) {
                    now_fragment = 2;
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, search_fragment).commit();
                } else if (position == 2) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, estimate_fragment).commit();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                now_fragment = 1;
                getSupportFragmentManager().beginTransaction().replace(R.id.container, scan_fragment).commit();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private String getDevicesUUID(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        return deviceId;
    }
}