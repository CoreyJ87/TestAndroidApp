package info.coreyjones.TestApp;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;


public class sensorsMenu extends ActionBarActivity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    protected GoogleApiClient mGoogleApiClient;
    protected Location mCurrentLocation;
    private SensorManager mSensorManager;
    private Sensor mLight, mMagnetic, mBarometer, mHumidity;
    SensorManager smm;
    List<Sensor> sensor;
    ListView lv;
    protected boolean weatherOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors_menu);
        buildGoogleApiClient();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        smm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        lv = (ListView) findViewById(R.id.listView);
        sensor = smm.getSensorList(Sensor.TYPE_ALL);
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sensor));

        mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mBarometer = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
    }


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
        } else if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
            TextView humiditySensor = (TextView) findViewById(R.id.humidityValue);
            float rh = event.values[0];
            humiditySensor.setText(String.format("Relative Humidity: %s", rh));
            if (mCurrentLocation != null) {
                double tempF = getTemp();
            }
        }
    }

    protected double getTemp() {
        double temp = 0.0;
        RequestQueue queue = Volley.newRequestQueue(this);
        TextView latitude = (TextView) findViewById(R.id.lat);
        TextView longitudeVal = (TextView) findViewById(R.id.longitude);
        double lat = mCurrentLocation.getLatitude();
        double longitude = mCurrentLocation.getLongitude();

        latitude.setText(String.valueOf(lat));
        longitudeVal.setText(String.valueOf(longitude));

        if(weatherOnce == false) {
            String url = String.format("http://api.worldweatheronline.com/free/v2/weather.ashx?q=%s,%s", lat, longitude);

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            TextView responseText = (TextView) findViewById(R.id.responseText);
                            responseText.setText("Response is: " + response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    TextView responseText = (TextView) findViewById(R.id.responseText);
                    responseText.setText("That didn't work!");
                }
            });
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
            weatherOnce = !weatherOnce;
        }
        return temp;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sensors_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        //TextView connectionStatus = (TextView) findViewById(R.id.connectStatus);
        //connectionStatus.setText("Connected");
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
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
