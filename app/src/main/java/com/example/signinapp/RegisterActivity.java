
package com.example.signinapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Handles new user registration and profile photo selection.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText firstNameInput;
    private EditText lastNameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;

    private ImageView profileImage;
    private Button registerButton;
    private TextView uploadPhoto;
    private TextView loginLink;

    private FirebaseHelper firebaseHelper;
    private Uri selectedPhoto;

    private ActivityResultLauncher<String> imagePicker;

    /**
     * Initializes the registration screen and image picker.
     *
     * @param savedInstanceState previously saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseHelper = new FirebaseHelper();

        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput =
                findViewById(R.id.confirmPasswordInput);

        profileImage = findViewById(R.id.profileImage);
        registerButton = findViewById(R.id.registerButton);
        uploadPhoto = findViewById(R.id.uploadPhoto);
        loginLink = findViewById(R.id.loginLink);

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedPhoto = uri;
                        profileImage.setImageURI(uri);
                    }
                }
        );

        uploadPhoto.setOnClickListener(v ->
                imagePicker.launch("image/*"));

        registerButton.setOnClickListener(v ->
                registerUser());

        loginLink.setOnClickListener(v ->
                finish());
    }

    /**
     * Validates registration information and creates a Firebase account.
     */
    private void registerUser() {

        String firstName =
                firstNameInput.getText().toString().trim();

        String lastName =
                lastNameInput.getText().toString().trim();

        String email =
                emailInput.getText().toString().trim();

        String password =
                passwordInput.getText().toString();

        String confirmPassword =
                confirmPasswordInput.getText().toString();

        if (firstName.isEmpty() ||
                lastName.isEmpty() ||
                email.isEmpty() ||
                password.isEmpty() ||
                confirmPassword.isEmpty()) {

            Toast.makeText(this,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email).matches()) {

            emailInput.setError("Enter a valid email");
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError(
                    "Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError(
                    "Passwords do not match");
            return;
        }

        registerButton.setEnabled(false);

        firebaseHelper.registerUser(
                email,
                password,
                firstName,
                lastName,
                selectedPhoto,
                (success, error) -> {

                    registerButton.setEnabled(true);

                    if (success) {

                        Toast.makeText(this,
                                "Account created successfully!",
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(
                                RegisterActivity.this,
                                ProfileActivity.class
                        );

                        startActivity(intent);
                        finish();

                    } else {

                        String message = error != null
                                ? error.getMessage()
                                : "Registration failed";

                        Toast.makeText(this,
                                message,
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }
}
