package com.example.android.sunshine;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    private TextView mWeatherDetail;
    private String mWeatherForDay;
    private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";

    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mWeatherDetail = (TextView) findViewById(R.id.tv_detail_activity);
        displayDetail();
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

}
