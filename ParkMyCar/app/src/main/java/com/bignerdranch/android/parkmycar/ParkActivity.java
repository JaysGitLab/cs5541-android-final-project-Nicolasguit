package com.bignerdranch.android.parkmycar;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ParkActivity extends AppCompatActivity {

    private static final String TAG = "ParkActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park);
    }

    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext,ParkActivity.class);
        return i;
    }
}
