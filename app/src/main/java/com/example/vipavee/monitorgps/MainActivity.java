package com.example.vipavee.monitorgps;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    LocationListener _networkLocationListener;
    LocationListener _passiveLocationListener;

    NetworkProviderStatusReceiver _statusReceiver;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public void onAccurateProvider(MenuItem item) {
        Criteria criteria = new Criteria();

        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedRequired(true);
        criteria.setAltitudeRequired(true);

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        List<String> matchingProviderNames = lm.getProviders(criteria, false);
        for (String providerName : matchingProviderNames) {
            LocationProvider provider = lm.getProvider(providerName);
            String logMessage = LogHelper.formationLocationProvider(this, provider);
            Log.d(LogHelper.LOGTAG, "Monitor Location Provider:" + logMessage);
        }
    }

    public void onLowPowerProvider(MenuItem item) {
        Criteria criteria = new Criteria();

        criteria.setPowerRequirement(Criteria.POWER_LOW);

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        List<String> matchingProviderNames = lm.getProviders(criteria, false);
        for (String providerName : matchingProviderNames) {
            LocationProvider provider = lm.getProvider(providerName);
            String logMessage = LogHelper.formationLocationProvider(this, provider);
            Log.d(LogHelper.LOGTAG, "Monitor Location Provider:" + logMessage);
        }
    }

    public void onStartNetworkListener(MenuItem item) {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (confirmNetworkProviderAvailable(lm)) {
            _statusReceiver = new NetworkProviderStatusReceiver();
            _statusReceiver.start(this);

            _networkLocationListener = new MyLocationListener();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, _networkLocationListener);
        }
    }

    public void onStartPassiveListener(MenuItem item) {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        _passiveLocationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, _passiveLocationListener);
    }

    public void onExit(MenuItem item) {
        stopLocationListener();
        finish();
    }

    private void stopLocationListener() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (_networkLocationListener != null) {
            lm.removeUpdates(_networkLocationListener);
            _networkLocationListener = null;
        }

        if (_statusReceiver != null) {
            _statusReceiver.stop(this);
            _statusReceiver = null;
        }

    }

    boolean confirmNetworkProviderAvailable(LocationManager lm) {
        return confirmAirplaneModeOff() &&
                confirmNetworkProviderEnabled(lm) &&
                confirmWiFiAvailable();
    }

    boolean confirmNetworkProviderEnabled(LocationManager lm) {

        boolean isAvailable = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(!isAvailable) {
            AlertUserDialog dialog = new AlertUserDialog("Please Enable Location Services",
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS) {
                @Override
                public void cancel() {

                }
            };
            dialog.show(getFragmentManager(), null);
        }

        return isAvailable;
    }

    boolean confirmAirplaneModeOff() {
        boolean isOff =
                Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 0;

        if(!isOff) {
            AlertUserDialog dialog = new AlertUserDialog("Please Disable Airplane Mode",
                    Settings.ACTION_AIRPLANE_MODE_SETTINGS) {
                @Override
                public void cancel() {

                }
            };
            dialog.show(getFragmentManager(), null);
        }

        return isOff;
    }

    boolean confirmWiFiAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isAvailable = wifiInfo.isAvailable();

        if(!isAvailable) {
            AlertUserDialog dialog = new AlertUserDialog("Pleasπe Turn On Wi-Fi",
                    Settings.ACTION_WIFI_SETTINGS) {
                @Override
                public void cancel() {

                }
            };
            dialog.show(getFragmentManager(), null);
        }

        return isAvailable;
    }

}
