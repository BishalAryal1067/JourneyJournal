package com.example.journeyjournal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    AppCompatButton loginButton;
    TextInputEditText loginEmail, loginPassword;
    String username, password;
    AppCompatTextView registerRedirect;
    FirebaseAuth loginAuthentication;
    SharedPreferences sharedPreferences;
    LinearLayoutCompat progressLayout;
    CheckBox rememberUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //finding progressBar using id
        progressLayout = findViewById(R.id.progressLayout);

        //remember user / keep user signed in
        rememberUser = findViewById(R.id.rememberUser);

        //redirecting to register page
        registerRedirect = findViewById(R.id.registerRedirect);
        registerRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                finish();
            }
        });


        //validating and logging user into the system
        loginAuthentication = FirebaseAuth.getInstance();
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressLayout.setVisibility(View.VISIBLE);

                //Extracting email and password given by user
                loginEmail = findViewById(R.id.loginEmail);
                loginPassword = findViewById(R.id.loginPassword);
                username = loginEmail.getText().toString().trim();
                password = loginPassword.getText().toString();

                boolean validationCheck = loginValidation(username, password);
                if (validationCheck) {
                    loginButton.setClickable(false);
                    loginAuthentication.signInWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                if (rememberUser.isChecked()) {
                                    sharedPreferences = getSharedPreferences("rememberUser", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("signInState", true).putString("uid", loginAuthentication.getUid()).apply();
                                }
                                progressLayout.setVisibility(View.INVISIBLE);
                                startActivity(new Intent(getApplicationContext(), DashboardActivity.class).putExtra("uid", loginAuthentication.getUid()));
                                finish();
                                Toast.makeText(LoginActivity.this, "Login Successful !", Toast.LENGTH_SHORT).show();
                            } else {
                                progressLayout.setVisibility(View.INVISIBLE);
                                Toast.makeText(LoginActivity.this, "Check email or password", Toast.LENGTH_SHORT).show();
                                loginEmail.setText("");
                                loginPassword.setText("");
                                loginButton.setClickable(true);
                            }
                        }
                    });
                } else {
                    progressLayout.setVisibility(View.INVISIBLE);
                }
            }
        });
    }


    //method for validating username and password input
    public boolean loginValidation(String username, String password) {
        if (username.length() == 0 || password.length() == 0) {
            progressLayout.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Username or password is empty !", Toast.LENGTH_SHORT).show();
            loginEmail.requestFocus();
            return false;
        } else {
            return true;
        }
    }
}