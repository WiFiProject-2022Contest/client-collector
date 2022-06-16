package wifilocation.wifi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

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
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, scan_fragment).commit();
                        return true;
                    case R.id.tab_search:
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