package com.example.journeyjournal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.car.ui.AlertDialogBuilder;
import com.example.journeyjournal.Model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    AppCompatImageButton backButton;
    TextInputEditText registerEmail, registerPassword, registerConfirmPassword;
    String email, password, confirmPassword;
    AppCompatButton registerButton;
    FirebaseAuth registerAuthentication;
    AppCompatTextView loginRedirect;
    LinearLayoutCompat regProgressLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //layout containing progress bar and text
        regProgressLayout = findViewById(R.id.regProgressLayout);

        //input fields
        registerEmail = findViewById(R.id.userEmail);
        registerPassword = findViewById(R.id.userPassword);
        registerConfirmPassword = findViewById(R.id.userConfirmPassword);

        //validating user information and creating user
        registerButton = findViewById(R.id.registerButton);
        registerAuthentication = FirebaseAuth.getInstance();
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                regProgressLayout.setVisibility(View.VISIBLE);
                email = registerEmail.getText().toString().trim();
                password = registerPassword.getText().toString().trim();
                confirmPassword = registerConfirmPassword.getText().toString().trim();
                boolean validationCheck = emailValidation(email) && passwordValidation(password, confirmPassword);
                if (validationCheck) {
                    userRegistration();
                } else {
                    regProgressLayout.setVisibility(View.INVISIBLE);
                }
            }
        });

        //redirecting back to login activity
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        loginRedirect = findViewById(R.id.loginRedirect);
        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    //validating email
    public boolean emailValidation(String email) {
        if (email.length() == 0) {
            registerEmail.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    //validating password
    public boolean passwordValidation(String password, String confirmPassword) {
        if (password.length() == 0) {
            registerPassword.requestFocus();
            Toast.makeText(this, "Password can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        } else if (password.length() < 6) {
            registerPassword.requestFocus();
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!password.equals(confirmPassword)) {
            registerConfirmPassword.requestFocus();
            Toast.makeText(this, "Password's don't match", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }


    //method for registering user
    public void userRegistration() {
        registerEmail = findViewById(R.id.userEmail);
        registerPassword = findViewById(R.id.userPassword);
        registerConfirmPassword = findViewById(R.id.userConfirmPassword);
        email = registerEmail.getText().toString().trim();
        password = registerPassword.getText().toString().trim();
        confirmPassword = registerConfirmPassword.getText().toString().trim();

        boolean validationResult = emailValidation(email) && passwordValidation(password, confirmPassword);
        if (validationResult) {
            registerAuthentication.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        UserModel user = new UserModel(email, password, registerAuthentication.getUid());
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference databaseNode = database.getReference("Users");
                        databaseNode.push().setValue(user);

                        regProgressLayout.setVisibility(View.INVISIBLE);

                        Toast.makeText(RegisterActivity.this, "User Registration Successful !", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();

                    } else {
                        regProgressLayout.setVisibility(View.INVISIBLE);
                        Toast.makeText(RegisterActivity.this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            regProgressLayout.setVisibility(View.INVISIBLE);
            registerEmail.setText("");
            registerPassword.setText("");
            registerConfirmPassword.setText("");
            registerEmail.requestFocus();
            Toast.makeText(RegisterActivity.this, "Chek your email amd password", Toast.LENGTH_SHORT).show();
        }
    }

    //redirecting to login on back press
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }
}