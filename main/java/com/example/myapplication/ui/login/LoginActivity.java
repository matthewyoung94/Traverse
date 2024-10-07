package com.example.myapplication.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.MapsActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private TextView usernameEditText;
    private TextView passwordEditText;
    private TextView signUpTextView;
    private TextView forgotPasswordTextView;
    private boolean isPasswordVisible = false;

    FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        signUpTextView = findViewById(R.id.signUp);
        forgotPasswordTextView = findViewById(R.id.forgotPassword);

        // Sign In button click listener
        loginButton.setOnClickListener(v -> {
            // Handle sign in here
            signIn();
        });

        // Sign up text view click listener
        signUpTextView.setOnClickListener(v -> {
            // Handle sign up here
            signUp();
        });

        // Forgot password text view click listener
        forgotPasswordTextView.setOnClickListener(v -> {
            // Handle forgot password here
            forgotPassword();
        });

        // Password visibility toggle
        passwordEditText.setOnLongClickListener(v -> {
            togglePasswordVisibility();
            return true;
        });
    }

    private void signIn() {
        String email = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Snackbar.make(loginButton, "Authentication successful", Snackbar.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                        finish();
                    } else {
                        Snackbar.make(loginButton, "Authentication failed", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    public void signInWithCredentials(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Snackbar.make(loginButton, "Authentication successful", Snackbar.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                        finish();
                    } else {
                        Snackbar.make(loginButton, "Authentication failed", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }


    private void signUp() {
        String email = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Snackbar.make(loginButton, "Email or password cannot be empty", Snackbar.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign up success, update UI with the signed-up user's information
                        Snackbar.make(loginButton, "Sign up successful", Snackbar.LENGTH_SHORT).show();
                        // Automatically sign in the newly created user
                        signIn();
                    } else {
                        // If sign up fails, display a message to the user.
                        Snackbar.make(loginButton, "Sign up failed: " + task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void forgotPassword() {
        // Add your forgot password logic here
        Snackbar.make(loginButton, "Forgot password clicked", Snackbar.LENGTH_SHORT).show();
    }

    private void togglePasswordVisibility() {
        // Toggle password visibility
        isPasswordVisible = !isPasswordVisible;
        passwordEditText.setInputType(isPasswordVisible ?
                android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }
}




