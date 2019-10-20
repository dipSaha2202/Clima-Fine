package com.londonappbrewery.climapm;

import org.json.JSONException;
import org.json.JSONObject;

public class WeatherDataModel {

    // the member variables here
    private String mTemperature, mCity, mIconName, mPressure, mHumidity;
    private int mCondtion;

    // WeatherDataModel from a JSON:
    public static WeatherDataModel dataFromJSon(JSONObject jsonObject){
        WeatherDataModel weatherData = new WeatherDataModel();
        try {
            weatherData.mCity = jsonObject.getString("name");
            weatherData.mCondtion = jsonObject.getJSONArray("weather").
                                                 getJSONObject(0).getInt("id");

            weatherData.mIconName = updateWeatherIcon(weatherData.mCondtion);

            double kelvToCelcTemp = jsonObject.getJSONObject("main").getDouble("temp") - 273.15;
            int roundedTemp = (int) Math.rint(kelvToCelcTemp);
            weatherData.mTemperature = Integer.toString(roundedTemp);

            double tempPressure = jsonObject.getJSONObject("main").getDouble("pressure");
            int roundedPressure = (int) Math.rint(tempPressure);
            weatherData.mPressure = Integer.toString(roundedPressure);

            double tempHumidity = jsonObject.getJSONObject("main").getDouble("humidity");
            int roundedHumidity = (int) Math.rint(tempHumidity);
            weatherData.mHumidity = Integer.toString(roundedHumidity);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return weatherData;
    }



    // to get the weather image name from the condition:
    private static String updateWeatherIcon(int condition) {

        if (condition >= 0 && condition < 300) {
            return "tstorm1";
        } else if (condition >= 300 && condition < 500) {
            return "light_rain";
        } else if (condition >= 500 && condition < 600) {
            return "shower3";
        } else if (condition >= 600 && condition <= 700) {
            return "snow4";
        } else if (condition >= 701 && condition <= 771) {
            return "fog";
        } else if (condition >= 772 && condition < 800) {
            return "tstorm3";
        } else if (condition == 800) {
            return "sunny";
        } else if (condition >= 801 && condition <= 804) {
            return "cloudy2";
        } else if (condition >= 900 && condition <= 902) {
            return "tstorm3";
        } else if (condition == 903) {
            return "snow5";
        } else if (condition == 904) {
            return "sunny";
        } else if (condition >= 905 && condition <= 1000) {
            return "tstorm3";
        }

        return "dunno";
    }

    // getter methods for temperature, city, and icon name:


    public String getmTemperature() {
        return mTemperature + "°";
    }

    public String getmCity() {
        return mCity;
    }

    public String getmIconName() {
        return mIconName;
    }

    public String getmPressure() {
        return mPressure;
    }

    public String getmHumidity() {
        return mHumidity;
    }
}
