package info.coreyjones.TestApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import org.json.JSONStringer;

import java.text.DateFormat;
import java.util.Date;


public class Weather extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        buildGoogleApiClient();
        restoreSensorText();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_weather, menu);
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
        final String apiUrl = "http://api.worldweatheronline.com/free/v2/weather.ashx";
        double lat = getLat();
        double longitude = getLong();
        String url = String.format("%s?q=%s,%s&key=%s&format=JSON", apiUrl, lat, longitude, apiKey);

        updateLatLng(lat, longitude);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        TextView responseText = (TextView) findViewById(R.id.responseText);
                        responseText.setText("Response is: " + response);
                        parseJson(response);
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


    private void parseJson(String theResponse) {
        if (!theResponse.equals(null)) {
            try {
                String weatherOutput = "Current Conditions:";
                JSONObject reader = new JSONObject(theResponse);
                JSONObject data = reader.getJSONObject("data");

                //Top levels of the json
                JSONArray current_condition_array = data.optJSONArray("current_condition");
                JSONArray weather_array = data.optJSONArray("weather");

                //Current Conditions. We dont loop the obj cus theres only one current condition
                JSONObject current_condition_obj = current_condition_array.getJSONObject(0);
                for(int i = 0; i<current_condition_obj.names().length(); i++){
                    Log.v("Response", "key = " + current_condition_obj.names().getString(i) + " value = " + current_condition_obj.get(current_condition_obj.names().getString(i)));
                    weatherOutput = String.format("%s\n  %s: %s",weatherOutput,current_condition_obj.names().getString(i),current_condition_obj.get(current_condition_obj.names().getString(i)));
                }

                //Weather by date. Loop later because it shows up to 5 days
                for(int x=0;x< weather_array.length();x++){
                    //Main Weather Obj
                    JSONObject weather_obj = weather_array.getJSONObject(x);

                    //Append the date of the current weather day
                    String date = weather_obj.getString("date");
                    String maxTempF = weather_obj.getString("maxtempF");
                    String minTemp = weather_obj.getString("mintempF");
                    String uvIndex = weather_obj.getString("uvIndex");

                    weatherOutput = String.format("%s\nMaxTemp: %s\nMinTemp: %s\nuvIndex: %s\n",weatherOutput,maxTempF,minTemp,uvIndex);
                    weatherOutput = String.format("%s\n\n----Date: %s----", weatherOutput, date);

                    //Astronomy
                    JSONArray astronomy_array = weather_obj.getJSONArray("astronomy");
                    JSONObject astronomy = astronomy_array.getJSONObject(0);
                    for(int i = 0; i<astronomy.names().length(); i++){
                        Log.v("Response", "key = " + astronomy.names().getString(i) + " value = " + astronomy.get(astronomy.names().getString(i)));
                        weatherOutput = String.format("%s\n  %s: %s",weatherOutput,astronomy.names().getString(i),astronomy.get(astronomy.names().getString(i)));
                    }

                    //Hourly
                    JSONArray hourly_obj_array = weather_obj.getJSONArray("hourly");
                    for(int a = 0;a<hourly_obj_array.length();a++){
                        JSONObject hourly_obj = hourly_obj_array.getJSONObject(a);
                        String time = hourly_obj.getString("time");
                        weatherOutput = String.format("%s\n\n Time: %s",weatherOutput, time);
                        for(int i = 0; i<hourly_obj.names().length(); i++){
                            Log.v("Response", "key = " + hourly_obj.names().getString(i) + " value = " + hourly_obj.get(hourly_obj.names().getString(i)));
                            weatherOutput = String.format("%s\n%s: %s",weatherOutput,hourly_obj.names().getString(i),hourly_obj.get(hourly_obj.names().getString(i)));
                        }
                    }
                }

                //Output to textView
                TextView parsedTextView = (TextView) findViewById(R.id.parsedData);
                parsedTextView.setText(weatherOutput);
                saveSensorText(getCurrentFocus());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private double getLat() {
        if (!mCurrentLocation.equals(null)) {
            return mCurrentLocation.getLatitude();
        } else {
            return 0;
        }
    }

    private double getLong() {
        if (!mCurrentLocation.equals(null)) {
            return mCurrentLocation.getLongitude();
        } else {
            return 0;
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
        TextView unparsedTextView = (TextView) findViewById(R.id.responseText);
        TextView parsedTextView = (TextView) findViewById(R.id.parsedData);
        String unparsed = unparsedTextView.getText().toString();
        String parsed = parsedTextView.getText().toString();

        //Save text to preferences
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("unparsed_json", unparsed);
        editor.putString("parsed_json", parsed);
        editor.putString("the_time", DateFormat.getTimeInstance().format(new Date()));

        editor.apply();
        toastMessage(getString(R.string.text_saved));
    }

    private void restoreSensorText() {
        String defaultResponseVal = getString(R.string.response_message_default);
        String defaultTimeVal = getString(R.string.default_time);
        String defaultParsedJson = "Parsed Json Default";
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String theRestoredresponse = sharedPref.getString("unparsed_json", defaultResponseVal);
        String theRestoredTime = sharedPref.getString("the_time", defaultTimeVal);
        String theRestoredParsed = sharedPref.getString("parsed_json",defaultParsedJson);

        if (!theRestoredresponse.equals(defaultResponseVal)) {
            toastMessage(getString(R.string.textRestored));
        }

        TextView responseText = (TextView) findViewById(R.id.responseText);
        responseText.setText(theRestoredresponse);

        TextView parsedTextView = (TextView) findViewById(R.id.parsedData);
        parsedTextView.setText(theRestoredParsed);

        TextView lastUpdated = (TextView) findViewById(R.id.lastUpdated);
        lastUpdated.setText(theRestoredTime);
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
        updateLatLng(getLat(), getLong());
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

    private void toastMessage(CharSequence text) {
        Toast toast = Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }
}
