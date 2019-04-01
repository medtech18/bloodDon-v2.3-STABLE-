package fr.blooddonbeta;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.Timer;
import java.util.TimerTask;

public class DonorActivity extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener
{

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    public static final long INTERVAL=10000;//variable to execute services every 10 second
    private Timer mTimer=null; // timer handling
    private Handler mHandler=new Handler(); // run on another Thread to avoid crash



    @Override
    public void onCreate()
    {
        // cancel if service is  already existed
        Toast.makeText(getApplicationContext(), "IN SERVICES", Toast.LENGTH_SHORT).show();

       buildGoogleApiClient();


       if(mTimer!=null)
           mTimer.cancel();
       else
           mTimer=new Timer(); // recreate new timer
       mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(),0,INTERVAL);// schedule task
    }



    protected synchronized void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)

                  .addConnectionCallbacks(this)

                .addOnConnectionFailedListener(this)

                .addApi(LocationServices.API)

                  .build();

          mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        Toast.makeText(getApplicationContext(), "lat" + mLastLocation.getLatitude() + "long" + mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
       mLocationRequest = new LocationRequest();
       mLocationRequest.setInterval(1000);
       mLocationRequest.setFastestInterval(1000);
       mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

       if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
       {
           Toast.makeText(getApplicationContext(), "NO PERMISSION", Toast.LENGTH_SHORT).show();

           return;
       }
       LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    //inner class of TimeDisplayTimerTask
    private class TimeDisplayTimerTask extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // display toast at every 10 second

                    Toast.makeText(getApplicationContext(), "lat :" + mLastLocation.getLatitude() + "long" + mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }




}

