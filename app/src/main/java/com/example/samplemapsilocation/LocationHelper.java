package com.example.samplemapsilocation;

import android.app.Activity;
import android.content.IntentSender;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

public class LocationHelper {

    static final int REQUEST_CHECK_SETTINGS = 1001;

    public static void requestEnableLocation(Activity activity) {

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10_000)
                .setFastestInterval(5_000);

        LocationSettingsRequest settingsRequest =
                new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest)
                        .setAlwaysShow(true)
                        .build();

        SettingsClient settingsClient =
                LocationServices.getSettingsClient(activity);

        settingsClient.checkLocationSettings(settingsRequest)
                .addOnSuccessListener(locationSettingsResponse -> {
                    // Location is already enabled
                })
                .addOnFailureListener(exception -> {

                    if (exception instanceof ResolvableApiException) {
                        try {
                            ResolvableApiException resolvable =
                                    (ResolvableApiException) exception;

                            resolvable.startResolutionForResult(
                                    activity,
                                    REQUEST_CHECK_SETTINGS
                            );

                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}