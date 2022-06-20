package wifilocation.wifi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    ScanFragment scan_fragment;
    SearchFragment search_fragment;
    EstimateFragment estimate_fragment;
    int now_fragment = 1; // 1은 scan, 2는 search

    String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermission();
        initView();
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
                scan_fragment.setBuilding("skku");
                search_fragment.setBuilding("skku");
                break;
            case R.id.map_wifilocation:
                scan_fragment.setBuilding("wifilocation");
                search_fragment.setBuilding("wifilocation");
                break;
            default:
                break;
        }
        if(now_fragment == 1) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, scan_fragment).commit();
        } else if(now_fragment == 2) {
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

        getSupportFragmentManager().beginTransaction().replace(R.id.container, scan_fragment).commit();
        BottomNavigationView bottom_navigation = findViewById(R.id.bottomNavigation);
        bottom_navigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.tab_scan:
                        now_fragment = 1;
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, scan_fragment).commit();
                        return true;
                    case R.id.tab_search:
                        now_fragment = 2;
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, search_fragment).commit();
                        return true;
                    case R.id.tab_estimate:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, estimate_fragment).commit();
                        return true;
                }
                return false;
            }
        });

    }
}