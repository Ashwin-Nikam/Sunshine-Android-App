package com.example.android.sunshine;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.example.android.sunshine.data.WeatherContract;

/**
 * Created by ashwin on 7/27/17.
 */

public class SunshineSyncUtils {

    private static boolean sInitialized;

    //----------------------------------------------------------------------------------------------

    synchronized public static void initialize(@NonNull final Context context) {
        if(sInitialized)
            return;
        sInitialized = true;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
                String[] projectionColumns = {WeatherContract.WeatherEntry._ID};
                String selectionStatement = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();
                Cursor cursor = context.getContentResolver().query(
                        forecastQueryUri,
                        projectionColumns,
                        selectionStatement,
                        null,
                        null);

                if(cursor == null || cursor.getCount() == 0)
                    startImmediateSync(context);

                cursor.close();

                return null;
            }
        }.execute();

    }

    //----------------------------------------------------------------------------------------------

    public static void startImmediateSync(@NonNull final Context context) {
        Intent intentToSyncImmediately = new Intent(context, SunshineSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }

    //----------------------------------------------------------------------------------------------

}
