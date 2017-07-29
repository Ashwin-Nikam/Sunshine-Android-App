package com.example.android.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;

/**
 * Created by ashwin on 7/1/17.
 */

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder>{

    private Cursor mCursor;
    private final ForecastAdapterOnClickHandler mOnClickHandler;
    private final Context mContext;

    public ForecastAdapter(@NonNull Context context, ForecastAdapterOnClickHandler clickHandler){
        mOnClickHandler = clickHandler;
        mContext = context;
    }

    //----------------------------------------------------------------------------------------------

    public void swapCursor(Cursor newCursor){
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    //----------------------------------------------------------------------------------------------

    public interface ForecastAdapterOnClickHandler{
        void onClick(long date);
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.forecast_list_item, parent, false);
        view.setFocusable(true);
        ForecastAdapterViewHolder forecastAdapterViewHolder = new ForecastAdapterViewHolder(view);
        return forecastAdapterViewHolder;
    }

    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder holder, int position) {

        mCursor.moveToPosition(position);

        /****************
         * Weather Icon *
         ****************/

        int weatherId = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
        int weatherImageId;

        weatherImageId = SunshineWeatherUtils
                .getSmallArtResourceIdForWeatherCondition(weatherId);

        holder.iconView.setImageResource(weatherImageId);


        /****************
         * Weather Date *
         ****************/

        long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
        String dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, false);

        holder.dateView.setText(dateString);

        /***********************
         * Weather Description *
         ***********************/

        int weather = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
        String description = SunshineWeatherUtils.getStringForWeatherCondition(mContext, weather);
        String descriptionAlly = mContext.getString(R.string.a11y_forecast, description);

        holder.descriptionView.setText(description);
        holder.descriptionView.setContentDescription(descriptionAlly);

        /**************************
         * High (max) temperature *
         **************************/

        double highTemp = mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);
        String highString = SunshineWeatherUtils.formatTemperature(mContext, highTemp);
        String highA11y = mContext.getString(R.string.a11y_high_temp);

        holder.highTempView.setText(highString);
        holder.highTempView.setContentDescription(highA11y);


        /*************************
         * Low (min) temperature *
         *************************/

        double lowTemp = mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);
        String lowString = SunshineWeatherUtils.formatTemperature(mContext, lowTemp);
        String lowA11y = mContext.getString(R.string.a11y_low_temp);

        holder.lowTempView.setText(lowString);
        holder.lowTempView.setContentDescription(lowA11y);
    }

    @Override
    public int getItemCount() {
        if(mCursor == null)
            return 0;
        else
            return mCursor.getCount();
    }

    //----------------------------------------------------------------------------------------------

    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        final TextView dateView;
        final TextView descriptionView;
        final TextView highTempView;
        final TextView lowTempView;

        final ImageView iconView;

        public ForecastAdapterViewHolder(View itemView){
            super(itemView);

            iconView = (ImageView) itemView.findViewById(R.id.weather_icon);
            dateView = (TextView) itemView.findViewById(R.id.date);
            descriptionView = (TextView) itemView.findViewById(R.id.weather_description);
            highTempView = (TextView) itemView.findViewById(R.id.high_temperature);
            lowTempView = (TextView) itemView.findViewById(R.id.low_temperature);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterposition = getAdapterPosition();
            mCursor.moveToPosition(adapterposition);
            long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
            mOnClickHandler.onClick(dateInMillis);
        }
    }

    //----------------------------------------------------------------------------------------------

}