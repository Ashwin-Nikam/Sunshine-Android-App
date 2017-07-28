package com.example.android.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.utilities.FakeDataUtils;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements
        ForecastAdapter.ForecastAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener{


    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_CONDITION_ID = 3;

    public static final String[] MAIN_FORECAST_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    private int mPosition = RecyclerView.NO_POSITION;
    ProgressBar mLoadingIndicator;
    RecyclerView mRecyclerView;
    ForecastAdapter mForecastAdapter;
    private static final int LOADER_ID = 7;
    static boolean PREFERENCE_CHANGED = false;

    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);



        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        mForecastAdapter = new ForecastAdapter(this, this);

        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mForecastAdapter);
        mRecyclerView.setHasFixedSize(true);

        int loaderId = LOADER_ID;
        showLoading();
        getSupportLoaderManager().initLoader(loaderId, null, this);

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        SunshineSyncUtils.initialize(this);

    }

    //----------------------------------------------------------------------------------------------

    private void showLoading(){
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void showWeatherData(){
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public void onClick(long date) {
        Intent weatherDetailIntent = new Intent(MainActivity.this, DetailActivity.class);
        Uri uriForDateClicked = WeatherContract.WeatherEntry.buildWeatherUriWithDate(date);
        weatherDetailIntent.setData(uriForDateClicked);
        startActivity(weatherDetailIntent);
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch(id){
            case LOADER_ID:
                Uri contentUri = WeatherContract.WeatherEntry.CONTENT_URI;
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
                String selection = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();
                return new CursorLoader(this, contentUri, MAIN_FORECAST_PROJECTION,
                        selection, null, sortOrder);
            default:
                throw new RuntimeException("Loader not implemented "+id);
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor weatherData) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mForecastAdapter.swapCursor(weatherData); //Called setWeatherData() from AsyncTask

        if(mPosition == RecyclerView.NO_POSITION)
            mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);

        if(weatherData.getCount() != 0)
            showWeatherData();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    //----------------------------------------------------------------------------------------------

    private void openLocationInMap(){
        String location = SunshinePreferences.getPreferredWeatherLocation(this);
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme("geo")
                    .path("0,0")
                    .query(location)
                    .build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        if(intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
        else
            Log.d("Error", "Couldn't call to "+location+", no" +
                    "apps insalled");
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.forecast_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.item_refresh:
                mForecastAdapter.swapCursor(null);
                getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
                return true;
            case R.id.menu_open_map:
                openLocationInMap();
                return true;
            case R.id.item_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        PREFERENCE_CHANGED = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(PREFERENCE_CHANGED == true){
            PREFERENCE_CHANGED = false;
            getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        }
    }

    //----------------------------------------------------------------------------------------------

}
