package com.bignerdranch.android.parkmycar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class ParkActivity extends AppCompatActivity {

    private static final String TAG = "ParkActivity";
    private static final String EXTRA_CAR_EXISTS = "com.bignerdranch.android.parkmycar.car_exist";
    public static final String EXTRA_PARK_OR_NOT = "com.bignerdranch.android.parkmycar.parking";
    private static final String EXTRA_PARKING_PHOTO = "com.bignerdranch.android.parkmycar.parking_photo";
    private static final String EXTRA_PARKING_NAME = "com.bignerdranch.android.parkmycar.parking_name";
    public static final String EXTRA_PARKING_LEVEL = "com.bignerdranch.android.parkmycar.parking_level";

    private ImageView mImageView;
    private TextView mTextView;
    private Switch mSwitch;
    private Spinner mLevelSelect;
    private Button mParkButton;
    private Button mUnparkButton;

    private int mLevel = 0;
    private boolean park;

    public static Intent newIntent(Context packageContext, boolean carExists, Bitmap mPhoto, String mTitle) {
        Intent i = new Intent(packageContext,ParkActivity.class);
        i.putExtra(EXTRA_CAR_EXISTS,carExists);
        i.putExtra(EXTRA_PARKING_PHOTO, mPhoto);
        i.putExtra(EXTRA_PARKING_NAME,mTitle);
        return i;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park);

        mImageView = (ImageView) findViewById(R.id.place_image);
        if(getIntent().getParcelableExtra(EXTRA_PARKING_PHOTO) != null) {
            mImageView.setImageBitmap((Bitmap) getIntent().getParcelableExtra(EXTRA_PARKING_PHOTO));
        }

        mTextView = (TextView) findViewById(R.id.place_title);
        if(getIntent().getSerializableExtra(EXTRA_PARKING_NAME) != null) {
            mTextView.setText((String) getIntent().getSerializableExtra(EXTRA_PARKING_NAME));
        }

        mLevelSelect = (Spinner) findViewById(R.id.level_select);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.level_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLevelSelect.setAdapter(adapter);
        mLevelSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mLevel = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSwitch = (Switch) findViewById(R.id.ask_level);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked){
                    mLevelSelect.setVisibility(View.INVISIBLE);
                } else{
                    mLevelSelect.setVisibility(View.VISIBLE);
                }
            }
        });

        mParkButton = (Button) findViewById(R.id.button_park);
        mParkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResultPark(RESULT_OK, true, mLevel);
            }
        });

        mUnparkButton = (Button) findViewById(R.id.button_unpark);
        mUnparkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResultPark(RESULT_OK, false, 0);
            }
        });

        boolean carExists = getIntent().getBooleanExtra(EXTRA_CAR_EXISTS,false);
        if(carExists){
            mUnparkButton.setEnabled(true);
            mParkButton.setEnabled(false);
        } else {
            mParkButton.setEnabled(true);
            mUnparkButton.setEnabled(false);
        }
    }

    private void sendResultPark(int resultCode, boolean park, int level){
        Intent intent = new Intent();
        intent.putExtra(EXTRA_PARKING_LEVEL, level);
        intent.putExtra(EXTRA_PARK_OR_NOT, park);
        setResult(resultCode, intent);
        finish();
    }
}
