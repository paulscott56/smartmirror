package za.co.paulscott.magicmirror;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pscot on 3/27/2016.
 */
public class OpenWeatherApi {

    private String API_KEY = "e255e600633bd27746ed615a1bdae32f";
    private String UNITS = "metric";
    private String lat = "-26.130491";
    private String lon = "28.017944";
    private String getUrl = "http://api.openweathermap.org/data/2.5/weather?lat="
            + lat + "&lon=" + lon + "&apikey=" + API_KEY + "&units=" + UNITS;

    private String currString = "";
    private Context ctx;

    public String getCurrString() {
        return currString;
    }

    public void setCurrString(String currString) {
        this.currString = currString;
    }

    public void getWeatherString(Context context) {
        this.ctx = context;
        RequestQueue queue = Volley.newRequestQueue(ctx);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // parse out the JSON
                        System.out.println(response);
                        try {
                            JSONObject res = new JSONObject(response);
                            JSONObject main = res.getJSONObject("main");
                            String minTemp = main.getString("temp_min");
                            String maxTemp = main.getString("temp_max");
                            String currTemp = main.getString("temp");
                            String ret = "Min: " + minTemp + "C Max: " + maxTemp + "C Current: " + currTemp + "C";
                            System.out.println("--------------------------------------------------------------------------" + ret);
                            setCurrString(ret);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ctx, "That didn't work!", Toast.LENGTH_SHORT).show();
                System.out.println(error.toString());
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


}
