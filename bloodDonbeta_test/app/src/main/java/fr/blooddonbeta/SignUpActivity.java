package fr.blooddonbeta;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
public class SignUpActivity extends AppCompatActivity implements View.OnFocusChangeListener{

    private Button signUp ;
    private EditText email ;
    private EditText confirmEmail ;
    private EditText pwd ;
    private EditText confirmPwd ;


    private FirebaseAuth mAuth;
    ProgressBar pgsDialog ;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signUp = (Button) findViewById(R.id.signUp_button);
        email = (EditText)findViewById(R.id.signUp_email);
        confirmEmail = (EditText)findViewById(R.id.signUp_confirmEmail);
        pwd = (EditText)findViewById(R.id.signUp_password);
        confirmPwd = (EditText)findViewById(R.id.signUp_confirmPassword);

        mAuth = FirebaseAuth.getInstance();
        email.setOnFocusChangeListener(this);
        confirmEmail.setOnFocusChangeListener(this);
        pwd.setOnFocusChangeListener(this);
        confirmPwd.setOnFocusChangeListener(this);
        pgsDialog = (ProgressBar)findViewById(R.id.signUpProgressBar);


    }

    @Override
    public void onFocusChange(View view, boolean b)
    {
        if (b && view.equals(confirmEmail))
        {
            confirmEmail.setHint("");
        }
        else
            confirmEmail.setHint("confirm Email");


        if (b && view.equals(email))
        {
            email.setHint("");
        }
        else
            email.setHint("Email");


        if (b && view.equals(pwd))
        {
            pwd.setHint("");

        }
        else
            pwd.setHint("Password");


        if (b && view.equals(confirmPwd))
        {
            confirmPwd.setHint("");
        }
        else
            confirmPwd.setHint(" Confirm Password");

    }


    public void registerUser()
    {
       String emailText = this.email.getText().toString().trim();
       String confirmEmailText = this.confirmEmail.getText().toString().trim();
       String pwdText = this.pwd.getText().toString().trim();
       String confirmPwdText = this.confirmPwd.getText().toString().trim();



        if(checkInputs(emailText,confirmEmailText,pwdText,confirmPwdText))
        {
            if(isInternetOn())
            {
                pgsDialog.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(emailText, pwdText)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task)
                            {
                                if (task.isSuccessful())
                                {
                                    // Sign in success, update UI with the signed-in user's information
                                    pgsDialog.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "user registered sucessfully", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(SignUpActivity.this,InfoActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    pgsDialog.setVisibility(View.GONE);
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        email.setError("User Email Already exists!");

                                    } else if (task.getException() instanceof FirebaseNetworkException) {
                                        email.setError("connect to Network ,Please!");

                                    } else if (task.getException().getMessage().contains("The email address is badly formatted.")) {
                                        email.setError("Incorrect Email!");

                                    } else {
                                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }

                                }

                            }
                        });
            }
            else
                Toast.makeText(getApplicationContext(),"you are not connected to network ",Toast.LENGTH_LONG).show();
        }


    }

    public boolean checkInputs( String email , String confirmEmail , String pwd , String confirmPwd)
    {
        boolean correct =true;

        if(email.isEmpty())
        {
            this.email.setError("set your email , please!");
            correct = false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            this.email.setError("Email format is wrong !");
        }

        if(confirmEmail.isEmpty())
        {
            this.confirmEmail.setError("confirm your email , please!");
            correct = false;

        }else if(!Patterns.EMAIL_ADDRESS.matcher(confirmEmail).matches())
        {
            this.confirmEmail.setError("Email format is wrong !");
        }

        if(!email.isEmpty() && !confirmEmail.isEmpty() && !email.matches(confirmEmail))
        {
            this.confirmEmail.setError("Email Missmatch !");
            correct=  false;
        }

        if(pwd.isEmpty())
        {
            this.pwd.setError("set your Password , please!");
            correct = false;
        }

        if(confirmPwd.isEmpty())
        {
            this.confirmPwd.setError("confirm your Password , please!");
            correct = false;
        }

        if(!pwd.isEmpty() && !confirmPwd.isEmpty() && !pwd.matches(confirmPwd))
        {
            this.confirmPwd.setError("Password Missmatch !");
            correct=  false;
        }

        this.email.clearFocus();
        this.confirmEmail.clearFocus();
        this.confirmPwd.clearFocus();
        this.pwd.clearFocus();


        return correct;
    }

    public void SignUpClicked(View view)
    {
        registerUser();
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

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }


    public void alreadyHaveAccount(View view)
    {
        Intent intentLogin  = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intentLogin);
    }

}
