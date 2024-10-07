package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public void fetchUserDataForTest() {
        fetchUserData();
    }

    public TextView getPointsTextView() {
        return pointsTextView;
    }

    public TextView getUsernameTextView() {
        return usernameTextView;
    }

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView usernameTextView;
    private TextView pointsTextView;
    private TextInputLayout usernameInputLayout;
    private EditText usernameEditText;
    private Button saveUsernameButton;
    private ImageView profileImageView;
    private RecyclerView locationsRecyclerView;

    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    public FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = auth.getCurrentUser();

    private List<String> visitedLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setTheme(R.style.Theme_MyApplication);
        setContentView(R.layout.activity_profile);

        usernameTextView = findViewById(R.id.usernameTextView);
        pointsTextView = findViewById(R.id.pointsTextView);
        locationsRecyclerView = findViewById(R.id.locationsRecyclerView);
        usernameInputLayout = findViewById(R.id.usernameInputLayout);
        usernameEditText = findViewById(R.id.usernameEditText);
        saveUsernameButton = findViewById(R.id.saveUsernameButton);
        profileImageView = findViewById(R.id.profileImageView);

        saveUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUsername = usernameEditText.getText().toString();
                if (!TextUtils.isEmpty(newUsername)) {
                    saveUsernameToFirestore(newUsername);
                } else {
                }
            }
        });

        usernameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameTextView.setVisibility(View.GONE);
                usernameInputLayout.setVisibility(View.VISIBLE);
                saveUsernameButton.setVisibility(View.VISIBLE);
                usernameEditText.setText(usernameTextView.getText().toString());
            }
        });

        FloatingActionButton uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        uploadPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        fetchUserData();
    }

    private void saveUsernameToFirestore(String newUsername) {
        String userId = currentUser.getUid();
        db.collection("user").document(userId)
                .update("username", newUsername)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        usernameTextView.setText(newUsername);
                        usernameTextView.setVisibility(View.VISIBLE);
                        usernameInputLayout.setVisibility(View.GONE);
                        saveUsernameButton.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("Firestore", "Error updating username", e);
                    }
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (currentUser == null) return;
        String userId = currentUser.getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/" + userId + ".jpg");

        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();
                                saveProfileImageUrlToFirestore(downloadUrl);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("Firebase Storage", "Image upload failed", e);
                    }
                });
    }


    private void saveProfileImageUrlToFirestore(String imageUrl) {
        String userId = currentUser.getUid();
        db.collection("user").document(userId)
                .update("profile_image_url", imageUrl)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Profile image URL updated"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating profile image URL", e));
    }

    private void fetchUserData() {
        String userId = currentUser.getUid();
        db.collection("user").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        long points = documentSnapshot.getLong("points");
                        pointsTextView.setText("Points: " + points);

                        String lastVisitedLocation = documentSnapshot.getString("location_name");
                        visitedLocations = (List<String>) documentSnapshot.get("visited_locations");
                        if (visitedLocations == null) {
                            visitedLocations = new ArrayList<>();
                        }
                        updateRecyclerView();

                        String username = documentSnapshot.getString("username");
                        if (username != null) {
                            usernameTextView.setText(username);
                        }

                        String profileImageUrl = documentSnapshot.getString("profile_image_url");
                        if (profileImageUrl != null) {
                            Glide.with(ProfileActivity.this).load(profileImageUrl).into(profileImageView);
                        }
                    } else {
                        Log.e("Firestore", "User document does not exist");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error getting user document", e));
    }

    private void updateRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(ProfileActivity.this);
        locationsRecyclerView.setLayoutManager(layoutManager);
        LocationAdapter adapter = new LocationAdapter(visitedLocations);
        locationsRecyclerView.setAdapter(adapter);
    }
}