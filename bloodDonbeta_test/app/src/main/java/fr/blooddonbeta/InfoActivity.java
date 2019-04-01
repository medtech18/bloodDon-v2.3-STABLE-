package fr.blooddonbeta;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.*;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;


public class InfoActivity extends AppCompatActivity {

    private static final int CHOOSE_IMAGE = 101;
    private DatePicker datePicker;
    private Calendar calendar;
    private TextView dateView;
    private int year, month, day;
    private CircleImageView profileImage ;
    private Uri uriProfileImage;
    private ProgressBar progressBar ;
    private DatabaseReference mDatabase;
    private String profileImageUrl;
    private final String userID = FirebaseAuth.getInstance().getUid();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        dateView = (TextView) findViewById(R.id.birthdayEditText);
        dateView.setInputType(InputType.TYPE_NULL);

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year, month+1, day);


        Spinner genderS = (Spinner) findViewById(R.id.genderSpinner);
        String[] genderL = new String[]{"Male", "Female"};

        Spinner bloodTypeS = (Spinner) findViewById(R.id.bloodTypeSpinner);
        String[] bloodTypeL = new String[]{"O -", "O +" , "B -" , "B +" ,"A -" ,"A +" ,"AB -" ,"AB +"};


        setSpinner(genderL,genderS);
        setSpinner(bloodTypeL,bloodTypeS);

        profileImage = (CircleImageView)findViewById(R.id.chatUserPicture);
        progressBar = (ProgressBar)findViewById(R.id.imageLoading);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //profileImageUrl = "gs://blooddonor-14a30.appspot.com/profilepics/defaultprofileimage.png";
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        finish();
        return true;
    }

  //  @Override
 //   public boolean onCreateOptionsMenu(Menu menu) {
 //       // Inflate the menu; this adds items to the action bar if it is present.
 //       getMenuInflater().inflate(R.menu.menu_main, menu);
  //      return true;
  //  }


    // here the code executed when a click on check button is performed

  //  @Override
  //  public boolean onOptionsItemSelected(MenuItem item) {
   //     switch(item.getItemId()) {
  //          case R.id.action_name:
  //              if(checkInputs())
  //              {

 //               }

   //             break;
   //     }
   //     return super.onOptionsItemSelected(item);
   // }

    private void setSpinner(String[] list , Spinner spinner)
    {
        // Get reference of widgets from XML layout

        // Initializing an ArrayAdapter
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this,R.layout.support_simple_spinner_dropdown_item,list
        );
        spinnerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

    }


    @SuppressWarnings("deprecation")
    public void setDate(View view) {
        showDialog(999);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999)
        {
            return new DatePickerDialog(this,
                    myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    // TODO Auto-generated method stub
                    // arg1 = year
                    // arg2 = month
                    // arg3 = day
                    showDate(arg1, arg2+1, arg3);
                    //dismissDialog(999);

                }
            };

    private void showDate(int year, int month, int day) {
        dateView.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    private void writeNewUser(User user)
    {
        final String userId = FirebaseAuth.getInstance().getUid();
        final String token = FirebaseInstanceId.getInstance().getToken();


        mDatabase.child("users").child(userId).setValue(user.getUserObject()).addOnCompleteListener(InfoActivity.this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            mDatabase.child("users").child(userId).child("device_token").setValue(token);

                            Intent intent = new Intent(InfoActivity.this, SearchActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                });


     //   mDatabase.child("users").child(userId).child("donors").setValue(user.getUserObject()).addOnCompleteListener(InfoActivity.this,
     //           new OnCompleteListener<Void>() {
     //               @Override
     //               public void onComplete(@NonNull Task<Void> task) {
     //                   if (task.isSuccessful()) {
     //                       Intent intent = new Intent(InfoActivity.this, SearchActivity.class);
     //                       startActivity(intent);
     //                       finish();
//
     //                   } else {
     //                       Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
     //                   }
//
     //               }
     //           });
     //

    }

    private User getUserData()
    {
        String fullname = ((EditText) findViewById(R.id.fullnameSpinner)).getText().toString();
        String gender = ((Spinner) findViewById(R.id.genderSpinner)).getSelectedItem().toString();
        String birthday = ((EditText) findViewById(R.id.birthdayEditText)).getText().toString();
        String bloodtype = ((Spinner) findViewById(R.id.bloodTypeSpinner)).getSelectedItem().toString();
        String weight = ((EditText) findViewById(R.id.weighEditText)).getText().toString();

         User user = new User(fullname, gender , birthday , bloodtype ,weight , profileImageUrl , MatchBloodType.getDonorsOf(bloodtype) ,MatchBloodType.getRecipientOf(bloodtype),"1","myID");
         return user;
    }

    private boolean checkInputs()
    {
        EditText fullame = (EditText)findViewById(R.id.fullnameSpinner);
        EditText weight = (EditText)findViewById(R.id.weighEditText);

        String fullnameText = fullame.getText().toString();
        String weighttext = weight.getText().toString();

        boolean correct = true;

        if(fullnameText.isEmpty())
        {
            fullame.setError("set your fullname please!");
            correct = false;
        }

        if(weighttext.isEmpty())
        {
            weight.setError("set your weight please !");
            correct = false;
        }

        fullame.clearFocus();
        weight.clearFocus();

        return correct;
    }

    public void editProfileClicked(View view)
    {
        showImageChooser();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriProfileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                profileImage.setImageBitmap(bitmap);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebaseStorage(final String userID)
    {
        StorageReference profileImageRef =
                FirebaseStorage.getInstance().getReference("profilepics/" + userID + ".jpg");

        if (uriProfileImage != null)
        {



            progressBar.setVisibility(View.VISIBLE);
            profileImageRef.putFile(uriProfileImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBar.setVisibility(View.GONE);
                           // profileImageUrl = taskSnapshot.getDownloadUrl().toString();


                           /// StorageReference profileImageRef =FirebaseStorage.getInstance().getReference("profilepics/" + userID + ".jpg");
                            final String baseUrl = taskSnapshot.getStorage().getPath();

                            if(baseUrl != null)
                            {
                                profileImageUrl = "gs://blooddonor-14a30.appspot.com"+baseUrl;

                            }
                            writeNewUser(getUserData());



                            // if(profileImageRef != null)
                           // {
                           //     profileImageUrl = "profilepics/" + userID + ".jpg" ;
//
                           // }else
                           // {
                           //     profileImageUrl = "gs://blooddonor-14a30.appspot.com/profilepics/defaultprofileimage.png";
//
                           // }




                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(InfoActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            writeNewUser(getUserData());

                        }
                    });
        }
    }

    private void showImageChooser()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }

    public void saveUserInfoClicked(View view)
    {
        try
        {

            uploadImageToFirebaseStorage(userID);

        }catch (Exception e)
        {
            Log.e("SAVING USER INFO", "exception :", e);

        }

    }
}
