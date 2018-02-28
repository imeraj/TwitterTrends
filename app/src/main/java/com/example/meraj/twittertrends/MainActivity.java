package com.example.meraj.twittertrends;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    final String TAG = MainActivity.class.getSimpleName();

    ArrayList<HashMap<String, String>> trendsList;
    ListView listView;
    ProgressBar pBar;
    String token = "";
    String woeid = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        trendsList = new ArrayList<>();
        listView = findViewById(R.id.list);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new GetTrends().execute();
                } else {
                    new GetTrends().execute();
                    Toast.makeText(this, "Location permission not given!", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private class GetTrends extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pBar = findViewById(R.id.simpleProgressBar);
            pBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... arg0) {
           // woeid = getCurrentWOEID();
            return getAuthToken(woeid);
        }

        @Override
        protected void onPostExecute(String result) {
            if (pBar.isShown())
                pBar.setVisibility(View.INVISIBLE);

            token = result;
            new GetTrendsTopics().execute();
        }
    }

    public String getCurrentWOEID() {
        Location location = null;
        String cityName = "";

        try {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException e) {
            Log.d(TAG, "SecurityException: " + e.getMessage());
        }

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses.size() > 0) {
                    cityName = addresses.get(0).getLocality();
                }
            } catch (IOException e) {
                Log.d(TAG, "IOException: " + e.getMessage());
            }

            if (!"".equals(cityName)) {
                HttpRequestHandler handler = new HttpRequestHandler("GET", Globals.WOEID_URL + "\"" + cityName + "\"" + " limit 1");
                handler.setContentType("application/json");
                String response = handler.makeRequest();

                try {
                    JSONObject jsonObjectDocument = new JSONObject(response.toString());
                    JSONObject place = jsonObjectDocument.getJSONObject("query").getJSONObject("results").getJSONObject("place");
                    woeid = place.getString("woeid");
                } catch (JSONException e) {
                    Log.d(TAG, "JSONException: " + e.getMessage());
                }
            }
        }

        return woeid;
    }

    public String getAuthToken(String woeid) {
        BufferedReader bufferedReader;
        HttpURLConnection conn;
        String token = "";
        String credentials = Globals.CONSUMER_KEY + ":"  + Globals.CONSUMER_SECRET;
        String authorization = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        String contentType = "application/x-www-form-urlencoded;charset=UTF-8";
        String param = "grant_type=client_credentials";

        HttpRequestHandler handler = new HttpRequestHandler("POST", Globals.TWITTER_AUTH_API);

        handler.setAuthorization(authorization);
        handler.setContentType(contentType);
        handler.setParam(param);

        String response = handler.makeRequest();

        try {
            JSONObject jsonObjectDocument = new JSONObject(response.toString());
            token = jsonObjectDocument.getString("token_type") + " " + jsonObjectDocument.getString("access_token");
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }

        return token;
    }

    private class GetTrendsTopics extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pBar = findViewById(R.id.simpleProgressBar);
            pBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... arg0) {
            return getTrendTopics();
        }

        @Override
        protected void onPostExecute(String result) {
            if (pBar.isShown())
                pBar.setVisibility(View.INVISIBLE);

            parseJson(result);

            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, trendsList,
                    R.layout.list_item, new String[]{"name", "url",
                    "query", "tweet_volume"}, new int[] { R.id.name,
                    R.id.url, R.id.query, R.id.tweet_volume} );

            listView.setAdapter(adapter);

        }
    }

    private String getTrendTopics() {
        HttpRequestHandler handler = new HttpRequestHandler("GET", Globals.TWITTER_TRENDS_API + woeid);

        handler.setAuthorization(token);
        handler.setContentType("application/json");

        String response = handler.makeRequest();
        return response;
    }

    private void parseJson(String jsonStr) {
        if (jsonStr != null && !"".equals(jsonStr)) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr.substring(1, jsonStr.length() -1));
                JSONArray trends = jsonObj.getJSONArray("trends");

                for (int i = 0; i < trends.length(); i++) {
                    JSONObject c = trends.getJSONObject(i);

                    try {
                        String name =  URLDecoder.decode(c.getString("name"), "UTF-8");
                        String url =   URLDecoder.decode(c.getString("url"), "UTF-8");
                        String query = URLDecoder.decode(c.getString("query"), "UTF-8");
                        String tweet_volume =  URLDecoder.decode(c.getString("tweet_volume"), "UTF-8");

                        HashMap<String, String> trend = new HashMap<>();

                        trend.put("name", name);
                        trend.put("url", url);
                        trend.put("query", query);
                        trend.put("tweet_volume", tweet_volume == "null" ? "" : tweet_volume);

                        trendsList.add(trend);
                    } catch (UnsupportedEncodingException e) {
                        Log.d(TAG, "UnsupportedEncodingException: " + e.getMessage());
                    }
                }
            } catch (JSONException e) {
                Log.d(TAG, "JSONException: " + e.getMessage());
            }
        }
    }
}
