package com.example.android.sunshine;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    TextView mWeatherTextView;
    TextView mErrorMessageDisplay;
    ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        mWeatherTextView = (TextView) findViewById(R.id.tv_weather_data);
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        loadWeatherData();
    }

    //----------------------------------------------------------------------------------------------

    private void showErrorMessage(){
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mWeatherTextView.setVisibility(View.INVISIBLE);
    }

    private void showWeatherData(){
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mWeatherTextView.setVisibility(View.VISIBLE);
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
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] s) {
            super.onPostExecute(s);

            if(s == null)
                showErrorMessage();

            for(String weather : s)
                mWeatherTextView.append(weather+"\n\n\n");

            showWeatherData();
        }
    }

    //----------------------------------------------------------------------------------------------

    private void loadWeatherData(){
        String location = SunshinePreferences.getPreferredWeatherLocation(this);
        new FetchWeatherTask().execute(location);
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
                mWeatherTextView.setText("");
                loadWeatherData();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //----------------------------------------------------------------------------------------------

}
