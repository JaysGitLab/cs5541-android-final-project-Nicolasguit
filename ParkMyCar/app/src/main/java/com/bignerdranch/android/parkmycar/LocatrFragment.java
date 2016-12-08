package com.bignerdranch.android.parkmycar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

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
    private Bitmap mParkingPhoto;
    private String mParkingName;
    private boolean backToCar;

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
                if(isConnectedToInternet(getContext())) pinMyCar();
                else {
                    Toast.makeText(getContext(), "No internet connection!", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_gps:
                if(mCar != null){
                    backToCar = true;
                    getDirections();
                }
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
        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        request.setInterval(1000);
        request.setFastestInterval(1000);

        checkGPS();


        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                checkGPS();
                return false;
            }
        });

        LocationServices.FusedLocationApi
                .requestLocationUpdates(mClient, request, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.i(TAG, "Got a fix: " + location);
                        mCurrentLocation = location;
                        CameraUpdate camera = CameraUpdateFactory.newLatLng(
                                new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                        mMap.animateCamera(camera);
                        if (backToCar) getDirections();
                    }
                });
    }

    private void checkGPS(){
        LocationManager mgr = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getContext(), "GPS is disabled!", Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public void pinMyCar() {
        if (mMap == null || mCurrentLocation == null) {
            return;
        }

        getPlace(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude());

        Intent intent = ParkActivity.newIntent(getActivity(), mCar, mParkingPhoto, mParkingName);
        startActivityForResult(intent, REQUEST_CAR_PARK);
    }

    private void getPlace(double lat, double lon){

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("maps.googleapis.com")
                .appendPath("maps")
                .appendPath("api")
                .appendPath("geocode")
                .appendPath("json")
                .appendQueryParameter("latlng",lat + "," + lon);

        String url = builder.build().toString();
        Log.i(TAG,"url: " + url);

        SearchPlace placeFetcher = new SearchPlace(url);
        placeFetcher.execute();
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

        SearchDirections directionFetcher = new SearchDirections(url);
        directionFetcher.execute();
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

    public class SearchDirections extends AsyncTask<Void, Void, Void> {
        private String mUrl;

        public SearchDirections(String url){
            mUrl = url;
        }

        @Override
        protected Void doInBackground(Void... params) {
            DirectionsFetchr directionsFetchr = new DirectionsFetchr();
            mLatLngs = directionsFetchr.downloadDirections(mUrl);
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            drawRoute(mLatLngs);
        }
    }

    public class SearchPlace extends AsyncTask<Void, Void, Void> {
        private String mUrl;
        private String mPlaceId;

        private SearchPlace(String url){mUrl = url;}

        @Override
        protected Void doInBackground(Void... params){
            PlaceFetchr placeFetchr = new PlaceFetchr();
            mPlaceId = placeFetchr.getPlace(mUrl);
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            getPlaceName(mPlaceId);
            getPlacePhoto(mPlaceId);
        }

        private void getPlaceName(String placeId){
            Log.i(TAG,"Looking for a place");
            Places.GeoDataApi.getPlaceById(mClient, placeId)
                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(@NonNull PlaceBuffer result) {
                            if(result.getStatus().isSuccess() && result.getCount() > 0){
                                Place myPlace = result.get(0);
                                mParkingName = myPlace.getName().toString();
                                Log.i(TAG, "Place found: " + myPlace.getName());
                            }else {
                                Log.e(TAG, "Place not found");
                            }
                            result.release();
                        }
                    });
        }

        private void getPlacePhoto(String placeId){
            Log.i(TAG, "Looking for a picture");
            Places.GeoDataApi.getPlacePhotos(mClient, placeId)
                    .setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {
                        @Override
                        public void onResult(@NonNull PlacePhotoMetadataResult photos) {
                            if(!photos.getStatus().isSuccess()){
                                Log.e(TAG,"Photo not found");
                                return;
                            }
                            PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                            if (photoMetadataBuffer.getCount() > 0) {
                                // Display the first bitmap in an ImageView in the size of the view
                                photoMetadataBuffer.get(0).getPhoto(mClient)
                                        .setResultCallback(new ResultCallback<PlacePhotoResult>() {
                                            @Override
                                            public void onResult(PlacePhotoResult placePhotoResult) {
                                                if (!placePhotoResult.getStatus().isSuccess()) {
                                                    Log.e(TAG,"Photo not found");
                                                    return;
                                                }
                                                Log.i(TAG, "Photo found");
                                                mParkingPhoto = placePhotoResult.getBitmap();
                                            }
                                        });
                            }
                            photoMetadataBuffer.release();
                        }
                    });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {{
        if (requestCode == REQUEST_CAR_PARK){
            if(resultCode == Activity.RESULT_OK){
                if(data.getBooleanExtra(ParkActivity.EXTRA_PARK_OR_NOT,false)){
                    Date parkTime = new Date();

                    mCar = new Car();
                    mCar.setLon(mCurrentLocation.getLongitude());
                    mCar.setLat(mCurrentLocation.getLatitude());
                    mCar.setParkTime(parkTime);
                    mCar.setLevel(data.getIntExtra(ParkActivity.EXTRA_PARKING_LEVEL,0));

                    LatLng myPoint = new LatLng(
                            mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

                    MarkerOptions myMarker = new MarkerOptions()
                            .position(myPoint);

                    mMap.clear();
                    mMap.addMarker(myMarker);
                }else {
                    mMap.clear();
                    backToCar = false;
                    mCar = null;
                }
            }
        }
    }}

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
