package com.bignerdranch.android.parkmycar;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Nicolas on 07/12/2016.
 */
public class PlaceFetchr {
    private final static String TAG = "PlaceFetchr";

    private byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally{
            connection.disconnect();
        }
    }

    private String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public String getPlace(String url){
        String placeId = null;

        try{
            String jsonString = getUrlString(url);
            //Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);

            placeId = parsePlaces(jsonBody);
        }  catch (IOException ioe){
            Log.e(TAG, "Failed to fetch directions", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }

        return placeId;
    }

    private String parsePlaces(JSONObject jsonBody)throws IOException, JSONException{
        JSONObject results = jsonBody.getJSONArray("results").getJSONObject(0);
        String placeId = results.getString("place_id");
        Log.i(TAG, "place_id: " + placeId);
        return placeId;
    }
}
