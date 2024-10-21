package com.example.socialplay;


import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.type.Date;
import com.squareup.picasso.Picasso;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import static android.content.ContentValues.TAG;
import de.hdodenhof.circleimageview.CircleImageView;

public class CreateProfileActivity extends AppCompatActivity {

    private EditText FirstName, LastName;
    private Button SendButton, DateOfBirthButton;
    private CircleImageView ProfilePicture;
    private DatePickerDialog datePickerDialog;
    private FirebaseAuth mAuth;
    private FirebaseFirestore _db;
    private StorageReference UserProfilePictureRef;
    private String ProfilePictureUrl;
    String currentUserId;
    private String selectedTenantId;
    private String selectedFirebaseDatabaseUrl;
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            result -> {

            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserId =mAuth.getCurrentUser().getUid();
        _db = FirebaseFirestore.getInstance();
        UserProfilePictureRef = FirebaseStorage.getInstance().getReference().child("profile pictures");

        FirstName = findViewById(R.id.first_name_register);
        LastName = findViewById(R.id.last_name_register);
        DateOfBirthButton = findViewById(R.id.date_of_birth_register);
        SendButton = findViewById(R.id.button);
        ProfilePicture = findViewById(R.id.profile_picture);

        initDatePicker();
        DateOfBirthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });
        SendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveProfileInfo();
            }
        });

        ProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetContent.launch("image/*");
            }
        });

        // Fetch the selected tenant ID from the intent
        selectedTenantId = getIntent().getStringExtra("selectedTenantId");

        // Fetch the selected tenant's Firebase database URL from Firestore
        fetchTenantFirebaseDatabaseUrl(selectedTenantId);
    }

    private void fetchTenantFirebaseDatabaseUrl(String tenantId) {
        _db.collection("tenants").document(tenantId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    selectedFirebaseDatabaseUrl = document.getString("firebaseDatabaseUrl");
                    initializeFirebase(selectedFirebaseDatabaseUrl);
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void initializeFirebase(String firebaseDatabaseUrl) {
        // Initialize Firebase with the selected tenant's Firebase database URL
        // This is a placeholder for the actual Firebase initialization code
        // You will need to update this with the appropriate Firebase initialization logic
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            StorageReference filePath = UserProfilePictureRef.child(currentUserId+ ".jpg");
            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(CreateProfileActivity.this, "Profile picture set successfully", Toast.LENGTH_SHORT).show();
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                ProfilePictureUrl = uri.toString();
                                Picasso.get()
                                        .load(ProfilePictureUrl)
                                        .placeholder(R.drawable.empty_profile_image)
                                        .into(ProfilePicture);
                            }
                        });
                    }
                }
            });
        }
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = getDateString(day, month, year);
                DateOfBirthButton.setText(date);
            }
        };
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(this, AlertDialog.BUTTON_POSITIVE, dateSetListener, year, month, day);
    }

    private String getDateString(int day, int month, int year) {

        Month monthEnum = Month.of(month);
        String monthString = monthEnum.getDisplayName(TextStyle.SHORT, Locale.getDefault());
        return monthString + "/" + day + "/" + year;
    }

    private void SaveProfileInfo() {
        String firstName = FirstName.getText().toString();
        String lastName = LastName.getText().toString();
        String dateOfBirth = DateOfBirthButton.getText().toString();

        if(TextUtils.isEmpty(firstName)){
            Toast.makeText(this, "Please complete First name field", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(lastName)){
            Toast.makeText(this, "Please complete Last name field", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(dateOfBirth)){
            Toast.makeText(this, "Please complete Date of birth field", Toast.LENGTH_SHORT).show();
            return;
        }
        HashMap userMap = new HashMap();
        userMap.put("firstName", firstName);
        userMap.put("lastName", lastName);
        userMap.put("dateOfBirth", dateOfBirth);
        userMap.put("profilePicture", ProfilePictureUrl);
        _db.collection("profiles")
                .document(currentUserId)
                .set(userMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        SendUserToMainActivity();
                        Log.d(TAG, "DocumentSnapshot added with ID: " + currentUserId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent loginIntent = new Intent(CreateProfileActivity.this, MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}
