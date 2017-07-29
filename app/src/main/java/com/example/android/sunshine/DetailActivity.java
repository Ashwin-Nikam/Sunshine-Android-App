package com.example.android.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.databinding.ActivityDetailBinding;
import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;

import org.w3c.dom.Text;

public class DetailActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    private TextView mWeatherDetail;
    private String mWeatherForDay;
    private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";
    public static final String[] WEATHER_DETAIL_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };


    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_HUMIDITY = 3;
    public static final int INDEX_WEATHER_PRESSURE = 4;
    public static final int INDEX_WEATHER_WIND_SPEED = 5;
    public static final int INDEX_WEATHER_DEGREES = 6;
    public static final int INDEX_WEATHER_CONDITION_ID = 7;

    private Uri mUri;

    private ActivityDetailBinding mDetailBinding;

    private static final int ID_DETAIL_LOADER = 111;

    private String mForecastSummary;

    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        mUri = getIntent().getData();
        if(mUri == null)
            throw new NullPointerException("URI for DetailActivity cannot be null");
        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.detail_share_item:
                Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                            .setType("text/plain")
                            .setText(mWeatherForDay + FORECAST_SHARE_HASHTAG).getIntent();
                startActivity(shareIntent);
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.item_settings:
                Intent intent = new Intent(DetailActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return true;
    }

    //----------------------------------------------------------------------------------------------

    public void displayDetail(){
        Intent intent = getIntent();
        if(intent.hasExtra(Intent.EXTRA_TEXT)){
            mWeatherForDay = intent.getStringExtra(Intent.EXTRA_TEXT);
            mWeatherDetail.setText(mWeatherForDay);
        }
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        displayDetail();
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){
            case ID_DETAIL_LOADER:
                return new CursorLoader(this, mUri, WEATHER_DETAIL_PROJECTION,
                        null, null, null);
            default:
                throw new RuntimeException("Loader not implemented "+id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean cursorHasValidData = false;
        if(data != null && data.moveToFirst())
            cursorHasValidData = true;

        if(!cursorHasValidData)
            return;

        /****************
         * Weather Icon *
         ****************/

        int weatherId = data.getInt(INDEX_WEATHER_CONDITION_ID);
        int weatherImageId = SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);
        mDetailBinding.primaryInfo.weatherIcon.setImageResource(weatherImageId);

        /****************
         * Weather Date *
         ****************/

        long localDateMidnightGmt = data.getLong(INDEX_WEATHER_DATE);
        String dateText = SunshineDateUtils.getFriendlyDateString(this, localDateMidnightGmt, true);
        mDetailBinding.primaryInfo.date.setText(dateText);

        /***********************
         * Weather Description *
         ***********************/

        String description = SunshineWeatherUtils.getStringForWeatherCondition(this, weatherId);
        String descriptionA11y = getString(R.string.a11y_forecast, description);
        mDetailBinding.primaryInfo.weatherDescription.setText(description);
        mDetailBinding.primaryInfo.weatherDescription.setContentDescription(descriptionA11y);
        mDetailBinding.primaryInfo.weatherIcon.setContentDescription(descriptionA11y);

        /**************************
         * High (max) temperature *
         **************************/

        double highInCelsius = data.getDouble(INDEX_WEATHER_MAX_TEMP);
        String highString = SunshineWeatherUtils.formatTemperature(this, highInCelsius);
        String highA11y = getString(R.string.a11y_high_temp, highString);
        mDetailBinding.primaryInfo.highTemperature.setText(highString);
        mDetailBinding.primaryInfo.highTemperature.setContentDescription(highA11y);

        /*************************
         * Low (min) temperature *
         *************************/

        double lowInCelsius = data.getDouble(INDEX_WEATHER_MIN_TEMP);
        String lowString = SunshineWeatherUtils.formatTemperature(this, lowInCelsius);
        String lowA11y = getString(R.string.a11y_low_temp, lowString);
        mDetailBinding.primaryInfo.lowTemperature.setText(lowString);
        mDetailBinding.primaryInfo.lowTemperature.setContentDescription(lowA11y);

        /************
         * Humidity *
         ************/

        float humidity = data.getFloat(INDEX_WEATHER_HUMIDITY);
        String humidityString = getString(R.string.format_humidity, humidity);
        String humidityA11y = getString(R.string.a11y_humidity, humidityString);
        mDetailBinding.extraDetails.humidity.setText(humidityString);
        mDetailBinding.extraDetails.humidity.setContentDescription(humidityA11y);
        mDetailBinding.extraDetails.humidityLabel.setContentDescription(humidityA11y);

        /****************************
         * Wind speed and direction *
         ****************************/

        float windSpeed = data.getFloat(INDEX_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(INDEX_WEATHER_DEGREES);
        String windString = SunshineWeatherUtils.getFormattedWind(this, windSpeed, windDirection);
        String windA11y = getString(R.string.a11y_wind, windString);
        mDetailBinding.extraDetails.windMeasurement.setText(windString);
        mDetailBinding.extraDetails.windMeasurement.setContentDescription(windA11y);
        mDetailBinding.extraDetails.windLabel.setContentDescription(windA11y);

        /************
         * Pressure *
         ************/

        float pressure = data.getFloat(INDEX_WEATHER_PRESSURE);
        String pressureString = getString(R.string.format_pressure, pressure);
        String pressureA11y = getString(R.string.a11y_pressure, pressureString);
        mDetailBinding.extraDetails.pressure.setText(pressureString);
        mDetailBinding.extraDetails.pressure.setContentDescription(pressureA11y);
        mDetailBinding.extraDetails.pressureLabel.setContentDescription(pressureA11y);

        mForecastSummary = String.format("%s - %s - %s%S", dateText, description, highString,
                lowString);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    //----------------------------------------------------------------------------------------------

}
