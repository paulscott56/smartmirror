package za.co.paulscott.magicmirror;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AnalogClock;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FullscreenActivity extends AppCompatActivity {

    private ListView lv;

    private boolean mVisible;
    private String API_KEY = "e255e600633bd27746ed615a1bdae32f";
    private String UNITS = "metric";
    private String lat = "-26.130491";
    private String lon = "28.017944";
    private String getUrl = "http://api.openweathermap.org/data/2.5/weather?lat="
            + lat + "&lon=" + lon + "&apikey=" + API_KEY + "&units=" + UNITS;
    private String trelloToken = "707fbd754835ac372811ef6c6365ebe560808b99b284b46a17f46cb632919b06";
    private String trelloKey = "718caa32afe66eca9ea319b14bb002cd";
    private String trelloBoardId = "57063bd1bfef9edab9b2ab90"; // Board ID of the Smart Mirror board
    private String trelloListId = "570641bc236e53e50efcbbff";
    private String trelloCardsUrl = "https://api.trello.com/1/list/570641bc236e53e50efcbbff?fields=name&cards=open&card_fields=name&key="
            + trelloKey + "&token=" + trelloToken;

    private RefreshHandler mRedrawHandler = new RefreshHandler();
    private TrelloRefreshHandler mTrelloRedrawHandler = new TrelloRefreshHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        }

        setContentView(R.layout.activity_fullscreen);
        mVisible = false;
        AnalogClock ac = (AnalogClock) findViewById(R.id.analogClock);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c.getTime());
        TextView txtView = (TextView) findViewById(R.id.date);
        txtView.setText(formattedDate);
        txtView.setGravity(Gravity.CENTER);
        txtView.setTextSize(20);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 10);
        updateUI();
        getTrelloCards();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                PackageManager pm = this.getPackageManager();
                Intent launchIntent = pm.getLaunchIntentForPackage("com.skyworthdigital.settings");
                this.startActivity(launchIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void getWeather() {

    }

    private void updateUI() {
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // parse out the JSON
                        try {
                            JSONObject res = new JSONObject(response);
                            JSONObject main = res.getJSONObject("main");
                            String minTemp = main.getString("temp_min");
                            String maxTemp = main.getString("temp_max");
                            String currTemp = main.getString("temp");
                            String ret = "Min: " + minTemp + "℃, Max: " + maxTemp + "℃, Current: " + currTemp + "℃";
                            TextView w = (TextView) findViewById(R.id.weath);
                            w.setText(ret);
                        } catch (JSONException e) {
                            TextView w = (TextView) findViewById(R.id.weath);
                            w.setText(R.string.noweather);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        mRedrawHandler.sleep(60 * 60 * 1000);
    }

    private void getTrelloCards() {
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, trelloCardsUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // parse out the JSON
                        try {
                            JSONObject res = new JSONObject(response);
                            JSONArray cards = res.getJSONArray("cards");
                            List<String> cardsToDisplay = new ArrayList<String>();
                            for (int i=0; i < cards.length(); i++) {
                                JSONObject card = cards.getJSONObject(i);
                                String cardName = card.getString("name");
                                cardsToDisplay.add(cardName);
                            }
                            String ret = cardsToDisplay.toString();
                            lv = (ListView) findViewById(R.id.listView);
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                    getApplicationContext(),
                                    android.R.layout.simple_list_item_1,
                                    cardsToDisplay );

                            lv.setAdapter(arrayAdapter);
                        } catch (JSONException e) {
                            List<String> errorCards = new ArrayList<String>();
                            lv = (ListView) findViewById(R.id.listView);
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                    getApplicationContext(),
                                    android.R.layout.simple_list_item_1,
                                    errorCards );

                            lv.setAdapter(arrayAdapter);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        mTrelloRedrawHandler.sleep(5 * 60 * 1000);
    }

    class RefreshHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            FullscreenActivity.this.updateUI();
        }

        public void sleep(long delayMillis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }

    class TrelloRefreshHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            FullscreenActivity.this.getTrelloCards();
        }

        public void sleep(long delayMillis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }

}
