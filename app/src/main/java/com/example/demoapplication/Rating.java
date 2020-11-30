package com.example.demoapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

public class Rating extends AppCompatActivity {

    private RatingBar mRatingBar;
    private EditText mDetailRating;
    private Button mSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        mRatingBar = findViewById(R.id.rating);
        mDetailRating = findViewById(R.id.writeRating);
        mSubmit = findViewById(R.id.submit);

        mSubmit.setOnClickListener(l -> {
            finish();
        });

    }
}