package info.coreyjones.TestApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import java.text.DateFormat;
import java.util.Date;

public class Weather extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private String responseText = "";
    final String apiKey = "a851d887d5e2d63218ba800ec5bb4";
    final String apiUrl = "http://api.worldweatheronline.com/free/v2/weather.ashx";
    ImageLoader imageLoader = ImageLoader.getInstance();

    //View Bindings
    @Bind(R.id.parsedData)
    TextView parsedTextView;
    @Bind(R.id.longitude)
    TextView longitudeVal;
    @Bind(R.id.lat)
    TextView latitude;
    @Bind(R.id.lastUpdated)
    TextView lastUpdated;
    @Bind(R.id.connectionStatus)
    TextView connectionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);
        ButterKnife.bind(this);
        buildGoogleApiClient();
        restoreSensorText(true);
    }

    @Override
    protected void onResume(){
        super.onResume();
        restoreSensorText(false);
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

    @OnClick(R.id.clear_button)
     void clearAll(Button button){
        String defaultResponseVal = getString(R.string.response_message_default);
        String defaultTimeVal = getString(R.string.default_time);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("unparsed_json", defaultResponseVal);
        editor.putString("the_time", defaultTimeVal);
        parsedTextView.setText("");
        responseText="";
        editor.apply();
        toastMessage("defaulted values");
    }

    @OnClick(R.id.refreshWeather)
    void refreshWeatherButton(Button button) {
        String updateTime = DateFormat.getTimeInstance().format(new Date());
        lastUpdated.setText(updateTime);
        getWeather();
    }

    private void getWeather() {
        double lat = getLat();
        double longitude = getLong();
        updateLatLng(lat, longitude);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = String.format("%s?q=%s,%s&key=%s&format=JSON", apiUrl, lat, longitude, apiKey);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        responseText = response;
                        parseJson(response,true);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
              //Do something with the error msg
            }
        });
        queue.add(stringRequest);
    }


    private void parseJson(String theResponse,boolean saveIt) {
        if (!theResponse.equals(null)) {
            try {
                String weatherOutput = "Current Conditions:";
                JSONObject reader = new JSONObject(theResponse);
                JSONObject data = reader.getJSONObject("data");
                parsedTextView.setText("");
                parsedTextView.setText(weatherOutput);
                //Top levels of the json
                JSONArray current_condition_array = data.optJSONArray("current_condition");
                JSONArray weather_array = data.optJSONArray("weather");

                //Current Conditions. We dont loop the obj cus theres only one current condition
                JSONObject current_condition_obj = current_condition_array.getJSONObject(0);
                for (int i = 0; i < current_condition_obj.names().length(); i++) {
                    Log.v("Response", "key = " + current_condition_obj.names().getString(i) + " value = " + current_condition_obj.get(current_condition_obj.names().getString(i)));
                    if (current_condition_obj.names().getString(i).equalsIgnoreCase("weatherDesc")) {
                        JSONArray weatherDescArr = current_condition_obj.optJSONArray("weatherDesc");
                        JSONObject weatherDescObj = weatherDescArr.getJSONObject(0);
                        parsedTextView.append(String.format("\nWeather Description: %s", weatherDescObj.getString("value")));
                    } else if (current_condition_obj.names().getString(i).equals("weatherIconUrl")) {
                        JSONArray weatherIconArr = current_condition_obj.optJSONArray("weatherIconUrl");
                        JSONObject weatherIconObj = weatherIconArr.getJSONObject(0);
                        //parsedTextView.append(String.format("\nWeather Icon URL: %s", weatherIconObj.getString("value")));
                        Bitmap bmp = imageLoader.loadImageSync(weatherIconObj.getString("value"));
                        SpannableStringBuilder ssb = new SpannableStringBuilder("\nCurrent weather condition: ");
                        ssb.setSpan(new ImageSpan(getApplicationContext(), bmp, DynamicDrawableSpan.ALIGN_BASELINE), 27, 28, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        parsedTextView.append(ssb);
                    } else {
                        parsedTextView.append(String.format("\n%s: %s", current_condition_obj.names().getString(i), current_condition_obj.get(current_condition_obj.names().getString(i))));
                    }
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

                   parsedTextView.append(String.format("\n\n----Date: %s----", date));
                   parsedTextView.append(String.format("\nMaxTemp: %s\nMinTemp: %s\nuvIndex: %s",maxTempF, minTemp, uvIndex));

                    //Astronomy
                    JSONArray astronomy_array = weather_obj.getJSONArray("astronomy");
                    JSONObject astronomy = astronomy_array.getJSONObject(0);
                    for(int i = 0; i<astronomy.names().length(); i++){
                        Log.v("Response", "key = " + astronomy.names().getString(i) + " value = " + astronomy.get(astronomy.names().getString(i)));
                        parsedTextView.append(String.format("\n%s: %s", astronomy.names().getString(i), astronomy.get(astronomy.names().getString(i))));
                    }

                    //Hourly
                    JSONArray hourly_obj_array = weather_obj.getJSONArray("hourly");
                    for(int a = 0;a<hourly_obj_array.length();a++){
                        JSONObject hourly_obj = hourly_obj_array.getJSONObject(a);
                        String time = hourly_obj.getString("time");
                        parsedTextView.append(String.format("\n\n Time: %s",time));
                        for(int i = 0; i<hourly_obj.names().length(); i++){
                            Log.v("Response", "key = " + hourly_obj.names().getString(i) + " value = " + hourly_obj.get(hourly_obj.names().getString(i)));
                            if (hourly_obj.names().getString(i).equalsIgnoreCase("weatherDesc")) {
                                JSONArray weatherDescArr = hourly_obj.optJSONArray("weatherDesc");
                                JSONObject weatherDescObj = weatherDescArr.getJSONObject(0);
                                parsedTextView.append(String.format("\nWeather Description: %s", weatherDescObj.getString("value")));
                            } else if (hourly_obj.names().getString(i).equalsIgnoreCase("weatherIconUrl")) {
                                JSONArray weatherIconArr = current_condition_obj.optJSONArray("weatherIconUrl");
                                JSONObject weatherIconObj = weatherIconArr.getJSONObject(0);
                                //parsedTextView.append(String.format("\nWeather Icon URL: %s", weatherIconObj.getString("value")));
                                Bitmap bmp = imageLoader.loadImageSync(weatherIconObj.getString("value"));
                                String timeWeatherString = "\n"+time+" weather condition: ";
                                int timeWeatherLength = timeWeatherString.length();
                                SpannableStringBuilder ssb = new SpannableStringBuilder(timeWeatherString);
                                ssb.setSpan(new ImageSpan(getApplicationContext(), bmp, DynamicDrawableSpan.ALIGN_BASELINE), timeWeatherLength-1, timeWeatherLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                parsedTextView.append(ssb);
                            } else {
                                parsedTextView.append(String.format("\n%s: %s", hourly_obj.names().getString(i), hourly_obj.get(hourly_obj.names().getString(i))));
                            }
                        }
                    }
                }
                if(saveIt) { saveSensorText(false); }
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
        latitude.setText(String.valueOf(lat));
        longitudeVal.setText(String.valueOf(longitude));
    }

    private void saveSensorText(boolean showToast) {
        //Save text to preferences
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("unparsed_json", responseText);
        editor.putString("the_time", DateFormat.getTimeInstance().format(new Date()));
        editor.apply();
        if(showToast) {
            toastMessage(getString(R.string.text_saved));
        }
    }

    private void restoreSensorText(boolean showToast) {
        String defaultResponseVal = getString(R.string.response_message_default);
        String defaultTimeVal = getString(R.string.default_time);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String theRestoredresponse = sharedPref.getString("unparsed_json", defaultResponseVal);
        String theRestoredTime = sharedPref.getString("the_time", defaultTimeVal);
        Log.v("Restored Response", theRestoredresponse);

        if (!theRestoredresponse.equals(defaultResponseVal) && showToast) {
            toastMessage(getString(R.string.textRestored));
        }
        parseJson(theRestoredresponse,false);
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
        connectionStatus.setText("Connected");
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        updateLatLng(getLat(), getLong());
    }

    @Override
    public void onConnectionSuspended(int i) {
        connectionStatus.setText("Suspended Connection");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        connectionStatus.setText("Connect to lat/long Failed");
    }

    private void toastMessage(CharSequence text) {
        Toast toast = Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }
}
