package rautiainen.matti.paikannin;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

public class LocationIO {

    private Context context;

    public LocationIO(Context context) {
        this.context = context;
    }

    public void writeCoordinates(double latitude, double longitude) {
        SharedPreferences coordinates = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = coordinates.edit();
        editor.putLong("latitude", Double.doubleToLongBits(latitude));
        editor.putLong("longitude", Double.doubleToLongBits(longitude));
        editor.apply();
    }

    public LatLng readCoordinates() {

        SharedPreferences coordinates = PreferenceManager.getDefaultSharedPreferences(context);
        if(!(coordinates.contains("latitude") && coordinates.contains("longitude")))
            return null;

        double latitude = Double.longBitsToDouble(coordinates.getLong("latitude", 0));
        double longitude = Double.longBitsToDouble(coordinates.getLong("longitude", 0));

        return new LatLng(latitude, longitude);
    }

}
