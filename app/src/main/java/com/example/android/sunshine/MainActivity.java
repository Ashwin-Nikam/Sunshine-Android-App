package com.example.android.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
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
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements
        ForecastAdapter.ForecastAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<String[]>,
        SharedPreferences.OnSharedPreferenceChangeListener{

    TextView mErrorMessageDisplay;
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
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        mForecastAdapter = new ForecastAdapter(this);

        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mForecastAdapter);
        mRecyclerView.setHasFixedSize(true);

        int loaderId = LOADER_ID;
        getSupportLoaderManager().initLoader(loaderId, null, this);

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

    }

    //----------------------------------------------------------------------------------------------

    private void showErrorMessage(){
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void showWeatherData(){
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public void onClick(String weatherForDay) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, weatherForDay);
        startActivity(intent);
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public Loader<String[]> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<String[]>(this) {

            String[] mWeatherData = null;

            @Override
            protected void onStartLoading() {
                if(mWeatherData != null)
                    deliverResult(mWeatherData);
                else{
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public String[] loadInBackground() {
                try{
                    String location = SunshinePreferences.
                            getPreferredWeatherLocation(MainActivity.this);
                    URL url = NetworkUtils.buildUrl(location);
                    String JSONResponse = NetworkUtils.getResponseFromHttpUrl(url);
                    mWeatherData = OpenWeatherJsonUtils.getSimpleWeatherStringsFromJson(MainActivity.this,
                            JSONResponse);
                    return mWeatherData;
                }catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(String[] data){
                mWeatherData = data;
                super.deliverResult(data);
            }

        };
    }

    @Override
    public void onLoadFinished(Loader<String[]> loader, String[] weatherData) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if(weatherData == null)
            showErrorMessage();
        else{
            mForecastAdapter.setWeatherData(weatherData); //Called setWeatherData() from AsyncTask
            showWeatherData();
        }
    }

    @Override
    public void onLoaderReset(Loader<String[]> loader) {

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
                mForecastAdapter.setWeatherData(null);
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
