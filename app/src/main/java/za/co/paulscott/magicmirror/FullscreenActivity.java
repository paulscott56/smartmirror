package za.co.paulscott.magicmirror;

import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AnalogClock;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FullscreenActivity extends AppCompatActivity {

    private String API_KEY = "e255e600633bd27746ed615a1bdae32f";
    private String UNITS = "metric";
    private String lat = "-26.130491";
    private String lon = "28.017944";
    private String getUrl = "http://api.openweathermap.org/data/2.5/weather?lat="
            + lat + "&lon=" + lon + "&apikey=" + API_KEY + "&units=" + UNITS;

    private boolean mVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWeatherString();

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
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public void getWeatherString() {
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
    }
}
