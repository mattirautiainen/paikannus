package rautiainen.matti.paikannin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;


public class Mailer {

    private Context context;

    public Mailer(Context context) {
        this.context = context;
    }

    public void setRecipientAddress(String recipientAddress) {
        SharedPreferences recipient = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = recipient.edit();
        editor.putString("recipient", recipientAddress);
        editor.apply();
    }

    public String getRecipientAddress() {
        SharedPreferences recipient = PreferenceManager.getDefaultSharedPreferences(context);
        return recipient.getString("recipient", "");
    }

    public void sendCoordinates(double latitude, double longtitude) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[] { getRecipientAddress()});
        i.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.coordinates));
        i.putExtra(Intent.EXTRA_TEXT   , String.valueOf(latitude) + ", " + String.valueOf(longtitude));
        try {
            context.startActivity(Intent.createChooser(i, context.getString(R.string.choose_program)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, context.getString(R.string.no_email_installed), Toast.LENGTH_SHORT).show();
        }
    }
}
