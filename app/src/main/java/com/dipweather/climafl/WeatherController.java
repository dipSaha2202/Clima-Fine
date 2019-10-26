package com.dipweather.climafl;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class WeatherController extends AppCompatActivity {
    final static int NEW_CITY_REQUEST_CODE = 2294;
    final int REQUEST_CODE_PERMISSION = 2202;
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    final String APP_ID = "e72ca729af228beabd5d20e3b7749713";

    final long MIN_TIME = 50000;
    final float MIN_DISTANCE = 1000;

    String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;

    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel, txtPressure, txtHumidity;

    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        mCityLabel = findViewById(R.id.locationTV);
        mWeatherImage = findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = findViewById(R.id.tempTV);
        ImageButton changeCityButton = findViewById(R.id.changeCityButton);
        txtPressure = findViewById(R.id.txtPressure);
        txtHumidity = findViewById(R.id.txtHumidity);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getWeatherForCurrentLocation();

        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newCityIntent = new Intent(
                        WeatherController.this, ChangeCityController.class);
                startActivityForResult(newCityIntent, NEW_CITY_REQUEST_CODE);
            }
        });
    }


    private void checkIfAnyLastKnownLocation() {
       if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED

                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //calling ActivityCompat#requestPermissions to request the missing permissions
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION);
            return;
        }

        Location location = locationManager.getLastKnownLocation(LOCATION_PROVIDER);

        if (location != null){
           setParamForLatLongAndFetchWeather(location);
           locationListener.onLocationChanged(location);
           // getWeatherForCurrentLocation();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_CITY_REQUEST_CODE && resultCode == RESULT_OK) {
            String cityName = data.getStringExtra("City");
            if (cityName != null) {
                getWeatherForCity(cityName);
            } else {
                getWeatherForCurrentLocation();
            }
        }
    }

    private void getWeatherForCity(String city) {
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);
        goToNetworkAndFetchData(params);
    }


    private void getWeatherForCurrentLocation() {
      //  locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setParamForLatLongAndFetchWeather(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                makeToastMessage("Error while getting GPS data. Turn on GPS");
            }
        };

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED

                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //calling ActivityCompat#requestPermissions to request the missing permissions
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION);
            return;
        }

        checkIfAnyLastKnownLocation();
        locationManager.requestLocationUpdates(
                LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
    }

    // to handle the case where the user grants the permission. See the documentation
    // Override onRequestPermissionResult
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeToastMessage("Permission Granted");
                getWeatherForCurrentLocation();
            } else {
                makeToastMessage("Permission Denied");
            }
        }
    }

    private void goToNetworkAndFetchData(RequestParams params) {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.get(WEATHER_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                WeatherDataModel weatherData = WeatherDataModel.dataFromJSon(response);
                if (weatherData != null) {
                    updateUI(weatherData);
                } else {
                    makeToastMessage("Weather Data was empty");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                makeToastMessage("Error while fetching data from internet. Turn on internet");
            }
        });
    }

    private void updateUI(WeatherDataModel weatherDataModel) {
        mTemperatureLabel.setText(weatherDataModel.getmTemperature());
        mCityLabel.setText(weatherDataModel.getmCity());
        txtHumidity.setText(weatherDataModel.getmHumidity());
        txtPressure.setText(weatherDataModel.getmPressure());

        int resourceID = getResources().getIdentifier(weatherDataModel.getmIconName(),
                "drawable", getPackageName());
        mWeatherImage.setImageResource(resourceID);
    }

    private void setParamForLatLongAndFetchWeather(Location location){
        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());

        RequestParams params = new RequestParams();
        params.put("lat", latitude);
        params.put("lon", longitude);
        params.put("appid", APP_ID);
        goToNetworkAndFetchData(params);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private void makeToastMessage(String message) {
        Toast.makeText(WeatherController.this, message, Toast.LENGTH_SHORT).show();
    }
}
