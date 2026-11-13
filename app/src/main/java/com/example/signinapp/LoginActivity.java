
package com.example.signinapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuthException;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Handles user login through Firebase Authentication.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private TextView registerLink;

    private FirebaseAuth auth;

    /**
     * Initializes the login interface and Firebase Authentication.
     *
     * @param savedInstanceState previously saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);

        loginButton.setOnClickListener(v -> loginUser());

        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            ));
        });

        // Automatically open profile if already logged in.
        if (auth.getCurrentUser() != null) {
            openProfile();
        }
    }

    /**
     * Validates the login fields and authenticates the user.
     */
    /**
     * Validates login credentials and authenticates the user.
     */
    private void loginUser() {

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        loginButton.setEnabled(false);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    loginButton.setEnabled(true);

                    if (task.isSuccessful()) {

                        Toast.makeText(this,
                                "Login successful!",
                                Toast.LENGTH_SHORT).show();

                        openProfile();

                    } else {

                        Exception exception = task.getException();

                        String errorCode = "UNKNOWN_ERROR";

                        if (exception instanceof FirebaseAuthException) {
                            errorCode =
                                    ((FirebaseAuthException) exception)
                                            .getErrorCode();
                        }

                        android.util.Log.e(
                                "LOGIN_ERROR",
                                "Firebase error: " + errorCode,
                                exception
                        );

                        Toast.makeText(this,
                                "Login failed: " + errorCode,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Opens the user's profile screen.
     */
    private void openProfile() {
        startActivity(new Intent(
                LoginActivity.this,
                ProfileActivity.class
        ));
        finish();
    }
}
