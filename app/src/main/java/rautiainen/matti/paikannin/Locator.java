package rautiainen.matti.paikannin;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class Locator implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final float MIN_ACCURACY = 40.0f;

    private Context context;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;

    private LocationResult locationResult;

    public Locator(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.getAccuracy() < MIN_ACCURACY) {
            locationResult.gotLocation(location);
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    public void requestLocation(LocationResult locationResult) {
        this.locationResult = locationResult;
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this.context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000*10);
        locationRequest.setFastestInterval(1000*5);
        if (servicesAvailable()) {

            int permissionCheck = ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION);
            //TODO

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    private boolean servicesAvailable() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);

        if (ConnectionResult.SUCCESS == resultCode)
            return true;
        else
            return false;
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    public static abstract class LocationResult {
        public abstract void gotLocation(Location location);
    }

}

