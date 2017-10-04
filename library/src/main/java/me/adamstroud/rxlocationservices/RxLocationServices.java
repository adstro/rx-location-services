package me.adamstroud.rxlocationservices;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;

import io.reactivex.Observable;
import io.reactivex.Single;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * TODO
 *
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class RxLocationServices {
    public static Single<FusedLocationProviderClient> getFusedLocationProviderClient(@NonNull Context context) {
        return Single.just(LocationServices.getFusedLocationProviderClient(context));
    }

    public static Single<FusedLocationProviderClient> getFusedLocationProviderClient(@NonNull Activity activity) {
        return Single.just(LocationServices.getFusedLocationProviderClient(activity));
    }

    public static Single<SettingsClient> getSettingsClient(@NonNull Context context) {
        return Single.just(LocationServices.getSettingsClient(context));
    }

    public static Single<SettingsClient> getSettingsClient(@NonNull Activity activity) {
        return Single.just(LocationServices.getSettingsClient(activity));
    }

    public static Single<LocationSettingsResponse> checkLocationSettings(@NonNull SettingsClient settingsClient,
                                                                         @NonNull LocationSettingsRequest request) {
        return Single.create(emitter -> {
            settingsClient
                    .checkLocationSettings(request)
                    .addOnFailureListener(emitter::onError)
                    .addOnSuccessListener(emitter::onSuccess);
        });
    }

    @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
    public static Observable<Location> getLocations(@NonNull FusedLocationProviderClient fusedLocationProviderClient,
                                                    @NonNull LocationRequest locationRequest) {
        return Observable.create(emitter -> {
            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    emitter.onNext(locationResult.getLastLocation());
                }
            };

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

            emitter.setCancellable(() -> fusedLocationProviderClient.removeLocationUpdates(locationCallback));
        });
    }
}
