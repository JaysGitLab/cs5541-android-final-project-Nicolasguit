package com.bignerdranch.android.parkmycar;

import android.*;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;

/**
 * Created by Nicolas on 17/11/2016.
 */
public class LocatrFragment extends SupportMapFragment {

    private static final String TAG = "LocatrFragment";
    private static final int REQUEST_CAR_PARK = 0;

    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 2;

    private GoogleApiClient mClient;
    private GoogleMap mMap;
    private Location mCurrentLocation;
    private Car mCar;

    public static LocatrFragment newInstance() {
        return new LocatrFragment();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
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

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
            }
        });
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void findLocation(){
        if (ActivityCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Location Permissions ok");
        }
        else {
            if(ActivityCompat.checkSelfPermission(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(getActivity(), new String[] {
                        android.Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSION_ACCESS_FINE_LOCATION);
            }
            if(ActivityCompat.checkSelfPermission(getActivity(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(getActivity(), new String[] {
                        android.Manifest.permission.ACCESS_COARSE_LOCATION},MY_PERMISSION_ACCESS_COARSE_LOCATION);
            }
            return;
        }

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setInterval(0);

        mMap.setMyLocationEnabled(true);

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


    }

    public void pinMyCar(){
        if(mMap == null || mCurrentLocation == null){
            return;
        }

        Intent i = ParkActivity.newIntent(getContext());
        startActivityForResult(i,REQUEST_CAR_PARK);

        Date parkTime = new Date();

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
