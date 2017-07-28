package com.example.android.sunshine;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.text.format.DateUtils;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.NotificationUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;

import java.net.URL;

/**
 * Created by ashwin on 7/27/17.
 */

public class SunshineSyncTask {


    synchronized public static void syncWeather(Context context) {

        try {

            URL weatherRequestUrl = NetworkUtils.getUrl(context);
            String JSONWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);
            ContentValues[] weatherValues = OpenWeatherJsonUtils.getWeatherContentValuesFromJson(context, JSONWeatherResponse);

            if(weatherValues != null && weatherValues.length != 0) {
                ContentResolver sunshineContentResolver = context.getContentResolver();

                sunshineContentResolver.delete(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        null,
                        null);

                sunshineContentResolver.bulkInsert(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        weatherValues);

                boolean notificationsEnabled = SunshinePreferences.areNotificationsEnabled(context);
                long timeSinceLastNotification = SunshinePreferences.getEllapsedTimeSinceLastNotification(context);
                boolean oneDayPassedSinceLastNotification = false;

                if(timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
                    oneDayPassedSinceLastNotification = true;
                }

                if(notificationsEnabled && oneDayPassedSinceLastNotification)
                    NotificationUtils.notifyUserOfNewWeather(context);

            }

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

}
