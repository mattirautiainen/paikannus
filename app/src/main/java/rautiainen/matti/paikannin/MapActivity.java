package rautiainen.matti.paikannin;

import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private Mailer mailer;
    private Locator locator;
    private LocationIO disk;

    private abstract class MapLocation {
        private Marker marker;
        private double latitude;
        private double longitude;

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void addMarker(double lat, double lon) {
            latitude = lat;
            longitude = lon;
            removeMarker();
            marker = setMarker();
        }

        public abstract Marker setMarker();

        public void removeMarker() {
            if(marker != null)
                marker.remove();
        }
    }

    private MapLocation newLocation = new MapLocation() {
        @Override
        public Marker setMarker() {
            return googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(getLatitude(), getLongitude()))
                    .title(getString(R.string.new_measurement))
                    .snippet(getLatitude() + ", " + getLongitude())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
    };

    private MapLocation oldLocation = new MapLocation() {
        @Override
        public Marker setMarker() {
            return googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(getLatitude(), getLongitude()))
                    .title(getString(R.string.old_measurement))
                    .snippet(getLatitude() + ", " + getLongitude())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        }
    };

    DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            switch(whichButton) {
                case BUTTON_POSITIVE:
                    mailer.sendCoordinates(newLocation.getLatitude(), newLocation.getLongitude());
                    disk.writeCoordinates(newLocation.getLatitude(), newLocation.getLongitude());
                    oldLocation.addMarker(newLocation.getLatitude(),newLocation.getLongitude());
                    newLocation.removeMarker();
                    break;
                case BUTTON_NEGATIVE:
                    break;
            }
        }
    };

    Locator.LocationResult locationResult = new Locator.LocationResult(){
        @Override
        public void gotLocation(Location location){
            newLocation.addMarker(location.getLatitude(),location.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));

            AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
            AlertDialog dialog = builder.setMessage(getString(R.string.send_to_email))
                    .setPositiveButton(getString(R.string.yes), dialogListener)
                    .setNegativeButton(getString(R.string.no), dialogListener)
                    .setCancelable(false)
                    .create();
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            wmlp.gravity = Gravity.TOP;
            dialog.show();
        }
    };

    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng location = disk.readCoordinates();
        if(location != null) {
            oldLocation.addMarker(location.latitude,location.longitude);
        }
    }

    public void getLocation(View view) {
        locator.requestLocation(locationResult);
        Toast.makeText(MapActivity.this, getString(R.string.locating), Toast.LENGTH_LONG).show();
    }

    public void editRecipient(View view) {
        final EditText input = new EditText(this);
        input.setText(mailer.getRecipientAddress());

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.title_recipient)).setMessage(getString(R.string.set_email_address)).setView(input);
        dialog.setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mailer.setRecipientAddress(input.getText().toString());
            }
        });
        dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        dialog.show();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mailer = new Mailer(this);
        locator = new Locator(this);
        disk = new LocationIO(this);

        SupportMapFragment fm = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);

        fm.getMapAsync(this);
    }

}
