package fr.blooddonbeta;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {
    private static final int CHOOSE_IMAGE = 101;
    public static final int RESULT_OK = -1;

    private CircleImageView profileImage;
    private Uri uriProfileImage;
    private ProgressBar progressBar;
    private DatabaseReference mDatabase;
    private String profileImageUrl;
    private String userId;
    private TextView updatedfullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        ImageView settingButton = findViewById(R.id.settingButton);
        ImageView LogOutButton = findViewById(R.id.LogOutButton);


        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserProfileActivity.this, SettingsActivity.class));
            }
        });

        LogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();

                Intent intent = new Intent(UserProfileActivity.this.getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);


            }
        });


        updatedfullName = findViewById(R.id.updatedFullName);


        profileImage = (CircleImageView) findViewById(R.id.updateProfileImage);
        progressBar = (ProgressBar) findViewById(R.id.updateProfileLoading);
        profileImageUrl = "gs://blooddonor-14a30.appspot.com/profilepics/defaultprofileimage.png";
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        userId = FirebaseAuth.getInstance().getUid();
        createUpdatedPictureListener();
        createFullNameListener();


        ImageView editProfileImage = findViewById(R.id.editProfileImage);
        editProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageChooser();
            }
        });

    }




    private void uploadImageToFirebaseStorage(final String userID) {
        StorageReference profileImageRef =
                FirebaseStorage.getInstance().getReference("profilepics/" + userID + ".jpg");

        if (uriProfileImage != null) {
            progressBar.setVisibility(View.VISIBLE);
            profileImageRef.putFile(uriProfileImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBar.setVisibility(View.GONE);

                            StorageReference profileImageRef =
                                    FirebaseStorage.getInstance().getReference("profilepics/" + userID + ".jpg");

                            if (profileImageRef != null) {
                                try {
                                    mDatabase.child("users").child(userId).child("profileUrl").setValue("gs://blooddonor-14a30.appspot.com/profilepics/" + userID + ".jpg");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            } else {
                                try {
                                    mDatabase.child("users").child(userId).child("profileUrl").setValue("gs://blooddonor-14a30.appspot.com/profilepics/defaultprofileimage.png");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriProfileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(UserProfileActivity.this.getApplicationContext().getContentResolver(), uriProfileImage);
                profileImage.setImageBitmap(bitmap);
                uploadImageToFirebaseStorage(userId);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void createUpdatedPictureListener() {
        FirebaseDatabase.getInstance().getReference("users").child(userId).child("profileUrl").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    try {
                        FirebaseStorage storage = FirebaseStorage.getInstance();

                        StorageReference gsReference = storage.getReferenceFromUrl(snapshot.getValue().toString());
                        Glide.with(profileImage.getContext())
                                .using(new FirebaseImageLoader())
                                .load(gsReference)
                                .into(profileImage);


                        // Glide.with(holder.ProfilePicture.getContext()).load(gsReference).into(holder.ProfilePicture);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void createFullNameListener()
    {
        FirebaseDatabase.getInstance().getReference("users").child(userId).child("fullname").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    updatedfullName.setText(snapshot.getValue().toString());


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    public void userProfileMainSearchClicked(View view)
    {
       //final Intent intentSearchActivity = new Intent(this,SearchActivity.class);
       //        startActivity(intentSearchActivity);
       //        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void mainSearchIcon(View view)
    {
    }
}
