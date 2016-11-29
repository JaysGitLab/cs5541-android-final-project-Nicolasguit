package com.bignerdranch.android.parkmycar;

import java.util.Date;

/**
 * Created by Nicolas on 22/11/2016.
 */
public class Car {

    private double mLat;
    private double mLon;
    private Date mParkTime;

    public double getLon() {
        return mLon;
    }

    public void setLon(double lon) {
        mLon = lon;
    }

    public double getLat() {
        return mLat;
    }

    public void setLat(double lat) {
        mLat = lat;
    }

    public Date getParkTime() {
        return mParkTime;
    }

    public void setParkTime(Date parkTime) {
        mParkTime = parkTime;
    }
}
