package com.example.android.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
        String dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, false);

        int weather = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
        String description = SunshineWeatherUtils.getStringForWeatherCondition(mContext, weather);

        double highTemp = mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);
        double lowTemp = mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);
        String highAndLowTemperature = SunshineWeatherUtils.formatHighLows(mContext, highTemp, lowTemp);

        String summary = dateString + " - " + description + " - " + highAndLowTemperature;
        holder.mWeatherTextView.setText(summary);
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

        public final TextView mWeatherTextView;

        public ForecastAdapterViewHolder(View itemView){
            super(itemView);
            mWeatherTextView = (TextView) itemView.findViewById(R.id.tv_weather_data);
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
