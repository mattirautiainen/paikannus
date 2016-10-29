package rautiainen.matti.paikannin;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
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

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private LatLng positionNew;
    private Marker markerNew;
    private Marker markerOld;
    private Mailer mailer;
    private Locator locator;

    Locator.LocationResult locationResult = new Locator.LocationResult(){
        @Override
        public void gotLocation(Location location){
            if(location == null) {
                //Toast.makeText(MapActivity.this, "Paikkatietoja ei saatu.", Toast.LENGTH_SHORT).show();
                return;
            }
            double latitude = location.getLatitude();
            double longtitude = location.getLongitude();

            positionNew = new LatLng(latitude, longtitude);

            if(markerNew != null) {
                markerNew.remove();
            }
            markerNew = googleMap.addMarker(new MarkerOptions().position(positionNew)
                    .title("Uusi mittaus: " + latitude + ", " + longtitude)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(positionNew));

            AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
            builder.setMessage("Lähetetäänkö paikkatiedot sähköpostiin?").setPositiveButton("Kyllä", dialogClickListener)
                    .setNegativeButton("Ei", dialogClickListener).setCancelable(false);

            AlertDialog dialog = builder.create();
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            wmlp.gravity = Gravity.TOP;
            dialog.show();
        }
    };


    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    mailer.sendCoordinates(positionNew.latitude, positionNew.longitude);
                    markerNew.remove();
                    if(markerOld != null) {
                        markerOld.remove();
                    }
                    markerOld = googleMap.addMarker(new MarkerOptions()
                            .position(positionNew)
                            .title("Vanha mittaus: " + positionNew.latitude + ", " + positionNew.longitude)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                    locator.writeCoordinates(positionNew.latitude,positionNew.longitude);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };

    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng location = locator.readCoordinates();
        if(location != null) {
            markerOld = googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title("Vanha mittaus: " + location.latitude + ", " + location.longitude)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        }
    }

    public void getLocation(View view) {
        locator.requestLocation(locationResult);
        Toast.makeText(MapActivity.this, "Paikkatietoja haetaan...", Toast.LENGTH_LONG).show();
    }

    public void editRecipient(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Vastaanottaja");
        alert.setMessage("Aseta vastaanottajan sähköpostiosoite");

        final EditText input = new EditText(this);
        input.setText(mailer.getRecipientAddress());
        alert.setView(input);

        alert.setPositiveButton("Hyväksy", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mailer.setRecipientAddress(input.getText().toString());
            }
        });

        alert.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mailer = new Mailer(this);
        locator = new Locator(this);

        SupportMapFragment fm = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);

        fm.getMapAsync(this);
    }

}
