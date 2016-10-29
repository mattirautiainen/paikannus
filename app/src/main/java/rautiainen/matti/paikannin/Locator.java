package rautiainen.matti.paikannin;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static android.content.Context.MODE_PRIVATE;

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
        }

    }

    public void requestLocation(LocationResult locationResult) {
        this.locationResult = locationResult;
        googleApiClient = new GoogleApiClient.Builder(this.context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
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

        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    public void writeCoordinates(double latitude, double longtitude) {

        try {
            FileOutputStream fou = context.openFileOutput("coordinates.txt", MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fou);
            outputStreamWriter.write(String.valueOf(latitude)+"\r\n"+String.valueOf(longtitude));
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "Failed to write to file: " + e.toString());
        }
    }

    public LatLng readCoordinates() {

        String latitude = null;
        String longtitude = null;

        try {
            InputStream inputStream = context.openFileInput("coordinates.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                latitude = bufferedReader.readLine();
                longtitude = bufferedReader.readLine();
                inputStream.close();
            }
        } catch (IOException e) {
            Log.e("Exception", "Failed to read file: " + e.toString());
        }
        if (latitude != null && longtitude != null)
            return new LatLng(Double.parseDouble(latitude), Double.parseDouble(longtitude));
        else {
            return null;
        }
    }
    public static abstract class LocationResult {
        public abstract void gotLocation(Location location);
    }

}

