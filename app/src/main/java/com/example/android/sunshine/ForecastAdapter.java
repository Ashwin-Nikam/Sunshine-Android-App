package com.example.android.sunshine;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by ashwin on 7/1/17.
 */

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder>{

    private String[] mWeatherData;
    private final ForecastAdapterOnClickHandler mOnClickHandler;

    public ForecastAdapter(ForecastAdapterOnClickHandler clickHandler){
        mOnClickHandler = clickHandler;
    }

    //----------------------------------------------------------------------------------------------

    public void setWeatherData(String[] weatherData){
        mWeatherData = weatherData;
        notifyDataSetChanged();
    }

    //----------------------------------------------------------------------------------------------

    public interface ForecastAdapterOnClickHandler{
        void onClick(String weatherForDay);
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.forecast_list_item, parent, false);
        ForecastAdapterViewHolder forecastAdapterViewHolder = new ForecastAdapterViewHolder(view);
        return forecastAdapterViewHolder;
    }

    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder holder, int position) {
        String weatherForTheDay = mWeatherData[position];
        holder.mWeatherTextView.setText(weatherForTheDay);
    }

    @Override
    public int getItemCount() {
        if(mWeatherData == null)
            return 0;
        else
            return mWeatherData.length;
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
            int position = getAdapterPosition();
            String weatherForDay = mWeatherData[position];
            mOnClickHandler.onClick(weatherForDay);
        }
    }

    //----------------------------------------------------------------------------------------------

}
