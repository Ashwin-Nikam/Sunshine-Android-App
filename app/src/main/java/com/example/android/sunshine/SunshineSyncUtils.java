package com.example.android.sunshine;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

/**
 * Created by ashwin on 7/27/17.
 */

public class SunshineSyncUtils {

    public static void startImmediateSync(@NonNull final Context context) {
        Intent intentToSyncImmediately = new Intent(context, SunshineSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }

}
