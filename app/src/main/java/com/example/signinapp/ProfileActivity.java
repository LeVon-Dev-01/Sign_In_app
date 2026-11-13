
package com.example.signinapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.bumptech.glide.Glide;

/**
 * Displays the authenticated user's profile information.
 */
public class ProfileActivity extends AppCompatActivity {

    private TextView profileName;
    private TextView profileEmail;
    private ImageView profileImage;
    private Button logoutButton;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    /**
     * Initializes the profile screen and loads user information.
     *
     * @param savedInstanceState previously saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profileImage = findViewById(R.id.profileImage);
        logoutButton = findViewById(R.id.logoutButton);

        logoutButton.setOnClickListener(v -> logoutUser());

        loadProfile();
    }

    /**
     * Retrieves the current user's profile from Firestore.
     */
    private void loadProfile() {

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            openLogin();
            return;
        }

        profileEmail.setText(currentUser.getEmail());

        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        String firstName =
                                document.getString("firstName");

                        String lastName =
                                document.getString("lastName");

                        String photoUrl =
                                document.getString("photoUrl");

                        profileName.setText(
                                (firstName == null ? "" : firstName)
                                        + " " +
                                        (lastName == null ? "" : lastName)
                        );

                        if (photoUrl != null &&
                                !photoUrl.isEmpty()) {

                            Glide.with(this)
                                    .load(photoUrl)
                                    .placeholder(
                                            android.R.drawable.ic_menu_myplaces)
                                    .into(profileImage);
                        }

                    } else {
                        Toast.makeText(this,
                                "Profile information not found",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(error ->
                        Toast.makeText(this,
                                "Unable to load profile",
                                Toast.LENGTH_SHORT).show());
    }

    /**
     * Signs out the current Firebase user.
     */
    private void logoutUser() {

        auth.signOut();
        openLogin();
    }

    /**
     * Returns the user to the login screen.
     */
    private void openLogin() {

        Intent intent = new Intent(
                ProfileActivity.this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}
