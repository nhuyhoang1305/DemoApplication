package com.example.demoapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

public class AddPlace extends AppCompatActivity {

    private final static String TAG = AddPlace.class.getSimpleName();

    private EditText location, type, address, rating, detailRaing;
    private Button submit, cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreated");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        location = findViewById(R.id.location);
        type = findViewById(R.id.type);
        address = findViewById(R.id.address);
        detailRaing = findViewById(R.id.detailRaing);
        submit = findViewById(R.id.submit);
        submit.setBackgroundColor(Color.GREEN);
        cancel = findViewById(R.id.cancel);
        cancel.setBackgroundColor(Color.RED);
        address.setText(getIntent().getStringExtra("location"));

        submit.setOnClickListener( l -> {
            finish();
        });

        cancel.setOnClickListener(l -> {
            finish();
        });

    }
}