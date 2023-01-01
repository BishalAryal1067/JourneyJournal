package com.example.journeyjournal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.android.car.ui.AlertDialogBuilder;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class EditDetailsActivity extends AppCompatActivity {

    AppCompatImageView updatedImage;
    TextInputEditText updatedTitle, updatedDate, updatedLocation, updatedDescription;
    AppCompatButton updateDetailsButton;
    LatLng currentLocation;
    FusedLocationProviderClient locationProviderClient;
    SupportMapFragment mapFragmentSupport;
    Button updateImageButton;
    Dialog mapsDialog;
    String uid;
    DatePickerDialog datePickerDialog;
    TextInputLayout journalDateLayout;

    Double latitude, longitude;
    AppCompatImageButton backButton;
    Uri uriPath;
    Bitmap bitmap;

    TextInputLayout journalLocationLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_details);


        //getting uid from login page
        uid = getIntent().getStringExtra("uid");

        //getting elements from layout file
        updatedImage = findViewById(R.id.updatedImage);
        updatedTitle = findViewById(R.id.updatedTitle);
        updatedDate = findViewById(R.id.updatedDate);
        updatedLocation = findViewById(R.id.updatedLocation);
        updatedDescription = findViewById(R.id.updatedDescription);

        //setting value to all the elements
        Glide.with(updatedImage.getContext()).load(getIntent().getStringExtra("image")).into(updatedImage);
        updatedTitle.setText(getIntent().getStringExtra("title"));
        updatedDate.setText(getIntent().getStringExtra("date"));
        updatedLocation.setText(getIntent().getStringExtra("location"));
        updatedDescription.setText(getIntent().getStringExtra("description"));


        //redirecting back to details page
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditDetailsActivity.this, JournalDetailsActivity.class)
                        .putExtra("image", getIntent().getStringExtra("image"))
                        .putExtra("title", getIntent().getStringExtra("title"))
                        .putExtra("date", getIntent().getStringExtra("date"))
                        .putExtra("location", getIntent().getStringExtra("location"))
                        .putExtra("description", getIntent().getStringExtra("description"))
                        .putExtra("uid", DashboardActivity.uid)
                        .putExtra("latitude",String.valueOf(latitude))
                        .putExtra("longitude",String.valueOf(longitude))
                );
                finish();
            }
        });


        //Getting journal date
        journalDateLayout = findViewById(R.id.journalDateLayout);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DATE);

        //Getting date from calendar and setting date to InputEditText
        updatedDate = findViewById(R.id.updatedDate);
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int mm, int dd, int yyyy) {
                int year, month, date;
                year = datePicker.getYear();
                month = datePicker.getDayOfMonth();
                date = datePicker.getDayOfMonth();
                updatedDate.setText((month + 1) + " / " + date + " / " + year);
            }
        }, year, month, date);

        journalDateLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });

        //getting location from map
        latitude = Double.valueOf(getIntent().getStringExtra("latitude"));
        longitude = Double.valueOf(getIntent().getStringExtra("longitude"));

        //updating data to the firebase
        updateDetailsButton = findViewById(R.id.updateDetailsButton);
        updateDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDetails();
            }
        });

        mapsDialog = new Dialog(this);
        mapsDialog.setContentView(R.layout.map_layout);
        mapsDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mapsDialog.setCancelable(true);
        journalLocationLayout = findViewById(R.id.journalLocationLayout);
        journalLocationLayout.setEndIconOnClickListener(new View.OnClickListener() {
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

        // image upload
        updateImageButton = findViewById(R.id.updateImageButton);
        updateImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dexter.withContext(getApplicationContext()).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(Intent.createChooser(intent, "Select an image"), 1);

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK) {
            uriPath = data.getData();

            try {
                InputStream inputStream = getContentResolver().openInputStream(uriPath);
                bitmap = BitmapFactory.decodeStream(inputStream);
                updatedImage = findViewById(R.id.updatedImage);
                updatedImage.setImageBitmap(bitmap);
                updateImageButton.setText("Change");
            } catch (Exception e) {

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
                        LatLng latitudeLongitude = new LatLng(latitude, longitude);
                        MarkerOptions locationMarker = new MarkerOptions();
                        locationMarker.position(latitudeLongitude);
                        googleMap.addMarker(locationMarker);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latitudeLongitude, 17));

                        updatedLocation = findViewById(R.id.updatedLocation);
                        try {
                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            String cityName = addresses.get(0).getAddressLine(0);
                            String stateName = addresses.get(0).getAddressLine(1);
                            String countryName = addresses.get(0).getAddressLine(2);
                            updatedLocation.setText(cityName);


                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {
                                currentLocation = latLng;
                                locationMarker.position(new LatLng(location.getLatitude(), location.getLongitude()));
                                googleMap.clear();
                                googleMap.addMarker(new MarkerOptions().position(latLng));

                                updatedLocation = findViewById(R.id.updatedLocation);
                                try {
                                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                                    String cityName = addresses.get(0).getAddressLine(0);
                                    String stateName = addresses.get(0).getAddressLine(1);
                                    String countryName = addresses.get(0).getAddressLine(2);

                                    updatedLocation.setText(cityName);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                    }
                });
            }

        });
    }

    //method of updating the data in the database
    public void updateDetails() {
        ProgressDialog statusDialog = new ProgressDialog(this);
        statusDialog.setTitle("Update Status");
        statusDialog.setMessage("Updating details..");
        statusDialog.show();

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference("JournalRecord").child(DashboardActivity.uid + new Random().nextInt(50));
        storageReference.putFile(uriPath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                statusDialog.dismiss();
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("title", updatedTitle.getText().toString().toLowerCase(Locale.ROOT));
                        map.put("date", updatedDate.getText().toString());
                        map.put("location", updatedLocation.getText().toString());
                        map.put("description", updatedDescription.getText().toString());
                        map.put("locationLatitude", String.valueOf(latitude));
                        map.put("locationLongitude", String.valueOf(longitude));
                        map.put("image", uri.toString());

                        FirebaseDatabase.getInstance().getReference()
                                .child("JournalRecord").child(DashboardActivity.uid)
                                .child(getIntent().getStringExtra("position"))
                                .updateChildren(map)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        successfulUpdate();
                                    }
                                });
                    }
                });
            }
        })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        float percent = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        statusDialog.setMessage("Uploading: " + (int) percent + "%");
                    }
                });
    }

    //on successful update of data we call this function
    public void successfulUpdate() {
        Toast.makeText(this, "Data successfully updated", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(EditDetailsActivity.this, DashboardActivity.class).putExtra("uid", DashboardActivity.uid));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(EditDetailsActivity.this, JournalDetailsActivity.class)
                .putExtra("image", getIntent().getStringExtra("image"))
                .putExtra("title", getIntent().getStringExtra("title"))
                .putExtra("date", getIntent().getStringExtra("date"))
                .putExtra("location", getIntent().getStringExtra("location"))
                .putExtra("description", getIntent().getStringExtra("description"))
                .putExtra("uid", DashboardActivity.uid)
                .putExtra("latitude",String.valueOf(latitude))
                .putExtra("longitude",String.valueOf(longitude))
        );
        finish();
    }
}



