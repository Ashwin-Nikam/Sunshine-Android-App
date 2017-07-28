package com.example.android.sunshine;

import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by ashwin on 7/27/17.
 */

public class SunshineFirebaseJobService extends JobService{

    private AsyncTask<Void, Void, Void> mFetchWeatherTask;

    //----------------------------------------------------------------------------------------------

    @Override
    public boolean onStartJob(final JobParameters job) {

        mFetchWeatherTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Context context = getApplicationContext();
                SunshineSyncTask.syncWeather(context);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(job, false);
            }
        }.execute();

        return true;
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public boolean onStopJob(JobParameters job) {

        if(mFetchWeatherTask != null)
            mFetchWeatherTask.cancel(true);

        return true;
    }

    //----------------------------------------------------------------------------------------------

}
