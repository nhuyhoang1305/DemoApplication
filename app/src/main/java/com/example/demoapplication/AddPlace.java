package com.example.demoapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AddPlace extends AppCompatActivity {

    private final static String TAG = AddPlace.class.getSimpleName();

    private EditText location, type, address, detailRating;
    private Button submit, cancel;
    private LatLng latLng;
    private Geocoder mGeocoder;
    private RatingBar rating;

    private String mLocation, mType, mAddress, mRating, mDetailRating;
    private double mLat, mLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreated");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        location = findViewById(R.id.location);
        type = findViewById(R.id.type);
        address = findViewById(R.id.address);
        rating = findViewById(R.id.rating);
        detailRating = findViewById(R.id.detailRaing);
        submit = findViewById(R.id.submit);
        submit.setBackgroundColor(Color.GREEN);
        cancel = findViewById(R.id.cancel);
        cancel.setBackgroundColor(Color.RED);

        if (getIntent().getExtras() != null){
            address.setText(getIntent().getStringExtra("location"));
            address.setFocusable(false);
            Bundle bundle = getIntent().getParcelableExtra("position");
            latLng = bundle.getParcelable("position");

            mLat = latLng.latitude;
            mLong = latLng.longitude;
        }
        else {
            mGeocoder = new Geocoder(this);
        }



        submit.setOnClickListener( l -> {

            if (mGeocoder != null){
                mAddress = address.getText().toString();
                if (mAddress.length() != 0){
                    List<Address> results = new ArrayList<>();
                    try {
                        results = mGeocoder.getFromLocationName(mAddress, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mLat = results.get(0).getLatitude();
                    mLong = results.get(0).getLongitude();
                    mLocation = location.getText().toString().length() != 0 ? location.getText().toString() : "?";
                    mRating = (rating.getRating() + "").length() != 0 ? (rating.getRating() + "") : "?";
                    mType = type.getText().toString().length() != 0 ? type.getText().toString() : "?";
                    mDetailRating = detailRating.getText().toString().length() != 0 ? detailRating.getText().toString() : "?";
                    try {
                        writeFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finish();
                }
                else {
                    address.setError("Thiếu địa chỉ!!!");
                }
            }
            else {
                mLocation = location.getText().toString().length() != 0 ? location.getText().toString() : "?";
                mRating = (rating.getRating() + "").length() != 0 ? (rating.getRating() + "") : "?";
                mType = type.getText().toString().length() != 0 ? type.getText().toString() : "?";
                mDetailRating = detailRating.getText().toString().length() != 0 ? detailRating.getText().toString() : "?";
                try {
                    writeFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finish();
            }


        });

        cancel.setOnClickListener(l -> {
            finish();
        });

    }

    public void writeFile() throws java.lang.Exception {
        InputStream is = getResources().openRawResource(R.raw.location);
        File Dir = getDir("location", Context.MODE_PRIVATE);
        File mFile = new File(Dir, "location.txt");

        FileOutputStream os = new FileOutputStream(mFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        is.close();
        os.close();

        FileWriter f = new FileWriter(mFile);

        String outInfo = mLocation + "|" + mLat + "|" + mLong + "|" + mType + "|"
                + mRating + "|"  + mDetailRating;

        f.write(outInfo);
        f.close();

    }
}