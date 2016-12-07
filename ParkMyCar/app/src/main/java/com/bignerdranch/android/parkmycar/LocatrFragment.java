package com.bignerdranch.android.parkmycar;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Nicolas on 17/11/2016.
 */
public class LocatrFragment extends Fragment {

    private static final String TAG = "LocatrFragment";
    private static final int REQUEST_CAR_PARK = 0;

    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 2;

    MapView mMapView;
    private GoogleApiClient mClient;
    private GoogleMap mMap;
    private Location mCurrentLocation;
    private Car mCar;
    private List<LatLng> mLatLngs;

    public static LocatrFragment newInstance() {
        return new LocatrFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_locatr, container, false);

        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
            }
        });

        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        getActivity().invalidateOptionsMenu();
                        findLocation();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();

        return v;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        getActivity().invalidateOptionsMenu();
        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        mClient.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_locatr, menu);

        MenuItem searchItem = menu.findItem(R.id.action_locate);
        searchItem.setEnabled(mClient.isConnected());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_locate:
                pinMyCar();
                return true;
            case R.id.action_gps:
                if(mCar != null) getDirections();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void findLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Location Permissions ok");
        } else {
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_ACCESS_FINE_LOCATION);
            }
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                        android.Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_ACCESS_COARSE_LOCATION);
            }
            return;
        }

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setInterval(0);

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                LocationManager mgr = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                if (!mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(getContext(), "GPS is disabled!", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        LocationServices.FusedLocationApi
                .requestLocationUpdates(mClient, request, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.i(TAG, "Got a fix: " + location);
                        mCurrentLocation = location;
                        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 15);
                        mMap.animateCamera(camera);
                    }
                });

        /*PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mClient, null);

        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
                for(PlaceLikelihood likelyPlace : placeLikelihoods){

                }
            }
        });*/
    }

    public void pinMyCar() {
        if (mMap == null || mCurrentLocation == null) {
            return;
        }

        Intent i = ParkActivity.newIntent(getActivity());
        startActivityForResult(i,REQUEST_CAR_PARK);

        Date parkTime = new Date();

        mCar = new Car();
        mCar.setLon(mCurrentLocation.getLongitude());
        mCar.setLat(mCurrentLocation.getLatitude());
        mCar.setParkTime(parkTime);

        LatLng myPoint = new LatLng(
                mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        MarkerOptions myMarker = new MarkerOptions()
                .position(myPoint);

        mMap.clear();
        mMap.addMarker(myMarker);
    }

    private void getDirections(){

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("maps.googleapis.com")
                .appendPath("maps")
                .appendPath("api")
                .appendPath("directions")
                .appendPath("json")
                .appendQueryParameter("origin",mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude())
                .appendQueryParameter("destination", mCar.getLat() + "," + mCar.getLon())
                .appendQueryParameter("mode","walking");

        String url = builder.build().toString();
        Log.i(TAG,"url: " + url);

        DirectionFetcher directionFetcher = new DirectionFetcher(url);
        directionFetcher.execute();
    }

    public class DirectionFetcher extends AsyncTask<Void, Void, List<LatLng>> {
        private String mUrl;

        public DirectionFetcher(String url){
            mUrl = url;
        }

        @Override
        protected List<LatLng> doInBackground(Void... params) {
            return downloadDirections(mUrl);
        }

        @Override
        protected void onPostExecute(List<LatLng> latLngs){
            mLatLngs = latLngs;
            drawRoute(mLatLngs);
        }

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

        private List<LatLng> downloadDirections(String url){
            List<LatLng> latLngs = new ArrayList<LatLng>();

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

        private void drawRoute(List<LatLng> points){
            PolylineOptions lineOptions = new PolylineOptions();
            lineOptions.addAll(points);
            lineOptions.width(10);
            lineOptions.color(Color.BLUE);

            MarkerOptions carMarker = new MarkerOptions()
                    .position(points.get(points.size() - 1));

            mMap.clear();
            mMap.addPolyline(lineOptions);
            mMap.addMarker(carMarker);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_ACCESS_COARSE_LOCATION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    findLocation();
                }
                else{
                    Toast.makeText(getActivity(), "This application can't work without that permission", Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSION_ACCESS_FINE_LOCATION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    findLocation();
                }
                else{
                    Toast.makeText(getActivity(), "This application can't work without that permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default: return;
        }
    }
}
