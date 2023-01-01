package com.example.journeyjournal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.example.journeyjournal.Adapter.JournalAdapter;
import com.example.journeyjournal.Model.JournalModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.FirebaseDatabase;

public class DashboardActivity extends AppCompatActivity {

    ExtendedFloatingActionButton addJournalButton;
    RecyclerView journalRecycler;
    AppCompatTextView logoutText;
    AppCompatImageButton logoutButton;
    JournalAdapter journalAdapter;
    SearchView searchBar;
    SharedPreferences sharedPreferences;
    LinearLayoutCompat signOut;

    static String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //user id
        uid = getIntent().getStringExtra("uid");

        //Recycler view
        journalRecycler = findViewById(R.id.journalRecycler);

        //checking if user is remembered by the system
        checkSignInStatus();

        //signing user out of the system
        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogout();
            }
        });

        logoutText = findViewById(R.id.logoutText);
        logoutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogout();
            }
        });


        //searching data
        searchBar = findViewById(R.id.searchBar);
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchItemByTitle(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                searchItemByTitle(query);
                return false;
            }
        });


        //loading data into recycler from firebase
        journalRecycler.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<JournalModel> options =
                new FirebaseRecyclerOptions.Builder<JournalModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("JournalRecord").child(getIntent().getStringExtra("uid")), JournalModel.class)
                        .build();

        //setting the adapter to the recycler
        journalAdapter = new JournalAdapter(options);
        journalRecycler.setAdapter(journalAdapter);

        //redirecting to add journal activity
        addJournalButton = findViewById(R.id.addJournalButton);
        addJournalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddJournalActivity.class).putExtra("uid", getIntent().getStringExtra("uid")));
                finish();
            }
        });

        //Setting up linear layout for recycler
        journalRecycler = findViewById(R.id.journalRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        journalRecycler.setLayoutManager(layoutManager);

    }

    @Override
    protected void onStart() {
        super.onStart();
        journalAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        journalAdapter.stopListening();
    }

    //method for searching item using their title
    public void searchItemByTitle(String query) {
        FirebaseRecyclerOptions<JournalModel> options =
                new FirebaseRecyclerOptions.Builder<JournalModel>()
                        .setQuery(FirebaseDatabase.getInstance()
                                .getReference().child("JournalRecord")
                                .child(getIntent().getStringExtra("uid"))
                                .orderByChild("title")
                                .startAt(query)
                                .endAt(query + "\uf8ff"), JournalModel.class)
                        .build();
        JournalAdapter recyclerAdapter = new JournalAdapter(options);
        recyclerAdapter.startListening();
        journalRecycler.setAdapter(recyclerAdapter);
    }

    public void checkSignInStatus() {
        sharedPreferences = getSharedPreferences("rememberUser", MODE_PRIVATE);
        sharedPreferences.getBoolean("signInState", true);
        uid = sharedPreferences.getString("uid", String.valueOf(MODE_PRIVATE));
    }

    public void userLogout(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DashboardActivity.this).setTitle("Log Out")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sharedPreferences = getSharedPreferences("rememberUser", MODE_PRIVATE);
                        sharedPreferences.edit().putBoolean("signInState", false).putString("uid", String.valueOf(MODE_PRIVATE)).apply();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
}

