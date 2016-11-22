package com.bignerdranch.android.parkmycar;

/**
 * Created by Nicolas on 22/11/2016.
 */
public class Car {

    private double mLat;
    private double mLon;

    public double getLon() {
        return mLon;
    }

    public void setLon(double longitude) {
        mLon = longitude;
    }

    public double getLat() {
        return mLat;
    }

    public void setLat(double latitude) {
        mLat = latitude;
    }
}
