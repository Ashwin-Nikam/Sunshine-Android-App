package com.example.android.sunshine;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by ashwin on 7/27/17.
 */

public class SunshineSyncIntentService extends IntentService {

    public SunshineSyncIntentService () {
        super("SunshineSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SunshineSyncTask.syncWeather(this);
    }
}
