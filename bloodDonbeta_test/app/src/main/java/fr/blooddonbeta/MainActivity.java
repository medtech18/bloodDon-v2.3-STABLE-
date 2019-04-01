package fr.blooddonbeta;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.EventLogTags;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;

public class MainActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";
    private Intent intentLogin ;
    private Intent intentSignUp ;
    private FirebaseAuth mAuth;
    public static String userId;
    private static boolean calledAlready = false;




    AlertDialog.Builder builder ;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (!calledAlready)
        {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);

            calledAlready = true;
        }

        builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Confirm");
        builder.setMessage("google play services are not installed or updated. do you want to download it ?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which)
            {
                String LINK_TO_GOOGLE_PLAY_SERVICES = "play.google.com/store/apps/details?id=com.google.android.gms&hl=en";
                try {
                    Log.i(TAG, "You must install google services.");
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://" + LINK_TO_GOOGLE_PLAY_SERVICES)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + LINK_TO_GOOGLE_PLAY_SERVICES)));
                }

                // Do nothing, but close the dialog
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                finish();

                // Do nothing
                dialog.dismiss();
            }
        });

        if(checkPlayServices())
        setContentView(R.layout.activity_main);

        intentLogin  = new Intent(MainActivity.this, LoginActivity.class);
        intentSignUp = new Intent(MainActivity.this,SignUpActivity.class);

        mAuth = FirebaseAuth.getInstance();
        //userId = mAuth.getUid();


      // try {
      //     PackageInfo info = getPackageManager().getPackageInfo(
      //             "fr.blooddonbeta",
      //             PackageManager.GET_SIGNATURES);
      //     for (android.content.pm.Signature signature : info.signatures) {
      //         MessageDigest md = MessageDigest.getInstance("SHA");
      //         md.update(signature.toByteArray());
      //         Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
      //     }
      // } catch (PackageManager.NameNotFoundException e) {

      // } catch (NoSuchAlgorithmException e) {

      // }





    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null)
        {
            Intent intent = new Intent(MainActivity.this,SearchActivity.class);

            try
            {
                startActivity(intent);
                finish();

            }catch (Exception e)
            {
                e.printStackTrace();
            }


        }
    }

    public void loginClicked(View view)
    {
            if(isInternetOn())
            {
                startActivity(intentLogin);
            }
            else
                Toast.makeText(getApplicationContext(),"you are not connected to network ",Toast.LENGTH_LONG).show();
    }

    public void goToSignUpClicked(View view)
    {
        if(isInternetOn())
        {

            startActivity(intentSignUp);
        }
        else
                Toast.makeText(getApplicationContext(),"you are not connected to network ",Toast.LENGTH_LONG).show();

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

    private boolean checkPlayServices()
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode))
            {
                AlertDialog alert = builder.create();
                alert.show();

            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }



}
