package info.coreyjones.witcher3map;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;


public class SettingsMenu extends ActionBarActivity implements CompoundButton.OnCheckedChangeListener {

    protected boolean wearableState = false;
    protected boolean fileState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_menu);

        //Create Text View with random text
        TextView myTextView = (TextView) findViewById(R.id.settings_text);
        myTextView.setText(Html.fromHtml(getString(R.string.settings_info)));
        myTextView.setMovementMethod(LinkMovementMethod.getInstance());

        //Restore edit text field
        String defaultValue = getString(R.string.edit_message_default);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String theRestoredMessage = sharedPref.getString("the_message", defaultValue);
        if (theRestoredMessage != defaultValue) {
            toastMessage(getString(R.string.textRestored));
        }
        EditText editTextField = (EditText) findViewById(R.id.messageText);
        editTextField.setText(theRestoredMessage);

        //Setup wearable switch
        Switch wearSwitch = (Switch) findViewById(R.id.sendWearable);
        wearableState = sharedPref.getBoolean("switch_state", false);
        wearSwitch.setChecked(wearableState);
        wearSwitch.setOnCheckedChangeListener(this);

        final CheckBox theCheckBox = (CheckBox) findViewById(R.id.checkBox);
        theCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileState = theCheckBox.isChecked();
            }
        });

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        Toast.makeText(this, "The Switch is " + (isChecked ? "on" : "off"),
                Toast.LENGTH_SHORT).show();
        if (isChecked) {
            wearableState = true;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("switch_state", wearableState);
            editor.apply();
        } else {
            wearableState = false;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("switch_state", wearableState);
            editor.apply();
        }
    }

    public void saveText(View view) {
        //Grab text from editText field
        EditText editText = (EditText) findViewById(R.id.messageText);
        String message = editText.getText().toString();

        //Save text to preferences
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("the_message", message);
        editor.apply();
        toastMessage(getString(R.string.text_saved));

        //If wearable switch state is set to true. Send to watch
        if (wearableState) {
            sendWearableNoti(getString(R.string.text_saved), message);
        }
        if(fileState){
            toastMessage("File State true");
            saveToFile(message,"textToSave.txt");
            readFile("textToSave.txt");
        }

    }

    public void resetText(View view) {
        EditText editText = (EditText) findViewById(R.id.messageText);
        editText.setText(getString(R.string.edit_message_default));
    }

    /* Protected functions */

    //Toast Message Wrapper for easier use.
    protected void toastMessage(CharSequence text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    protected void sendWearableNoti(String notiTitle, String notiContent) {
        int notificationId = 001;
        int eventId = 1;
        String EXTRA_EVENT_ID = "";

        Intent viewIntent = new Intent(this, SettingsMenu.class);

        viewIntent.putExtra(EXTRA_EVENT_ID, eventId);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_event_black_12dp)
                        .setContentTitle(notiTitle)
                        .setContentText(notiContent)
                        .setContentIntent(viewPendingIntent);

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    protected void saveToFile(String textToSave,String filename){
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(textToSave.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void readFile(String filename){
        StringBuilder text = new StringBuilder();
        try {
            File sdcard = getFilesDir();
            File file = new File(sdcard,filename);

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close() ;
        }catch (IOException e) {
            e.printStackTrace();
        }

        TextView tv = (TextView)findViewById(R.id.fileOutput);
        tv.setText(text.toString()); ////Set the text to text view.
    }
}
