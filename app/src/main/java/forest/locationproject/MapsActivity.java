package forest.locationproject;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    private Map<String, LatLng> MarkerList = new HashMap<>();
    private FirebaseFirestore db;
    private DocumentReference docRef;

    private FusedLocationProviderClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // The database must me initialized.
        FirebaseApp.initializeApp(this);

    }

    /// Store the new values in the database.
    public void updateDatabase() {
        docRef.set(MarkerList)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //System.out.println("Saved successfully");
                        Toast.makeText(MapsActivity.this, "Location saved.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //System.out.println("Failed to save.");
                        Toast.makeText(MapsActivity.this,
                                "Failed to save location.",
                                Toast.LENGTH_SHORT).show();
                        System.out.print(e.toString());
                    }
                });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        db = FirebaseFirestore.getInstance();
        docRef = db.document("All Locations/Locations");
        mMap = googleMap;

        // Load the database
        // Don't ask...
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Map TempMarkerList = documentSnapshot.getData();

                            System.out.println("\n\n\n");

                            Set<Map.Entry<String, LatLng>> set = TempMarkerList.entrySet();
                            for (Map.Entry<String, LatLng> mapEntry : set) {
                                String keyName = mapEntry.getKey();
                                System.out.print("\n" + keyName + ":  ");

                                Object keyValues = mapEntry.getValue();

                                Set<Map.Entry<String, Double>> set2 = ((HashMap) keyValues).entrySet();
                                int counter = 0;
                                double[] tempCoords = {0, 0};
                                for (Map.Entry<String, Double> mapEntry2 : set2) {
                                    tempCoords[counter++] = mapEntry2.getValue();
                                }

                                for (double d : tempCoords)
                                    System.out.print(" " + d);

                                MarkerList.put(keyName, new LatLng(tempCoords[0], tempCoords[1]));

                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(tempCoords[0], tempCoords[1]))
                                        .title(keyName));

                            }

                        } else {
                            Toast.makeText(MapsActivity.this, "Failed to get db.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MapsActivity.this, "Error!",
                                Toast.LENGTH_SHORT).show();
                        System.out.println(e.toString());
                    }
                });
        // End load.


        // Manually add a fixed location, no real reason why
        LatLng timisoara = new LatLng(45.752456, 21.230946);
        mMap.addMarker(new MarkerOptions().position(timisoara).title("Marker in Timisoara"));
        mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        timisoara,
                        14f
                )
        );
        MarkerList.put("Timisoara", timisoara);

        // Location permission check..
        /*
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
        client = LocationServices.getFusedLocationProviderClient(this);
        */

        final Button button = findViewById(R.id.selectButton);
        button.setOnClickListener(new View.OnClickListener() {


            public void onClick(View v) {
                EditText nameBox = findViewById(R.id.nameBox);
                String markerName = String.valueOf(nameBox.getText());

                // Create a random location.
                Random rand = new Random();
                // double randomValue1 = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
                double randomValue1 = 45 + (46 - 45) * rand.nextDouble();
                double randomValue2 = 21 + (22 - 21) * rand.nextDouble();

                LatLng randomLoc = new LatLng(randomValue1, randomValue2);


                // Add the random location with its given name to the map.
                mMap.addMarker(new MarkerOptions().position(randomLoc).title(markerName));
                mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                                randomLoc,
                                14f
                        )
                );

                System.out.println("Lat:" + randomValue1
                        + "  Lon:" + randomValue2
                        + " Marker name:" + markerName);

                MarkerList.put(markerName, randomLoc);

                // Upload the location to the database.
                updateDatabase();

                // Get location, not working.
/*
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            System.out.println("LOCATION: " + location.toString());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MapsActivity.this, "GPS Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
                */
            }
        });

    }
}
