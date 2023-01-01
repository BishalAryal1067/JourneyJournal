package com.example.journeyjournal;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Fragment;
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
import android.widget.SearchView;
import android.widget.Toast;


import com.example.journeyjournal.Model.JournalModel;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
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
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AddJournalActivity extends AppCompatActivity {

    AppCompatImageView journalImage;
    Button addImageButton;
    AppCompatButton submitButton;
    AppCompatImageButton backButton;
    TextInputEditText journalDate, journalLocation, journalTitle, journalDescription;
    TextInputLayout journalDateLayout, journalLocationLayout;
    DatePickerDialog datePickerDialog;

    Dialog mapsDialog;
    Uri uriPath;
    Bitmap bitmap;
    SupportMapFragment mapFragmentSupport;
    FusedLocationProviderClient locationProviderClient;
    LatLng currentLocation;
    String uid, locationLatitude, locationLongitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journal);

        //getting uid from login page
        uid = getIntent().getStringExtra("uid");

        //back button
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddJournalActivity.this, DashboardActivity.class).putExtra("uid", uid));
            }
        });


        //getting input fields
        journalImage = findViewById(R.id.journalImage);
        journalTitle = findViewById(R.id.journalTitle);
        journalDate = findViewById(R.id.journalDate);
        journalLocation = findViewById(R.id.journalLocation);
        journalDescription = findViewById(R.id.journalDescription);


        //data upload to firebase and cloud on clicking submit button
        submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validation()){
                    uploadData();
                }
            }
        });


        //Getting journal date
        journalDateLayout = findViewById(R.id.journalDateLayout);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DATE);

        //Getting date from calendar and setting date to InputEditText
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int mm, int dd, int yyyy) {
                int year, month, date;
                year = datePicker.getYear();
                month = datePicker.getDayOfMonth();
                date = datePicker.getDayOfMonth();
                journalDate.setText((month + 1) + " / " + date + " / " + year);
            }
        }, year, month, date);

        journalDateLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });


        // Adding image
        addImageButton = findViewById(R.id.addImageButton);
        addImageButton.setOnClickListener(new View.OnClickListener() {
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


        //Location implementation
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
//                        mapsDialog.show();
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
                        LatLng latitudeLongitude = new LatLng(location.getLatitude(), location.getLongitude());
                        MarkerOptions locationMarker = new MarkerOptions();
                        locationMarker.position(latitudeLongitude);
                        googleMap.addMarker(locationMarker);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latitudeLongitude, 17));

                        journalLocation = findViewById(R.id.journalLocation);
                        locationLatitude = String.valueOf(latitudeLongitude.latitude);
                        locationLongitude = String.valueOf(latitudeLongitude.longitude);
                        try {
                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            String cityName = addresses.get(0).getAddressLine(0);
                            String stateName = addresses.get(0).getAddressLine(1);
                            String countryName = addresses.get(0).getAddressLine(2);
                            journalLocation.setText(cityName);


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


                                journalLocation = findViewById(R.id.journalLocation);
                                try {
                                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                                    String cityName = addresses.get(0).getAddressLine(0);
                                    String stateName = addresses.get(0).getAddressLine(1);
                                    String countryName = addresses.get(0).getAddressLine(2);

                                    journalLocation.setText(cityName);
                                    locationLatitude = String.valueOf(currentLocation.latitude);
                                    locationLongitude = String.valueOf(currentLocation.longitude);

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


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK) {
            uriPath = data.getData();

            try {
                InputStream inputStream = getContentResolver().openInputStream(uriPath);
                bitmap = BitmapFactory.decodeStream(inputStream);
                journalImage = findViewById(R.id.journalImage);
                journalImage.setImageBitmap(bitmap);
                addImageButton.setText("Change");
            } catch (Exception e) {

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //uploading image to cloud storage and all the data to firebase
    private void uploadData() {

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Upload Status");
        progressDialog.show();

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        final StorageReference storageReference = firebaseStorage.getReference("Image1" + new Random().nextInt(60));

        storageReference.putFile(uriPath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        progressDialog.dismiss();

                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                JournalModel journalModel = new JournalModel(uri.toString(),
                                        journalTitle.getText().toString(),
                                        journalDate.getText().toString(),
                                        journalLocation.getText().toString(),
                                        journalDescription.getText().toString(),
                                        locationLatitude, locationLongitude
                                );


                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                DatabaseReference databaseReference = firebaseDatabase.getReference("JournalRecord").child(getIntent().getStringExtra("uid"));
                                databaseReference.push().setValue(journalModel);


                                journalTitle.setText("");
                                journalDate.setText("");
                                journalLocation.setText("");
                                journalDescription.setText("");
                                journalImage.setImageResource(R.drawable.default_landscape);
                                addImageButton.setText("ADD IMAGE");
                                Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), DashboardActivity.class).putExtra("uid", uid));
                                finish();
                            }
                        });

                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                        float percent = (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploaded : " + (int) percent + "%");
                    }
                });
    }

    //validating journal entries
    public boolean validation() {
        if(uriPath==null){
            addImageButton.requestFocus();
            Toast.makeText(this, "Select an image", Toast.LENGTH_SHORT).show();
            return false;
        }

        if ((journalTitle.getText().toString()).isEmpty()) {
            journalTitle.requestFocus();
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if ((journalDescription.getText().toString()).isEmpty()) {
            journalTitle.requestFocus();
            Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if ((journalDate.getText().toString()).isEmpty()) {
            journalTitle.requestFocus();
            Toast.makeText(this, "Date cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if ((journalLocation.getText().toString()).isEmpty()) {
            journalTitle.requestFocus();
            Toast.makeText(this, "Location cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    //navigating back to dashboard on backpress
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AddJournalActivity.this, DashboardActivity.class)
                .putExtra("uid", DashboardActivity.uid));
        finish();
    }
}
