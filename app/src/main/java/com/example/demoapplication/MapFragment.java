package com.example.demoapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private final static String TAG = MapFragment.class.getSimpleName();

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;
    private Marker current;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location currentLocation;
    private Marker userMarker;
    private MaterialSearchBar materialSearchBar;
    private View mapView;
    private final float DEFAULT_ZOOM = 15;

    List<String> suggestionsList;
    List<Pair<Double, Double>> latlong;

    private FileParse fp;

    private ArrayList<String[]> infos = new ArrayList<String[]>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Initialize view
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        init();
        initView();

        materialSearchBar = v.findViewById(R.id.searchBar);

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                getActivity().startSearch(text.toString(), true, null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                    //opening or closing a navigation drawer
                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    materialSearchBar.closeSearch();
                }
            }
        });

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable editable) {
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> geoResults = new ArrayList<>();
                try {
                    Log.d(TAG, editable.toString());
                    geoResults = geocoder.getFromLocationName(editable.toString(), 10);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                suggestionsList = new ArrayList<>();
                latlong = new ArrayList<>();
                for (Address geo : geoResults){
                    Log.d(TAG, geo.getAddressLine(0)
                    + "\n" + geo.getLatitude()
                    + "\n" + geo.getLongitude());
                    suggestionsList.add(geo.getAddressLine(0));
                    latlong.add(new Pair<>(geo.getLatitude(), geo.getLongitude()));
                }
                materialSearchBar.updateLastSuggestions(suggestionsList);
                if (!materialSearchBar.isSuggestionsVisible()) {
                    materialSearchBar.clearSuggestions();
                }
            }

        });

        materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                Log.d(TAG, position + "");
                //AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                // Initialize marker options
                MarkerOptions markerOptions = new MarkerOptions();
                // Set position for marker
                markerOptions.position(new LatLng(latlong.get(position).first, latlong.get(position).second));
                //Set title of marker
                markerOptions.title(suggestion);
                // Remove all marker
                //mMap.clear();
                //Animating to zoom the marker
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latlong.get(position).first, latlong.get(position).second), DEFAULT_ZOOM));
                // Add marker on map
                current = mMap.addMarker(markerOptions);

            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });

        return v;
    }

    private void initView() {
        //Initialize map fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        //Async map
        supportMapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        addMarker();
        // When map is loaded
        mMap.setOnMapClickListener(latLng -> {
            // When clicked on map
            // Initialize marker options
            MarkerOptions markerOptions = new MarkerOptions();
            // Set position for marker
            markerOptions.position(latLng);
            //Set title of marker
            try {
                markerOptions.title(getAddress(latLng.latitude, latLng.longitude));
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Remove all marker
            //mMap.clear();
            //Animating to zoom the marker
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
            // Add marker on map
            current = mMap.addMarker(markerOptions);

        });

        mMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            if (marker.equals(current)) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                builder1.setMessage("Địa điểm: " + marker.getTitle() + "\nBạn có muốn thêm địa điểm này?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        (dialog, id) -> {
                            Intent intent = new Intent(getContext(), AddPlace.class);
                            intent.putExtra("location", marker.getTitle());
                            Bundle args = new Bundle();
                            args.putParcelable("position", marker.getPosition());
                            intent.putExtra("position", args);
                            startActivity(intent);
                            try {
                                readFile();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        (dialog, id) -> current.remove());

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
            else {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                builder1.setMessage("Địa điểm: " + marker.getTitle()
                        + "\n" + marker.getSnippet()
                        + "\nBạn có muốn đánh giá địa điểm này?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        (dialog, id) -> {
                            Intent intent = new Intent(getContext(), Rating.class);
                            intent.putExtra("location", marker.getTitle());
                            intent.putExtra("rating", marker.getSnippet());
                            startActivity(intent);
                           // dialog.cancel();
                        });

                builder1.setNegativeButton(
                        "No",
                        (dialog, id) -> dialog.cancel());

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
            return true;
        });
    };

    @NotNull
    private String getAddress(double lat, double longit) throws IOException {

        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

        List<Address> addresses = geocoder.getFromLocation(lat, longit, 2);
        return addresses.get(0).getAddressLine(0);
    }

    private void addMarker() {
        for (int i = 1; i < infos.size(); ++i){
            String[] params = infos.get(i);
            LatLng latLng = new LatLng(Double.parseDouble(params[1]), Double.parseDouble(params[2]));
            params[0] = params[0] != null ? params[0] : "?";
            params[3] = params[3] != null ? params[3] : "?";
            params[4] = params[4] != null ? params[4] : "?";
            params[5] = params[5] != null ? params[5] : "?";

            String snippet = "Loại hình hỗ trợ: " + params[3] + "\n" +
                    "Rating: " + params[4] + "\n" +
                    "Đánh giá chi tiết: " + params[5];
            //Log.d(TAG, snippet);
            mMap.addMarker(new MarkerOptions().position(latLng).title(params[0]).snippet(snippet));
        }
    }

    private void init() {
        buildLocationRequest();
        buildLocationCallback();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        try {
            readFile();
            infos = fp.infos;
        } catch (java.lang.Exception e){
            Log.e(TAG, "Failed to parse the file" + e);
        }

    }

    private void buildLocationRequest(){
        Log.d("mytag", "buildLocationRequest: created!!");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setSmallestDisplacement(10f);
    }

    private void buildLocationCallback() {
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
                addMarkerAndMoveCamera(locationResult.getLastLocation());
            }
        };
    }

    private void addMarkerAndMoveCamera(Location lastLocation){
        Log.d("mytag", "addMarkerAndMoveCamera: called!!");
        if (userMarker != null) {
            userMarker.remove();
        }

        LatLng userLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        userMarker = mMap.addMarker(new MarkerOptions().position(userLatLng).title("demo"));
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(userLatLng, DEFAULT_ZOOM);
        mMap.animateCamera(yourLocation);
    }

    public void readFile() throws java.lang.Exception {
        InputStream is = getResources().openRawResource(R.raw.location);
        File Dir = getContext().getDir("location", Context.MODE_PRIVATE);
        File mFile = new File(Dir, "location.txt");
        FileOutputStream os = new FileOutputStream(mFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        is.close();
        os.close();
        fp = new FileParse(mFile.getAbsolutePath());

    }


}