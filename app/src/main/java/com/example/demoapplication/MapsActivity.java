package com.example.demoapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location currentLocation;
    private Marker userMarker;
    private MaterialSearchBar materialSearchBar;
    private View mapView;
    //private Button btnFind;
    private RippleBackground rippleBg;

    private final float DEFAULT_ZOOM = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        init();
        initView();

        materialSearchBar = findViewById(R.id.searchBar);
        //btnFind = findViewById(R.id.btn_find);
        rippleBg = findViewById(R.id.ripple_bg);


        Places.initialize(MapsActivity.this, "AIzaSyAqRZmmAqpUYKUz6FxIs2cAcP669zkIvp4");
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(), true, null, true);
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
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                Log.d("mytag", token.toString());
                Log.d("mytag", s.toString());

                placesClient.findAutocompletePredictions(predictionsRequest).addOnSuccessListener((response) -> {
                    List<String> suggestionsList = new ArrayList<>();
                    predictionList = response.getAutocompletePredictions();
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                        Log.i("mytag", prediction.getPlaceId());
                        suggestionsList.add(prediction.getFullText(null).toString());
                    }
                    materialSearchBar.updateLastSuggestions(suggestionsList);
                    if (!materialSearchBar.isSuggestionsVisible()) {
                        materialSearchBar.showSuggestionsList();
                    }
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e("mytag", "Place not found: " + apiException.getStatusCode());
                    }
                });

//                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
//                    @Override
//                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
//                        if (task.isSuccessful()) {
//                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
//                            if (predictionsResponse != null) {
//                                predictionList = predictionsResponse.getAutocompletePredictions();
//                                List<String> suggestionsList = new ArrayList<>();
//                                for (int i = 0; i < predictionList.size(); i++) {
//                                    AutocompletePrediction prediction = predictionList.get(i);
//                                    suggestionsList.add(prediction.getFullText(null).toString());
//                                }
//                                materialSearchBar.updateLastSuggestions(suggestionsList);
//                                if (!materialSearchBar.isSuggestionsVisible()) {
//                                    materialSearchBar.showSuggestionsList();
//                                }
//                            }
//                        } else {
//                            Log.i("mytag", "prediction fetching task unsuccessful");
//                        }
//                    }
//                });
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });

        materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                Log.d("mytag", position + "");
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                final String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Collections.singletonList(Place.Field.LAT_LNG);

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(fetchPlaceResponse -> {
                    Place place = fetchPlaceResponse.getPlace();
                    Log.d("mytag", "Place found: " + place.getName() + "Place LatLng: " + place.getLatLng());
                    LatLng latLngOfPlace = place.getLatLng();
                    if (latLngOfPlace != null) {
                        userMarker = mMap.addMarker(new MarkerOptions().position(latLngOfPlace).title("demo"));
                        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLngOfPlace, 17);
                        mMap.animateCamera(yourLocation);
                    }
                }).addOnFailureListener(e -> {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        apiException.printStackTrace();
                        int statusCode = apiException.getStatusCode();
                        Log.i("mytag", "place not found: " + e.getMessage());
                        Log.i("mytag", "status code: " + statusCode);
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });
//        btnFind.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                LatLng currentMarkerLocation = mMap.getCameraPosition().target;
//                //rippleBg.startRippleAnimation();
//
//
//            }
//        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnInfoWindowClickListener(marker -> {
            //
            Toast.makeText(this, "You clicked " + marker.getTitle(), Toast.LENGTH_SHORT).show();
        });

//        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
//            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
//            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//            layoutParams.setMargins(0, 0, 40, 180);
//        }
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
//
//        SettingsClient settingsClient = LocationServices.getSettingsClient(MapsActivity.this);
//        com.google.android.gms.tasks.Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
//
//        ((com.google.android.gms.tasks.Task) task).addOnSuccessListener(MapsActivity.this, (OnSuccessListener<LocationSettingsResponse>) locationSettingsResponse -> getDeviceLocation());
//
//        ((com.google.android.gms.tasks.Task) task).addOnFailureListener(MapsActivity.this, e -> {
//            if (e instanceof ResolvableApiException) {
//                ResolvableApiException resolvable = (ResolvableApiException) e;
//                try {
//                    resolvable.startResolutionForResult(MapsActivity.this, 51);
//                } catch (IntentSender.SendIntentException e1) {
//                    e1.printStackTrace();
//                }
//            }
//        });

        mMap.setOnMyLocationButtonClickListener(() -> {
            if (materialSearchBar.isSuggestionsVisible())
                materialSearchBar.clearSuggestions();
            if (materialSearchBar.isSearchOpened())
                materialSearchBar.closeSearch();
            return false;
        });


    }

    private void init() {
        buildLocationRequest();
        buildLocationCallback();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
    }

    private void initView(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(userLatLng, 17);
        mMap.animateCamera(yourLocation);
    }

    private void buildLocationRequest(){
        Log.d("mytag", "buildLocationRequest: created!!");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setSmallestDisplacement(10f);
    }
}