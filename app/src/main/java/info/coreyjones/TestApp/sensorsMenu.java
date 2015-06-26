package info.coreyjones.TestApp;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;


public class sensorsMenu extends ActionBarActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mLight, mMagnetic, mBarometer;
    private List<Sensor> sensor;
    private ListView lv;
    final Context context = this;

    //App Default Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors_menu);
        setupSensors();
        createSensorListDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sensors_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void createSensorListDialog() {
        final Dialog optionDialog = new Dialog(context);
        optionDialog.setContentView(R.layout.dialog);
        optionDialog.setTitle("Options");
        Button cancelButton = (Button) optionDialog.findViewById(R.id.dialogButtonCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionDialog.dismiss();
            }
        });

        Button listSensors = (Button) findViewById(R.id.listSensors);
        listSensors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionDialog.show();
            }
        });
        lv = (ListView) optionDialog.findViewById(R.id.listView);
        sensor = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sensor));
    }

    private void setupSensors() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mBarometer = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    //Sensor Methods
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        float x, y, z;
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lux = event.values[0];
            String luxString = String.format("Light Sensor(Lux) Value: %s", String.valueOf(lux));
            TextView lightSensor = (TextView) findViewById(R.id.lightSensorValue);
            lightSensor.setText(luxString);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            TextView magnetSensor = (TextView) findViewById(R.id.magnetvalues);
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
            String fullString = String.format("Magnetometer: \nX: %s\nY: %s\nZ: %s", x, y, z);
            magnetSensor.setText(fullString);
        } else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            TextView pressureSensor = (TextView) findViewById(R.id.barometerValue);
            String pressureString = String.format("Barometric Pressure:\n%s:hPa(millibar)", event.values[0]);
            pressureSensor.setText(pressureString);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mBarometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
