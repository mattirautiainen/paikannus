package rautiainen.matti.paikannin;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static android.content.Context.MODE_PRIVATE;

public class Mailer {

    private Context context;

    public Mailer(Context context) {
        this.context = context;
    }

    public void setRecipientAddress(String recipientAddress) {
        try {
            FileOutputStream fou = context.openFileOutput("recipient.txt", MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fou);
            outputStreamWriter.write(recipientAddress);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "Failed to write to file: " + e.toString());
        }
    }

    public String getRecipientAddress() {
        String recipientAdress = null;
        try {
            InputStream inputStream = context.openFileInput("recipient.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                recipientAdress = bufferedReader.readLine();
                inputStream.close();
            }
        } catch (IOException e) {
            Log.e("Exception", "Failed to read file: " + e.toString());
        }
        return recipientAdress;
    }

    public void sendCoordinates(double latitude, double longtitude) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[] { getRecipientAddress()});
        i.putExtra(Intent.EXTRA_SUBJECT, "Koordinaatit");
        i.putExtra(Intent.EXTRA_TEXT   , String.valueOf(latitude) + ", " + String.valueOf(longtitude));
        try {
            context.startActivity(Intent.createChooser(i, "Valitse ohjelma"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "Ei sähköpostiohjelmia asennettuna.", Toast.LENGTH_SHORT).show();
        }
    }
}
