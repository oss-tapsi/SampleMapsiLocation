package com.example.samplemapsilocation;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Map;

import ir.mapsi.common.data.remote.LegacyNetworkProviderAdapter;
import ir.mapsi.common.data.remote.model.HttpRequestMethod;
import ir.mapsi.common.data.remote.model.ResponseData;
import ir.mapsi.common.data.remote.model.ServiceConfig;
import ir.mapsi.common.data.remote.model.UrlConfig;
import ir.mapsi.common.presentation.MapsiLocation;
import ir.mapsi.common.presentation.listener.MapsiLocationListener;
import ir.mapsi.common.presentation.model.ApplicationInfo;
import ir.mapsi.common.presentation.model.ApplicationInitializer;
import ir.mapsi.common.presentation.model.MapsiLocationConfig;
import ir.mapsi.common.presentation.model.MapsiNetworkConfig;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "Location SDK TAG";
    private final MapsiLocation mapsiLocation = new MapsiLocation();
    private final MapsiLocationListener mapsiLocationListenerForSingleLocation = location -> Log.d(TAG, "single location: -> " + location);
    private MapsiLocationListener mapsiLocationListenerForIntervalLocation = null;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    private TextView locationPermissionTextView;
    private TextView locationGpsEnableTextView;
    private TextView locationSdkStatusTextView;
    private Button locationPermissionButton;
    private Button gpsLocationTurnOnButton;

    private final BroadcastReceiver locationProviderReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
                        updateLocationGpsEnableStatusViewData();
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        startMapsiLocation();
    }

    private void initViews() {
        locationPermissionTextView = findViewById(R.id.txtLocationPermission);
        locationPermissionButton = findViewById(R.id.btnGetLocationPermission);
        locationGpsEnableTextView = findViewById(R.id.txtLocationGpsEnable);
        locationSdkStatusTextView = findViewById(R.id.txtLocationSdkStatus);
        gpsLocationTurnOnButton = findViewById(R.id.btnGpsLocationTurnOn);
        handleClickGetSingleLocationButton();
        handleClickStartGetIntervalLocationButton();
        handleClickStopGetIntervalLocationButton();
        handleClickGetLocationPermissionButton();
        handleClickGpsLocationTurnOnButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(
                LocationManager.PROVIDERS_CHANGED_ACTION
        );

        registerReceiver(locationProviderReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLocationPermissionViewData();
        updateLocationGpsEnableStatusViewData();
    }

    private void updateLocationGpsEnableStatusViewData() {
        boolean isGpsEnable = isGpsEnabled(this);
        if (isGpsEnable) {
            locationGpsEnableTextView.setText("GPS is On");
            gpsLocationTurnOnButton.setVisibility(TextView.GONE);
        } else {
            locationGpsEnableTextView.setText("GPS is Off");
            gpsLocationTurnOnButton.setVisibility(TextView.VISIBLE);
        }
        updateLocationSdkStatus();
    }

    private void updateLocationPermissionViewData() {
        boolean isPermitted = hasLocationPermission(this);
        if (isPermitted) {
            locationPermissionTextView.setText("Location Permission Granted");
            locationPermissionButton.setVisibility(TextView.GONE);
        } else {
            locationPermissionTextView.setText("Location Permission Not Granted");
            locationPermissionButton.setVisibility(TextView.VISIBLE);
        }
        updateLocationSdkStatus();
    }

    private void updateLocationSdkStatus() {
        boolean canUseLocationSdk = isGpsEnabled(this) && hasLocationPermission(this);
        if (canUseLocationSdk) {
            locationSdkStatusTextView.setText("Now Mapsi Location should provide location");
        } else {
            locationSdkStatusTextView.setText("Mapsi Location can should provide location");
        }
    }

    private void handleClickGetSingleLocationButton() {
        Button button = findViewById(R.id.btnGetSingleLocation);
        button.setOnClickListener(v -> {
            mapsiLocation.getLocation(2000, mapsiLocationListenerForSingleLocation);
        });
    }

    private void handleClickGetLocationPermissionButton() {
        locationPermissionButton.setOnClickListener(v -> {
            getLocationPermission();
        });
    }

    private void handleClickGpsLocationTurnOnButton() {
        gpsLocationTurnOnButton.setOnClickListener(v -> {
            LocationHelper.requestEnableLocation(this);
        });
    }

    private void handleClickStartGetIntervalLocationButton() {
        Button button = findViewById(R.id.btnStartGetIntervallyLocation);
        button.setOnClickListener(v -> {
            removeGetIntervalLocationListener();
            mapsiLocationListenerForIntervalLocation = location -> Log.d(TAG, "interval location: -> " + location);
            mapsiLocation.getLiveLocation(2000, mapsiLocationListenerForIntervalLocation);
        });
    }

    private void handleClickStopGetIntervalLocationButton() {
        Button button = findViewById(R.id.btnStopGetIntervallyLocation);
        button.setOnClickListener(v -> {
            removeGetIntervalLocationListener();
        });
    }

    private void removeGetIntervalLocationListener() {
        if (mapsiLocationListenerForIntervalLocation != null) {
            mapsiLocation.removeLiveLocationListener(mapsiLocationListenerForIntervalLocation);
            mapsiLocationListenerForIntervalLocation = null;
        }
    }

    private void startMapsiLocation() {
        ApplicationInitializer applicationInitializer = new ApplicationInitializer(this.getApplication());
        startMapsiLocationWithNormalMode(applicationInitializer);
//        startMapsiLocationWithDenoisedDefault(applicationInitializer);
//        startMapsiLocationWithDenoisedCustom(applicationInitializer);
    }

    private void startMapsiLocationWithNormalMode(ApplicationInitializer applicationInitializer) {
        MapsiLocationConfig mapsiLocationConfig = new MapsiLocationConfig.Raw(applicationInitializer, new ApplicationInfo("test application"));
        mapsiLocation.start(mapsiLocationConfig);
    }

    private void startMapsiLocationWithDenoisedDefault(ApplicationInitializer applicationInitializer) {
        String API_KEY = "";
        MapsiLocationConfig mapsiLocationConfig = new MapsiLocationConfig.Denoised.Default(
                API_KEY,
                new ServiceConfig.Full(
                        new UrlConfig(
                                "https://map.tapsi.ir/api/client/v1/auth/token",
                                HttpRequestMethod.Post.INSTANCE
                        ),
                        new UrlConfig(
                                "https://map.tapsi.ir/api/client/v1/location/geolocate/",
                                HttpRequestMethod.Post.INSTANCE
                        )
                ),
                MapsiNetworkConfig.Companion.getDefault(),
                applicationInitializer,
                new ApplicationInfo("test application")
        );
        mapsiLocation.start(mapsiLocationConfig);
    }

    private void startMapsiLocationWithDenoisedCustom(ApplicationInitializer applicationInitializer) {
        MapsiLocationConfig mapsiLocationConfig = new MapsiLocationConfig.Denoised.Custom(
                new ServiceConfig.Derivative(
                        new UrlConfig(
                                "https://map.tapsi.ir/api/client/v1/location/geolocate/",
                                HttpRequestMethod.Post.INSTANCE
                        )
                ),
                new LegacyNetworkProviderAdapter(
                        (requestData, networkCallback) -> {
                            // You can call api call with retrofit or other ways for communication with your application server for providing location
                            // I only create fake response for test
                            ResponseData responseData = getFakeResponseData();
                            networkCallback.onSuccess(responseData);

                            // You can send error when failure occurred
//                                networkCallback.onError(new Exception("Fake Exception"));
                        }
                ),
                MapsiNetworkConfig.Companion.getDefault(),
                applicationInitializer,
                new ApplicationInfo("test application")
        );
        mapsiLocation.start(mapsiLocationConfig);
    }


    private ResponseData getFakeResponseData() {
        String body = "{\n" +
                "  \"location\": {\n" +
                "    \"latitude\": 35.7448,\n" +
                "    \"longitude\": 51.3753,\n" +
                "    \"altitude\": 435.0\n" +
                "  },\n" +
                "  \"timestamp\": 1756630000000,\n" +
                "  \"accuracy\": 5.0,\n" +
                "  \"provider\": \"LOCATION_PROVIDER_FUSED\",\n" +
                "  \"speed\": 0.0,\n" +
                "  \"bearing\": 0.0,\n" +
                "  \"isMocked\": true\n" +
                "}";
        return new ResponseData(
                200,
                Map.of(),
                body
        );
    }

    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isGpsEnabled(Context context) {
        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            updateLocationPermissionViewData();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LocationHelper.REQUEST_CHECK_SETTINGS) {
            updateLocationGpsEnableStatusViewData();
        }
    }

    private void getLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(locationProviderReceiver);
    }
}