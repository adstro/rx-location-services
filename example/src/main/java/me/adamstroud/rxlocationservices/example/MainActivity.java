package me.adamstroud.rxlocationservices.example;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import io.reactivex.disposables.CompositeDisposable;
import me.adamstroud.rxlocationservices.RxLocationServices;
import me.adamstroud.rxlocationservices.example.databinding.ActivityMainBinding;
import timber.log.Timber;

/**
 * TODO
 *
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ID_LOCATION_PERMISSION = 1;
    private static final int REQUEST_ID_CHECK_SETTINGS = 2;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ID_LOCATION_PERMISSION);
        } else {
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(5000)
                    .setFastestInterval(5000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            compositeDisposable.add(RxLocationServices.getSettingsClient(this)
                    .flatMap(settingsClient -> RxLocationServices.checkLocationSettings(settingsClient, new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()))
                    .flatMap(locationSettingsResponse -> {
                        LocationSettingsStates states = locationSettingsResponse.getLocationSettingsStates();

                        Timber.d("bleUsable = %b, gpsUsable = %b, networkLocationUsable = %b", states.isBleUsable(), states.isGpsUsable(), states.isNetworkLocationUsable());
                        return RxLocationServices.getFusedLocationProviderClient(MainActivity.this);
                    })
                    .flatMapObservable(fusedLocationProviderClient -> RxLocationServices.getLocations(fusedLocationProviderClient, locationRequest))
                    .subscribe(location -> {
                                Timber.d("Received location from -> %s ", location);
                                binding.location.setText(location.toString());
                            },
                            throwable -> {
                                Timber.e(throwable, "Could not get location");

                                int statusCode = ((ApiException) throwable).getStatusCode();
                                switch (statusCode) {
                                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                                        // Location settings are not satisfied, but this can be fixed
                                        // by showing the user a dialog.
                                        try {
                                            // Show the dialog by calling startResolutionForResult(),
                                            // and check the result in onActivityResult().
                                            ResolvableApiException resolvable = (ResolvableApiException) throwable;
                                            resolvable.startResolutionForResult(MainActivity.this, REQUEST_ID_CHECK_SETTINGS);
                                        } catch (IntentSender.SendIntentException sendEx) {
                                            // Ignore the error.
                                        }
                                        break;
                                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                        // Location settings are not satisfied. However, we have no way
                                        // to fix the settings so we won't show the dialog.
                                        break;
                            }}));
        }
    }

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
