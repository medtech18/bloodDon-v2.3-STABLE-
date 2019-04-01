package fr.blooddonbeta;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import android.support.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;


public class LoginActivity extends AppCompatActivity implements View.OnFocusChangeListener
{

    private static final String TAG = "FACELOG";
    private static final int LOG_OUT = 101;
    private Button signUp ;
    private EditText email ;
    private EditText pwd ;
    private FirebaseAuth mAuth;
    private ProgressBar pgsDialog ;
    private CallbackManager mCallbackManager;
    private ImageView facebookLoginButton ;
    private LoginButton loginButton;
    private boolean isUserRegistered;






    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signUp = (Button) findViewById(R.id.signIn_button);
        email = (EditText)findViewById(R.id.signIn_email);
        pwd = (EditText)findViewById(R.id.signIn_password);
        mAuth = FirebaseAuth.getInstance();
        pgsDialog = (ProgressBar)findViewById(R.id.signIn_progressBar);

        email.setOnFocusChangeListener(this);
        pwd.setOnFocusChangeListener(this);

        facebookLoginButton = findViewById(R.id.face_button);
        facebookLoginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                loginButton.performClick();
            }
        });




        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile", "user_friends");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                //handleFacebookAccessToken(loginResult.getAccessToken());

                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });


        //createFullNameListener();
        isUserRegistered = false;

    }

    // Normal Login

    public void loginClicked(View view)
    {
        String emailText = this.email.getText().toString().trim();
        String pwdText = this.pwd.getText().toString().trim();

        if(checkInputs(emailText,pwdText))
        {
            if(isInternetOn())
            {
                pgsDialog.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(emailText,pwdText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            // Sign in success, update UI with the signed-in user's information
                            pgsDialog.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(),"Login Successufluuululuy",Toast.LENGTH_LONG).show();

                            String token = FirebaseInstanceId.getInstance().getToken();
                            createFullNameListener(token);


                            try
                            {
                                if(isUserRegistered)
                                {
                                    Intent intent = new Intent(LoginActivity.this,SearchActivity.class);
                                    startActivity(intent);
                                    finish();

                                }else
                                {
                                    Intent intent = new Intent(LoginActivity.this,InfoActivity.class);
                                    startActivity(intent);

                                }

                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                        }else
                        {
                            pgsDialog.setVisibility(View.GONE);

                            if(task.getException() instanceof FirebaseAuthInvalidUserException)
                            {
                                email.setError("No user found for this email, return back to sign up !");

                            }else if(task.getException() instanceof FirebaseNetworkException)
                            {
                                Toast.makeText(getApplicationContext(),"you are not connected to network!",Toast.LENGTH_LONG).show();

                            } else
                            {
                                Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }

                        }

                    }
                });
            }
            else
            {
                Toast.makeText(getApplicationContext(),"you are not connected to network ",Toast.LENGTH_LONG).show();
            }
        }

    }


    @Override
    public void onFocusChange(View view, boolean b)
    {
        if (b && view.equals(pwd))
        {
            pwd.setHint("");

        }
        else
            pwd.setHint("Password");

        if (b && view.equals(email))
        {
            email.setHint("");
        }
        else
            email.setHint("Email");


    }

    public boolean checkInputs( String email , String pwd )
    {
        boolean correct =true;

        if(email.isEmpty())
        {
            this.email.setError("set your email , please!");
            correct = false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            this.email.setError("Email format is wrong !");
            correct = false;
        }

        if(pwd.isEmpty())
        {
            this.pwd.setError("set your Password , please!");
            correct = false;
        }

        this.email.clearFocus();
        this.pwd.clearFocus();

        return correct;
    }

    public final boolean isInternetOn()
    {
        ConnectivityManager connec = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // ARE WE CONNECTED TO THE NET
        if ( connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED )
        {
            // MESSAGE TO SCREEN FOR TESTING (IF REQ)
            //Toast.makeText(this, connectionType + ” connected”, Toast.LENGTH_SHORT).show();
            return true;
        }
        else if ( connec.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED
                ||  connec.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED  )
        {
            return false;
        }

        return false;
    }

    // Facebook Login



// ...

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private void handleFacebookAccessToken(AccessToken token)
    {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            String token = FirebaseInstanceId.getInstance().getToken();

                            createFullNameListener(token);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    public void createFullNameListener( final String token)
    {
        mAuth = FirebaseAuth.getInstance();

        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getUid());

        userRef.child("isProfileCreated").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    // use "username" already exists

                    if(dataSnapshot.getValue().equals("1"))
                    {
                        userRef.child("device_token").setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                Intent intent = new Intent(LoginActivity.this,SearchActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });

                    }

                }else
                {
                    Intent intent = new Intent(LoginActivity.this,InfoActivity.class);
                    startActivity(intent);
                    finish();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });


    }


    private void testDialog(String message)
    {
        new AlertDialog.Builder(LoginActivity.this)
                .setMessage(message)
                .setPositiveButton("OK",   new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

}
