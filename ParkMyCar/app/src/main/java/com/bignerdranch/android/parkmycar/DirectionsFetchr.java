package com.bignerdranch.android.parkmycar;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nicolas on 07/12/2016.
 */
public class DirectionsFetchr {
    private final static String TAG = "DirectionsFetchr";

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

    public List<LatLng> downloadDirections(String url){
        List<LatLng> latLngs = new ArrayList<>();

        try{
            String jsonString = getUrlString(url);
            //Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);

            latLngs = parseDirections(jsonBody);
        } catch (IOException ioe){
            Log.e(TAG, "Failed to fetch directions", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }

        return latLngs;
    }

    private List<LatLng> parseDirections(JSONObject jsonBody) throws IOException, JSONException{
        JSONObject routes = jsonBody.getJSONArray("routes").getJSONObject(0);
        JSONObject polyline = routes.getJSONObject("overview_polyline");
        String overviewPolyline = polyline.getString("points");
        Log.i(TAG, "Polyline: " + overviewPolyline);
        return PolyUtil.decode(overviewPolyline);
    }
}
