package com.example.android.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.android.sunshine.utilities.SunshineDateUtils;

/**
 * Created by ashwin on 7/4/17.
 */

public class WeatherProvider extends ContentProvider {

    WeatherDbHelper mDbHelper;
    SQLiteDatabase mDb;
    public static final int CODE_WEATHER = 100;
    public static final int CODE_WEATHER_WITH_DATE = 101;
    UriMatcher sUriMatcher = buildUriMatcher();

    public UriMatcher buildUriMatcher(){

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_WEATHER, CODE_WEATHER);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_WEATHER+"/#", CODE_WEATHER_WITH_DATE);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDbHelper = new WeatherDbHelper(context);

        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        mDb = mDbHelper.getReadableDatabase();
        Cursor cursor;

        switch (sUriMatcher.match(uri)){
            case CODE_WEATHER:
                cursor = mDb.query(WeatherContract.WeatherEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_WEATHER_WITH_DATE:
                String mSelection = uri.getLastPathSegment();
                String[] mSelectionArgs = {mSelection};
                cursor = mDb.query(WeatherContract.WeatherEntry.TABLE_NAME, projection,
                        WeatherContract.WeatherEntry.COLUMN_DATE+"=?", mSelectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri "+uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        mDb = mDbHelper.getWritableDatabase();
        int numRowsDeleted;
        if(s == null)
            s = "1";

        switch (sUriMatcher.match(uri)){
            case CODE_WEATHER:
                numRowsDeleted = mDb.delete(WeatherContract.WeatherEntry.TABLE_NAME,
                        s, strings);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri "+uri);
        }
        if(numRowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return numRowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        mDb = mDbHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case CODE_WEATHER:
                mDb.beginTransaction();
                int numRowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long date = value.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
                        if (!SunshineDateUtils.isDateNormalized(date))
                            throw new IllegalArgumentException("Date must be normalized and inserted");
                        long num = mDb.insert(WeatherContract.WeatherEntry.TABLE_NAME, null,
                                value);
                        if (num != -1)
                            numRowsInserted++;
                    }

                    mDb.setTransactionSuccessful();

                } finally{
                    mDb.endTransaction();
                }

                if(numRowsInserted > 0)
                    getContext().getContentResolver().notifyChange(uri, null);
                return numRowsInserted;

            default:
                return super.bulkInsert(uri, values);
        }
    }
}
