package com.example.socialplay;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SelectPostImage;
    private Button AddPostButton;
    private EditText Description;
    private Uri PictureUri;
    private StorageReference PicturesReference;
    private String saveCurrentDate, saveCurrentTime, postRandomName;
    private String DownloadUrl;
    private String CurrentUserId;
    private FirebaseFirestore _db;
    private FirebaseAuth mAuth;
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            result -> {

            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        _db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        SelectPostImage = findViewById(R.id.add_image);
        AddPostButton = findViewById(R.id.add_post_button);
        Description = findViewById(R.id.description);
        mToolbar = findViewById(R.id.add_post_page_toolbar);
        PicturesReference = FirebaseStorage.getInstance().getReference();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add post");

        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetContent.launch("image/*");
            }
        });

        AddPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidatePostInfo();
            }
        });
    }

    private void ValidatePostInfo() {
        if(PictureUri == null){
            Toast.makeText(this, "Please add a picture", Toast.LENGTH_SHORT).show();
        }
        else{
            StorePicture();
        }
    }

    private void StorePicture() {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calendar.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;
        StorageReference filePath = PicturesReference.child("Post Pictures")
                .child(PictureUri.getLastPathSegment() + postRandomName + ".jpg");
        filePath.putFile(PictureUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            DownloadUrl = uri.toString();
                            Toast.makeText(PostActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                            SavingPostInformation();
                        }
                    });


                }
            }
        });

    }

    private void SavingPostInformation() {
        _db.collection("profiles")
                .document(CurrentUserId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(value.exists()){
                            String userFirstName = value.getString("firstName");
                            String userLastName = value.getString("lastName");
                            String userProfileImageUrl = value.getString("profilePicture");

                            HashMap postMap = new HashMap();
                            postMap.put("uid", CurrentUserId);
                            postMap.put("fullName", userFirstName + " " + userLastName);
                            postMap.put("date", saveCurrentDate);
                            postMap.put("time", saveCurrentTime);
                            postMap.put("description", Description.getText().toString());
                            postMap.put("profilePicture", userProfileImageUrl);
                            postMap.put("postPicture", DownloadUrl);

                            _db.collection("posts")
                                    .add(postMap)
                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if(task.isSuccessful()){
                                                SendUserToMainActivity();
                                                Toast.makeText(PostActivity.this, "New post added successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data!= null){
            PictureUri = data.getData();
            SelectPostImage.setImageURI(PictureUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            SendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent loginIntent = new Intent(PostActivity.this, MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}