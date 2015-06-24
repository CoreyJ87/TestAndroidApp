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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
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

import java.text.DateFormat;
import java.util.Date;
import java.util.List;


public class sensorsMenu extends ActionBarActivity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private SensorManager mSensorManager;
    private Sensor mLight, mMagnetic, mBarometer;
    private String theRestoredresponse;
    private SensorManager smm;
    private List<Sensor> sensor;
    private ListView lv;
    final Context context = this;

    //App Default Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors_menu);
        setupSensors();
        buildGoogleApiClient();
        restoreSensorText();
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

    public void getWeatherButton(View view) {
        if (mCurrentLocation != null) {
            TextView lastUpdated = (TextView) findViewById(R.id.lastUpdated);
            String updateTime = DateFormat.getTimeInstance().format(new Date());
            lastUpdated.setText(updateTime);
            getWeather(getCurrentFocus());
        }
    }

    //My Methods
    private void getWeather(final View view) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String apiKey = "a851d887d5e2d63218ba800ec5bb4";
        double lat = getLat();
        double longitude = getLong();
        updateLatLng(lat,longitude);
        String url = String.format("http://api.worldweatheronline.com/free/v2/weather.ashx?q=%s,%s&key=%s&format=JSON", lat, longitude, apiKey);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        TextView responseText = (TextView) findViewById(R.id.responseText);
                        responseText.setText("Response is: " + response);
                        theRestoredresponse = response;
                        saveSensorText(view);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TextView responseText = (TextView) findViewById(R.id.responseText);
                responseText.setText("That didn't work!");
            }
        });
        queue.add(stringRequest);
    }


    private double getLat() {
        if (!mCurrentLocation.equals(null)) {
            double lat = mCurrentLocation.getLatitude();
            return lat;
        }
        else {
            double lat = 0;
            return lat;
        }
    }

    private double getLong() {
        if (!mCurrentLocation.equals(null)) {
            double longitude = mCurrentLocation.getLongitude();
            return longitude;
        } else {
            double longitude = 0;
            return longitude;
        }
    }

    private void updateLatLng(double lat, double longitude) {
        TextView latitude = (TextView) findViewById(R.id.lat);
        TextView longitudeVal = (TextView) findViewById(R.id.longitude);
        latitude.setText(String.valueOf(lat));
        longitudeVal.setText(String.valueOf(longitude));
    }

    private void saveSensorText(View view) {
        //Grab text from editText field
        TextView editText = (TextView) findViewById(R.id.responseText);
        String message = editText.getText().toString();

        //Save text to preferences
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("the_response", message);
        editor.putString("the_time", DateFormat.getTimeInstance().format(new Date()));
        editor.apply();
        toastMessage(getString(R.string.text_saved));
    }

    private void restoreSensorText() {
        String defaultResponseVal = getString(R.string.response_message_default);
        String defaultTimeVal = getString(R.string.default_time);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        theRestoredresponse = sharedPref.getString("the_response", defaultResponseVal);
        String theRestoredTime = sharedPref.getString("the_time", defaultTimeVal);
        if (!theRestoredresponse.equals(defaultResponseVal)) {
            toastMessage(getString(R.string.textRestored));
        }
        TextView responseText = (TextView) findViewById(R.id.responseText);
        responseText.setText(theRestoredresponse);
        TextView lastUpdated = (TextView) findViewById(R.id.lastUpdated);
        lastUpdated.setText(theRestoredTime);
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

    private void toastMessage(CharSequence text) {
        Toast toast = Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT);
        toast.show();
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


    //Location Methods
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        //TextView connectionStatus = (TextView) findViewById(R.id.connectStatus);
        //connectionStatus.setText("Connected");
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        updateLatLng(getLat(),getLong());
    }

    @Override
    public void onConnectionSuspended(int i) {
        //TextView connectStatus = (TextView) findViewById(R.id.connectStatus);
        //connectStatus.setText("Suspended Connection");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //TextView connectStatus = (TextView) findViewById(R.id.connectStatus);
        //connectStatus.setText("Connect to lat/long Failed");
    }

}
