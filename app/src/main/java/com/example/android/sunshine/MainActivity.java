package com.example.android.sunshine;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements ForecastAdapter.ForecastAdapterOnClickHandler{

    TextView mErrorMessageDisplay;
    ProgressBar mLoadingIndicator;
    RecyclerView mRecyclerView;
    ForecastAdapter mForecastAdapter;

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

        loadWeatherData();
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

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(String... strings) {

            String location = strings[0];
            URL url = NetworkUtils.buildUrl(location);
            try {
                String jSONResponse = NetworkUtils.getResponseFromHttpUrl(url);
                String[] mWeatherData = OpenWeatherJsonUtils.
                        getSimpleWeatherStringsFromJson(MainActivity.this, jSONResponse);
                return mWeatherData;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] weatherData) {
            super.onPostExecute(weatherData);

            if(weatherData == null)
                showErrorMessage();
            else{
                mForecastAdapter.setWeatherData(weatherData); //Called setWeatherData() from AsyncTask
                showWeatherData();
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    private void loadWeatherData(){
        String location = SunshinePreferences.getPreferredWeatherLocation(this);
        new FetchWeatherTask().execute(location);
    }

    //----------------------------------------------------------------------------------------------

    private void openLocationInMap(){
        String location = "1600 Amphitheatre Parkway, CA";
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
                loadWeatherData();
                return true;
            case R.id.menu_open_map:
                openLocationInMap();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //----------------------------------------------------------------------------------------------

}
