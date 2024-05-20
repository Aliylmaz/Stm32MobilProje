package com.example.myhome;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WeatherManager {

    private static final String TAG = "WeatherManager";
    private static final String API_KEY = "aec0113d1aaa1e73113e89015ff084ca"; // OpenWeatherMap API key
    private static WeatherManager instance;
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue requestQueue;
    private Context context;
    private Location currentLocation;
    private JSONObject weatherData;

    // Geri çağırma arayüzü
    public interface WeatherCallback {
        void onWeatherDataFetched();
        void onError(String error);
    }

    private WeatherManager(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public static synchronized WeatherManager getInstance(Context context) {
        if (instance == null) {
            instance = new WeatherManager(context);
        }
        return instance;
    }

    @SuppressLint("MissingPermission")
    public void getLocationAndWeather(final WeatherCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permissions are not granted");
            callback.onError("Location permissions are not granted");
            return;
        }
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(60000); // 1 dakika
        locationRequest.setFastestInterval(10000); // 10 saniye

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.w(TAG, "Location result is null");
                    callback.onError("Location result is null");
                    return;
                }
                currentLocation = locationResult.getLastLocation();
                fetchWeatherData(callback);
                fusedLocationClient.removeLocationUpdates(this);
            }
        }, Looper.getMainLooper());
    }

    private void fetchWeatherData(final WeatherCallback callback) {
        if (currentLocation == null) {
            Log.w(TAG, "Current location is null, cannot fetch weather data");
            callback.onError("Current location is null");
            return;
        }

        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + currentLocation.getLatitude() + "&lon=" + currentLocation.getLongitude() + "&appid=" + API_KEY + "&units=metric&lang=tr";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        weatherData = response;
                        Log.d(TAG, "Weather data fetched successfully.");
                        callback.onWeatherDataFetched();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error fetching weather data: " + error.getMessage());
                callback.onError("Error fetching weather data: " + error.getMessage());
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    public String getHavaDurumuIcon() {
        try {
            if (weatherData != null) {
                return weatherData.getJSONArray("weather").getJSONObject(0).getString("icon");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting weather icon: " + e.getMessage());
        }
        return null;
    }

    public String getSıcaklık() {
        try {
            if (weatherData != null) {
                return weatherData.getJSONObject("main").getString("temp");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting temperature: " + e.getMessage());
        }
        return null;
    }


    public String getKonum() {
        try {
            if (weatherData != null) {
                return weatherData.getString("name");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting location: " + e.getMessage());
        }
        return null;
    }

    public String getSaat() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    public String getHaftanınGünü() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", new Locale("tr"));
        return sdf.format(calendar.getTime());
    }
}
