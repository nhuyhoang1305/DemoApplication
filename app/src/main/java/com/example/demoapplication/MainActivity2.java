package com.example.demoapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.demoapplication.ui.login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity2 extends AppCompatActivity implements OnNavigationItemSelectedListener{

    private ActionBar toolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        toolBar = getSupportActionBar();
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_add_places, R.id.navigation_login)
                .build();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        NavigationUI.setupWithNavController(navView, navController);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        toolBar.setTitle("MapDemo");




    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    toolBar.setTitle("MapDemo");
                    startActivity(new Intent(MainActivity2.this, MainActivity.class));
                    return true;
                case R.id.navigation_add_places:
                    toolBar.setTitle("Add Place");
                    return true;
                case R.id.navigation_login:
                    toolBar.setTitle("Login");
                    return true;
            }
            return false;
        }
    };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        //Log.d("MainActivity2", id + "");
        if (id == R.id.navigation_home){
           // Log.d("MainActivity2", id + "");
            startActivity(new Intent(MainActivity2.this, MainActivity.class));
            //finish();
            return true;
        }

        return false;
    }
}