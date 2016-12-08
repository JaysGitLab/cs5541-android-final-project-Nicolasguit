package com.bignerdranch.android.parkmycar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Nicolas on 22/11/2016.
 */
public class Car implements Serializable {

    private double mLat;
    private double mLon;
    private Date mParkTime;
    private int mLevel;

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

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int level) {
        this.mLevel = level;
    }

    public JSONObject getCarJson(){
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("lon", mLon);
            jsonObject.put("lat", mLat);
            jsonObject.put("time",mParkTime);
            jsonObject.put("mLevel", mLevel);
        } catch (JSONException je){
            je.printStackTrace();
        }
        return jsonObject;
    }
}
