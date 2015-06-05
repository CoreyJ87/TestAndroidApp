package info.coreyjones.witcher3map;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


public class Witcher3Map extends ActionBarActivity {
    public boolean fullScreen = false;
    public final String siteURL = "http://witcher3map.com";


    //Overrides for activity creation and event handlers
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_witcher3_map);
        WebView theMapView = setupWebView(siteURL, R.id.mapView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_witcher3_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handling action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            initSettingsMenu();
        } else if (id == R.id.action_immersive) {
            toggleHideyBar();
            if (fullScreen) {
                toastMessage("Full Screen mode: OFF!");
                fullScreen = !fullScreen;
            } else {
                toastMessage("Full Screen mode: ON!");
                fullScreen = !fullScreen;
            }
        } else if (id == R.id.refresh_window) {
            refreshPage(R.id.mapView);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        WebView myWebView = (WebView) findViewById(R.id.mapView);
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
            myWebView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    //Custom Functions

    //WebView Creator
    protected WebView setupWebView(String theURL, int TheViewID) {
        WebView theWebView = (WebView) findViewById(TheViewID);
        theWebView.setWebViewClient(new WebViewClient());

        WebSettings webSettings = theWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setSupportZoom(true);

        theWebView.loadUrl(theURL);
        return theWebView;
    }

    //Refresh WebView - Pass in R.id.controlID
    protected void refreshPage(int TheID){
        WebView theWebView = (WebView) findViewById(TheID);
        String currentURL = theWebView.getUrl();
        theWebView.loadUrl(currentURL);
        toastMessage("Refreshing Page...");
    }

    //Settings Menu Initialization
    protected void initSettingsMenu() {
        Intent intent = new Intent(this, SettingsMenu.class);
        startActivity(intent);
    }

    //Toast Message Wrapper for easier use.
    protected void toastMessage(CharSequence text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    //Immersion Mode Function from Google
    protected void toggleHideyBar() {

        int uiOptions = this.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;

        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        // Immersive mode: Backward compatible to KitKat.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        this.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        //END_INCLUDE (set_ui_flags)
    }


}
