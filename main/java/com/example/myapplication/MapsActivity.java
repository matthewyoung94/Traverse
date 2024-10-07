package com.example.myapplication;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.ArraySet;
import androidx.fragment.app.FragmentActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import androidx.appcompat.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.myapplication.ui.login.LoginActivity;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityMapsBinding;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.google.maps.android.SphericalUtil;
import com.google.android.material.snackbar.Snackbar;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private LocationCallback locationCallback;
    private TextView timeElapsedTextView;


    private TextView distanceTextView;
    private TextView pointsTextView;
    private long startTimeMillis;

    private Marker chosenMarker;

    private Location locationGlobal;

    private int points = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);

        // method to start the app and find location
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        if (isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setTheme(R.style.Theme_MyApplication);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            showSnackbar("No internet connection");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showSnackbar("Location permission is required for this app");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            SupportMapFragment mapFragment = SupportMapFragment.newInstance();
            new GoogleMapOptions().mapId(getResources().getString(R.string.map_id));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.map, mapFragment)
                    .commit();
            mapFragment.getMapAsync(this);
        }


        // method to count the time elapsed and add to text view in xml
        timeElapsedTextView = findViewById(R.id.time_elapsed);
        startTimeMillis = System.currentTimeMillis();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
                final long elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timeElapsedTextView.setText("Time Elapsed: " + elapsedMinutes + " minutes");
                        float[] results = new float[1];
                        if(chosenMarker != null && locationGlobal != null)  {
                            Location.distanceBetween(locationGlobal.getLatitude(), locationGlobal.getLongitude(),
                                    chosenMarker.getPosition().latitude, chosenMarker.getPosition().longitude, results);
                            float distanceKm = results[0] / 1000;
                            String shortDistance = String.format("%.2f km", distanceKm);
                            distanceTextView = findViewById(R.id.distance);
                            distanceTextView.setText("Distance: " + shortDistance);
                            Log.d("Distance", "Distance to marker: " + distanceKm + " km");}
                    }
                });
            }
        }, 0, 1000);

        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_profile) {
                    Log.d("MapsActivity", "Profile button clicked");
                    Intent profileintent = new Intent(MapsActivity.this, ProfileActivity.class);
                    startActivity(profileintent);
                    return true;
                } else if (itemId == R.id.menu_settings) {
                    Log.d("MapsActivity", "Settings button clicked");
                    Intent settingsIntent = new Intent(MapsActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    return true;
                } else if (itemId == R.id.menu_logout) {
                    Log.d("MapsActivity", "Logout button clicked");
                    FirebaseAuth.getInstance().signOut();
                    Intent loginIntent = new Intent(MapsActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginIntent);
                    finish();
                    return true;
                } else {
                    return false;
                }
            }

        });
    }
    //method to clear the main activity when the logout button is pressed
    private void clearSession() {
        SharedPreferences preferences = getSharedPreferences("MY_APP_PREFS", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
    private boolean isDarkModeEnabled() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean("light_dark_toggle", false);
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            public void onMapLoaded() {
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    // location
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
                    locationRequest = LocationRequest.create();
                    locationRequest.setInterval(10000); // updating the location
                    locationRequest.setFastestInterval(5000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    locationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult == null) {
                                return;
                            }
                            locationGlobal = locationResult.getLastLocation();
                            if (chosenMarker != null && locationGlobal != null) {
                                float[] results = new float[1];
                                Location.distanceBetween(locationGlobal.getLatitude(), locationGlobal.getLongitude(),
                                        chosenMarker.getPosition().latitude, chosenMarker.getPosition().longitude, results);
                                float distanceKm = results[0] / 1000;
                                String shortDistance = String.format("%.2f km", distanceKm);
                                distanceTextView = findViewById(R.id.distance);
                                distanceTextView.setText("Distance: " + shortDistance);
                                Log.d("Distance", "Distance to marker: " + distanceKm + " km");
                            }
                        }

                    };

                    fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                showNearbyPlaces(location);
                                locationGlobal = location;
                            }
                        }
                    });
                } else {
                    ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                }
            }

            private void showNearbyPlaces(Location location) {
                String apiKey = "AIzaSyCgl_xTAa2SPIUcEJ1-Jo4ZD29PLwJHZHE";
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                double radius = 3400;
                double minRadius = 0;

                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(userLatLng)
                        .include(SphericalUtil.computeOffset(userLatLng, radius, 0))
                        .include(SphericalUtil.computeOffset(userLatLng, radius, 90))
                        .include(SphericalUtil.computeOffset(userLatLng, radius, 180))
                        .include(SphericalUtil.computeOffset(userLatLng, radius, 270))
                        .build();
                Log.d("RADIUS_VALUE", "Radius: " + radius);
                List<Place.Field> placeFields = Arrays.asList(
                        Place.Field.NAME,
                        Place.Field.ADDRESS,
                        Place.Field.LAT_LNG,
                        Place.Field.TYPES);

                if (!Places.isInitialized()) {
                    Places.initialize(getApplicationContext(), apiKey);
                }

                PlacesClient placesClient = Places.createClient(MapsActivity.this);
                FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);
                Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
                placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                        mMap.clear();
                        if (task.isSuccessful()) {
                            FindCurrentPlaceResponse response = task.getResult();
                            List<Marker> markers = new ArrayList<>();
                            for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                                Place place = placeLikelihood.getPlace();
                                String placeName = place.getName(); // Extract the name of the place
                                Log.d("PlaceName", "Nearby place: " + placeName); // Log the name of the place

                                List<String> placeFields = Arrays.asList(
                                        Place.Field.NAME.name(),
                                        Place.Field.ADDRESS.name(),
                                        Place.Field.LAT_LNG.name(),
                                        Place.Field.TYPES.name());
                                List<Place.Type> types = place.getTypes();

                                if ((types != null)
                                        && types.toString().contains("TOURIST_ATTRACTION")
                                        || types.toString().contains("UNIVERSITY")
                                        || types.toString().contains("TRAIN_STATION")
                                        || types.toString().contains("POINT_OF_INTEREST")) {
                                    LatLng latLng = place.getLatLng();
                                    Log.d("type", "Found place type: " + types.toString());
                                    float distanceMeters = 0;
                                    Log.d("location", "Current location: " + location.getLatitude() + ", " + location.getLongitude());
                                    Log.d("location", "Place location: " + latLng.latitude + ", " + latLng.longitude);
                                    Log.d("distance", "Distance (meters): " + distanceMeters);
                                    Log.d("radius", "Radius (meters): " + radius);

                                    if (latLng != null) {
                                        Location placeLocation = new Location("");
                                        placeLocation.setLatitude(latLng.latitude);
                                        placeLocation.setLongitude(latLng.longitude);
                                        float[] results = new float[1];
                                        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                                                latLng.latitude, latLng.longitude, results);
                                        distanceMeters = results[0];
                                        BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.treasure);
                                        if (distanceMeters <= radius && distanceMeters >= minRadius) {
                                            //&& distanceMeters >= minRadius * 1000
                                            Log.d("marker", "Adding marker at location: " + latLng.latitude + ", " + latLng.longitude);
                                            Marker marker = mMap.addMarker(new MarkerOptions()
                                                    .position(latLng)
                                                    .title(place.getName())
                                                    .snippet(place.getAddress())
                                                    .icon(markerIcon));
                                            markers.add(marker);
                                            Log.d("type", "Found place type: " + types.toString());
                                        }
                                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0);
                                        mMap.moveCamera(cameraUpdate);
                                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                                    }
                                }
                            }
                            if (!markers.isEmpty()) {
                                int randomIndex = new Random().nextInt(markers.size());
                                chosenMarker = markers.get(randomIndex);
                                for (Marker marker : markers) {
                                    if (marker.equals(chosenMarker)) {
                                        marker.setVisible(true);
                                    } else {
                                        marker.setVisible(false);
                                    }
                                }

                                // TEMPLATE CODE FOR CREATING GOOGLE MAPS DIRECTIONS TO THE MARKER. IN THIS INTENT IT SENDS THE USER TO
                                // THE GOOGLE MAPS APP...
                                View settingsView = getLayoutInflater().inflate(R.layout.settings_activity, null);

                                SwitchCompat modeToggle = settingsView.findViewById(R.id.mode_toggle);
                                if (modeToggle.isChecked()) {
                                    LatLng markerPosition = chosenMarker.getPosition();
                                    String uri = "google.navigation:q=" + markerPosition.latitude + "," + markerPosition.longitude;
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                    intent.setPackage("com.google.android.apps.maps");
                                    startActivity(intent);
                                } else {
                                }
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(chosenMarker.getPosition(), 15);
                                mMap.moveCamera(cameraUpdate);
                                locationGlobal = location;
                                float[] results = new float[1];
                                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                                        chosenMarker.getPosition().latitude, chosenMarker.getPosition().longitude, results);
                                float distanceKm = results[0] / 1000;
                                String shortDistance = String.format("%.2f km", distanceKm);
                                distanceTextView = findViewById(R.id.distance);
                                distanceTextView.setText("Distance: " + shortDistance);
                                Log.d("Distance", "Distance to marker: " + distanceKm + " km");
                                if (distanceKm <= 0.1) {
                                    // Update pointsTextView to reflect the earned points
                                    pointsTextView = findViewById(R.id.points);
                                    pointsTextView.setText("Points: 0");
                                    points += 100;
                                    pointsTextView.setText("Points: " + points);
                                    Context context = getApplicationContext();
                                    showSnackbar("100 Points!");

                                    // Play a notification sound
                                    MediaPlayer mediaplayer = MediaPlayer.create(MapsActivity.this, R.raw.notification);
                                    mediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mediaPlayer) {
                                            mediaPlayer.reset();
                                            mediaPlayer.release();
                                        }
                                    });
                                    mediaplayer.start();


                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    DocumentReference userDocRef = db.collection("user").document(userId);

                                    userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (documentSnapshot.exists()) {
                                                long currentPoints = documentSnapshot.getLong("points");
                                                long newPoints = currentPoints + 100;
                                                pointsTextView = findViewById(R.id.points);
                                                pointsTextView.setText("Points: " + newPoints);
                                                // Update instead of set!
                                                String locationName = chosenMarker.getTitle();
                                                Map<String, Object> data = new HashMap<>();
                                                data.put("points", newPoints);
                                                data.put("location_name", "Last Location: " + locationName);
                                                userDocRef.update(data)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d("Firestore", "Data saved successfully");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.e("Firestore", "Error saving data", e);
                                                            }
                                                        });
                                                List<String> visitedLocations = (List<String>) documentSnapshot.get("visited_locations");
                                                if (visitedLocations == null) {
                                                    visitedLocations = new ArrayList<>();
                                                }
                                                visitedLocations.add(locationName);
                                                // Update the user  with the new list of visited locations
                                                userDocRef.update("visited_locations", visitedLocations)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d("Firestore", "Visited location added successfully");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.e("Firestore", "Error adding visited location", e);
                                                            }
                                                        });
                                                // Reload nearby places to potentially find a new marker
                                                showNearbyPlaces(location);
                                            } else {
                                                Log.e("Firestore", "User document does not exist");
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("Firestore", "Error getting user document", e);
                                        }
                                    });
                                }

                                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                    @Override
                                    public boolean onMarkerClick(Marker marker) {
                                        // Create an information card containing the description
                                        String description = marker.getTitle() + "\n" + marker.getSnippet();
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                                        builder.setMessage(description)
                                                .setCancelable(true)
                                                .setPositiveButton("Back To Game", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                        AlertDialog alert = builder.create();
                                        alert.show();
                                        return true;
                                    }
                                });
                            }
                        }
                        ;
                    }
                });
            }
        });
    }
}