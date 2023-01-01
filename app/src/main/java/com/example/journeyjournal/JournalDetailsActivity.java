package com.example.journeyjournal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class JournalDetailsActivity extends AppCompatActivity {

    AppCompatImageView detailImage;
    AppCompatTextView detailTitle, detailDate, detailLocation, detailDescription;
    AppCompatImageButton backButton, deleteButton, mapButton;
    ExtendedFloatingActionButton updateButton;
    SupportMapFragment mapFragmentSupport;
    FusedLocationProviderClient locationProviderClient;
    Double latitudeView, longitudeView;
    Dialog mapsDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_details);

        //finding elements
        detailImage = findViewById(R.id.detailImage);
        detailTitle = findViewById(R.id.detailTitle);
        detailDate = findViewById(R.id.detailDate);
        detailLocation = findViewById(R.id.detailLocation);
        detailDescription = findViewById(R.id.detailDescription);
        backButton = findViewById(R.id.backButton);
        deleteButton = findViewById(R.id.deleteButton);
        updateButton = findViewById(R.id.updateButton);
        mapButton = findViewById(R.id.mapDisplayButton);


        //back button redirecting back to dashboard
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(JournalDetailsActivity.this, DashboardActivity.class)
                        .putExtra("image", getIntent().getStringExtra("image"))
                        .putExtra("title", detailTitle.getText().toString())
                        .putExtra("date", detailDate.getText().toString())
                        .putExtra("location", detailLocation.getText().toString())
                        .putExtra("description", detailDescription.getText().toString())
                        .putExtra("latitude", getIntent().getStringExtra("latitude"))
                        .putExtra("longitude", getIntent().getStringExtra("longitude"))
                        .putExtra("position", getIntent().getStringExtra("position"))
                        .putExtra("uid", DashboardActivity.uid)
                );
                finish();
            }
        });


        //calling method to delete data
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteData();
            }
        });

        //opening maps to view location
        //Location implementation
        mapsDialog = new Dialog(this);
        mapsDialog.setContentView(R.layout.map_layout);
        mapsDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mapsDialog.setCancelable(true);

        latitudeView = Double.valueOf(getIntent().getStringExtra("latitude"));
        longitudeView = Double.valueOf(getIntent().getStringExtra("longitude"));
        mapButton = findViewById(R.id.mapDisplayButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapFragmentSupport = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapsFragment);
                locationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

                Dexter.withContext(getApplicationContext()).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        getLocation();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

                mapsDialog.show();
            }
        });


        //setting all the values to the elements
        Glide.with(detailImage.getContext()).load(getIntent().getStringExtra("image")).into(detailImage);
        detailTitle.setText(getIntent().getStringExtra("title"));
        detailDate.setText(getIntent().getStringExtra("date"));
        detailLocation.setText(getIntent().getStringExtra("location"));
        detailDescription.setText(getIntent().getStringExtra("description"));

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(JournalDetailsActivity.this, EditDetailsActivity.class)
                        .putExtra("image", getIntent().getStringExtra("image"))
                        .putExtra("title", detailTitle.getText().toString())
                        .putExtra("date", detailDate.getText().toString())
                        .putExtra("location", detailLocation.getText().toString())
                        .putExtra("description", detailDescription.getText().toString())
                        .putExtra("latitude", getIntent().getStringExtra("latitude"))
                        .putExtra("longitude", getIntent().getStringExtra("longitude"))
                        .putExtra("position", getIntent().getStringExtra("position"))
                );
                finish();
            }
        });
    }

    //Method to delete journal data
    public void deleteData() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseDatabase.getInstance().getReference("JournalRecord").child(DashboardActivity.uid)
                                .child(getIntent().getStringExtra("position")).removeValue();
                        startActivity(new Intent(JournalDetailsActivity.this, DashboardActivity.class)
                                .putExtra("uid", DashboardActivity.uid));
                        Toast.makeText(JournalDetailsActivity.this, "Data deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    //redirecting back to dashboard page on back press
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(JournalDetailsActivity.this, DashboardActivity.class)
                .putExtra("image", getIntent().getStringExtra("image"))
                .putExtra("title", detailTitle.getText().toString())
                .putExtra("date", detailDate.getText().toString())
                .putExtra("location", detailLocation.getText().toString())
                .putExtra("description", detailDescription.getText().toString())
                .putExtra("latitude", getIntent().getStringExtra("latitude"))
                .putExtra("longitude", getIntent().getStringExtra("longitude"))
                .putExtra("position", getIntent().getStringExtra("position"))
                .putExtra("uid", DashboardActivity.uid)
        );
        finish();
    }

    //location retrieval method
    public void getLocation() {
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
        Task<Location> locationTask = locationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mapFragmentSupport.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        LatLng latitudeLongitude = new LatLng(latitudeView, longitudeView);
                        MarkerOptions locationMarker = new MarkerOptions();
                        locationMarker.position(latitudeLongitude);
                        googleMap.addMarker(locationMarker);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latitudeLongitude, 17));

                    }
                });
            }

        });
    }

}