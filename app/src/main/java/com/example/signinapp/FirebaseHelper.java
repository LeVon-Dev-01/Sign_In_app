
package com.example.signinapp;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides Firebase authentication, Firestore, and Storage operations.
 */
public class FirebaseHelper {

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;

    /**
     * Initializes Firebase services.
     */
    public FirebaseHelper() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    /**
     * Registers a new user using email and password.
     *
     * @param email user's email
     * @param password user's password
     * @param firstName user's first name
     * @param lastName user's last name
     * @param photoUri optional profile photo
     * @param callback callback receiving success or failure
     */
    public void registerUser(
            String email,
            String password,
            String firstName,
            String lastName,
            Uri photoUri,
            @NonNull RegistrationCallback callback) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        callback.onComplete(
                                false,
                                task.getException()
                        );
                        return;
                    }

                    String uid = auth.getCurrentUser().getUid();

                    if (photoUri == null) {
                        saveUser(uid, firstName, lastName,
                                email, null, callback);
                        return;
                    }

                    StorageReference imageRef = storage
                            .getReference()
                            .child("profile_images")
                            .child(uid + ".jpg");

                    imageRef.putFile(photoUri)
                            .continueWithTask(uploadTask -> {
                                if (!uploadTask.isSuccessful()) {
                                    throw uploadTask.getException();
                                }

                                return imageRef.getDownloadUrl();
                            })
                            .addOnCompleteListener(uploadTask -> {

                                if (uploadTask.isSuccessful()) {
                                    saveUser(
                                            uid,
                                            firstName,
                                            lastName,
                                            email,
                                            uploadTask.getResult().toString(),
                                            callback
                                    );
                                } else {
                                    callback.onComplete(
                                            false,
                                            uploadTask.getException()
                                    );
                                }
                            });
                });
    }

    /**
     * Saves user profile information in Cloud Firestore.
     *
     * @param uid Firebase user ID
     * @param firstName user's first name
     * @param lastName user's last name
     * @param email user's email
     * @param photoUrl profile photo URL
     * @param callback registration callback
     */
    private void saveUser(
            String uid,
            String firstName,
            String lastName,
            String email,
            String photoUrl,
            RegistrationCallback callback) {

        Map<String, Object> user = new HashMap<>();

        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("email", email);
        user.put("photoUrl", photoUrl);

        firestore.collection("users")
                .document(uid)
                .set(user)
                .addOnCompleteListener(task -> {

                    callback.onComplete(
                            task.isSuccessful(),
                            task.getException()
                    );
                });
    }

    /**
     * Returns the Firebase Authentication instance.
     *
     * @return Firebase authentication object
     */
    public FirebaseAuth getAuth() {
        return auth;
    }

    /**
     * Returns the Firestore database instance.
     *
     * @return Firestore database object
     */
    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    /**
     * Callback for registration results.
     */
    public interface RegistrationCallback {

        /**
         * Receives the result of a registration operation.
         *
         * @param success whether registration completed successfully
         * @param error error information, or null on success
         */
        void onComplete(boolean success, Exception error);
    }
}

