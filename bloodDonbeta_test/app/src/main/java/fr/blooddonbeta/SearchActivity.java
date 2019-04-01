package fr.blooddonbeta;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.*;
import com.google.firebase.auth.*;
import com.skyfishjy.library.RippleBackground;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class SearchActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleApiClient mGoogleApiClient;
    private boolean googleApiConnected;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private LatLng myGeoLocation;
    private ArrayList<User> userList;
    private ArrayList<String> keys;
    final Handler handler = new Handler();
    private Boolean googleAPILOADED = true;
    private int doFragment;


    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList();
    private ArrayList<String> permissions = new ArrayList();
    private final static int ALL_PERMISSIONS_RESULT = 101;


    //Firebase Variables

    private DatabaseReference dRef;
    private DatabaseReference rRef;
    private DatabaseReference users;
    private DatabaseReference isUserOnline;
    private String userId;
    private FirebaseUser currentUser;


    //
    ImageButton userMapsButton;

    ///FAGEMENT

    // User newRecyleUserItem ;
    SearchResultFragment myFragment;
    FragmentManager fragmentManager;
    ConstraintLayout layout;
    FragmentTransaction ft;
    SearchFragment bloodSeachAnimationFragment;
    UserProfileFragment userProfileFragment;
    ConversationFragment conversationFragment;


    // progress bar new values

    int userLimit = 10;
    int distanceLimit;
    boolean stopSearch = false;

    // bloodSearchAnimation

    RippleBackground rippleBackground;


    private String bloodType;
    private String myCurrentDonors;
    private String myCurrentRecipients;
    private User currentUserObject;


    static int userOperation;
    //public static User curentUserInfo;

    private List<String> currentList;


    private belka.us.androidtoggleswitch.widgets.ToggleSwitch toggleSwitch;



    // Chat Components

    private DateFormat df ;
    public static boolean anotherActivityOpened;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_search);

        } catch (Exception e) {
            e.printStackTrace();
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser.getUid();
        dRef = FirebaseDatabase.getInstance().getReference("DonorAvailable");
        dRef.keepSynced(true);
        rRef = FirebaseDatabase.getInstance().getReference("RecipientAvailable");
        users= FirebaseDatabase.getInstance().getReference("users");
        isUserOnline = FirebaseDatabase.getInstance().getReference("users").child(userId).child("online");
        isUserOnline.keepSynced(true);
        isUserOnline.onDisconnect().setValue(ServerValue.TIMESTAMP);
        //rRef.keepSynced(true);

        userList = new ArrayList<User>();

        //  newRecyleUserItem = new User();
        keys = new ArrayList<String>();
        myFragment = new SearchResultFragment();
        bloodSeachAnimationFragment = new SearchFragment();
        userProfileFragment = new UserProfileFragment();
        conversationFragment = new ConversationFragment();
        doFragment = 0;


        //recyle view

        fragmentManager = getFragmentManager();
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.fragment_switch);
        layout.removeAllViewsInLayout();
        ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragment_switch, bloodSeachAnimationFragment);
        //ft.addToBackStack(null);
        ft.commitAllowingStateLoss();

        buildGoogleApiClient();
        // mGoogleApiClient.connect();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        rippleBackground = (RippleBackground) findViewById(R.id.content);
        bloodSeachAnimationFragment = new SearchFragment();


        userOperation = 0;


        // create Firebase Listeners
        createBloodTypeListenerOf(userId);
        createMyCurrentDonorsListener(userId);
        createMyCurrentRecipientsListener(userId);
        //createMyCurrentInfoListener(userId);

        toggleSwitch = findViewById(R.id.donationSwitcher);


        // Chat Components

        df = new SimpleDateFormat("HH:mm");

        anotherActivityOpened = false;

        // current User object
        currentUserObject = new User();
        setCurrentUserObject();


    }


    @Override
    public void onStart() {
        super.onStart();

        if (toggleSwitch != null) {
            toggleSwitch.setOnToggleSwitchChangeListener(new belka.us.androidtoggleswitch.widgets.ToggleSwitch.OnToggleSwitchChangeListener() {

                @Override
                public void onToggleSwitchChangeListener(int position, boolean isChecked) {
                    userOperation = position;
                }
            });

        }
        if (!SearchActivity.this.isFinishing()) {

            if (currentUser != null)
                isUserOnline.setValue(true);

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!SearchActivity.this.isFinishing()) {

            if (currentUser != null)
                isUserOnline.setValue(true);
        }
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        if (!SearchActivity.this.isFinishing()){

            if(currentUser != null)
            {
                String currenTime = df.format(Calendar.getInstance().getTime());

                if(!anotherActivityOpened)
                    isUserOnline.setValue(System.currentTimeMillis());

            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        String currenTime = df.format(Calendar.getInstance().getTime());

        if(!anotherActivityOpened)
            isUserOnline.setValue(System.currentTimeMillis());

        //users.child(userId).child("online").setValue("last seen at "+ currenTime);
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //testDialog("Connection established. Fetching location ..");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {

            askForPermission();
            onConnected(bundle);
        }

        if (googleAPILOADED) {
            // startNewSearch();
            //getLocation();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        // testDialog("connection SUSPENDED");
    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {
            //testDialog("loctionChanged : WORKING");
            mLastLocation = location;

            GeoFire geoFireD = new GeoFire(dRef);
            geoFireD.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));

            //GeoFire geoFireR = new GeoFire(rRef);
            //geoFireR.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));


            myGeoLocation = new LatLng(location.getLatitude(), location.getLongitude());
        } else {
            testDialog("loctionChanged : NOT WORKING");
        }
    }

    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getBaseContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }



    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            askForPermission();
            getLocation();
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                // testDialog("getloaction result: " + location.getLatitude() + "-" + location.getLongitude());

                                mLastLocation = location;

                                GeoFire geoFireD = new GeoFire(dRef);
                                geoFireD.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));

                                GeoFire geoFireR = new GeoFire(rRef);
                                geoFireR.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));


                                myGeoLocation = new LatLng(location.getLatitude(), location.getLongitude());


                                startNewSearch();

                            }
                        }
                    });

        }
    }

    public void searchClicked(View view) {
        if (mGoogleApiClient.isConnected()) {
            testDialog("google API CLIENT IS CONNECTED");
            //  getLocation();
        } else {
            testDialog("google api client is NOT connected");

        }
    }


    private int radius = 100;

    private Boolean driverFound = false;
    private String driverFoundID;

    int count = 0;

    GeoQuery geoQuery;
    User newRecyleUserItem;


    private void getClosestUsers() {
        //testDialog("O - :" + MatchBloodType.covertToS(MatchBloodType.getRecipientOf("O -").split(",")).toString());
        //testDialog("O - :" + MatchBloodType.covertToS(MatchBloodType.getRecipientOf("O +")).toString());
        //testDialog("O - :" + MatchBloodType.covertToS(MatchBloodType.getRecipientOf("B -")).toString());
        //testDialog("O - :" + MatchBloodType.covertToS(MatchBloodType.getRecipientOf("B +")).toString());
        //testDialog("O - :" + MatchBloodType.covertToS(MatchBloodType.getRecipientOf("A -")).toString());
        //testDialog("O - :" + MatchBloodType.covertToS(MatchBloodType.getRecipientOf("A +")).toString());
        //testDialog("O - :" + MatchBloodType.covertToS(MatchBloodType.getRecipientOf("AB -")).toString());


        //radius = 10;
        driverFound = false;

        if (myGeoLocation != null) {
            //userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            //rRef = FirebaseDatabase.getInstance().getReference("RecipientAvailable");
            GeoFire geoFireR = new GeoFire(rRef);
            geoQuery = geoFireR.queryAtLocation(new GeoLocation(myGeoLocation.latitude, myGeoLocation.longitude), radius);
            geoQuery.removeAllListeners();


            if (userOperation == 0) // if user clicked on Donor
            {
                // getMyCurrentRecipients(userId);

                if (myCurrentRecipients != null) {
                    //currentList.clear();
                    currentList = Arrays.asList(myCurrentRecipients.split(","));

                }

            } else if (userOperation == 1) // if user clicked on Recipient
            {

                if (myCurrentDonors != null) {
                    //currentList.clear();
                    currentList = Arrays.asList(myCurrentDonors.split(","));

                }
            }

            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(final String key, final GeoLocation location) {
                    if (!key.equals(userId) && !keys.contains(key))//&& !stopSearch )
                    {
                        keys.add(key);

                        final Location recipientLocation = new Location("");
                        recipientLocation.setLongitude(location.longitude);
                        recipientLocation.setLatitude(location.latitude);

                        users.child(key).child("bloodtype").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String bloodType = snapshot.getValue().toString();
                                    if (bloodType != null && currentList.contains(bloodType)) {
                                        if (count < userLimit) {
                                            pushUserToRecyleView(key, recipientLocation);
                                            count++;

                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                testDialog(databaseError.getMessage());
                            }
                        });

                        //pushUserToRecyleView(key,recipientLocation);

                    }
                }

                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "GEOLOCATION IS NULL", Toast.LENGTH_LONG);
        }

    }

    public void createBloodTypeListenerOf(final String key) {
        users.child(key).child("bloodtype").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    bloodType = snapshot.getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                testDialog(databaseError.getMessage());
            }
        });

    }

    public void createMyCurrentDonorsListener(final String key) {
        users.child(key).child("myDonors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    myCurrentDonors = snapshot.getValue().toString();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                testDialog(databaseError.getMessage());

            }
        });
    }

    public void createMyCurrentRecipientsListener(final String key) {
        users.child(key).child("myRecipients").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    myCurrentRecipients = snapshot.getValue().toString();


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                testDialog(databaseError.getMessage());

            }
        });

    }


    public void createMyCurrentInfoListener(final String key) {
        users.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {


                    //curentUserInfo = new User((String) snapshot.child("fullname").getValue(),
                    //        (String) snapshot.child("gender").getValue(),
                    //        (String) snapshot.child("birthday").getValue(),
                    //        (String) snapshot.child("bloodtype").getValue(),
                    //        (String) snapshot.child("weight").getValue(),
                    //        (String) snapshot.child("profileUrl").getValue(),
                    //        (String) snapshot.child("myDonors").getValue(),
                    //        (String) snapshot.child("myRecipients").getValue());


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                testDialog(databaseError.getMessage());

            }
        });

    }


    public void pushUserToRecyleView(final String key, final Location recipientLocation) {
        users.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    newRecyleUserItem = new User((String) dataSnapshot.child("fullname").getValue(),
                            (String) dataSnapshot.child("gender").getValue(),
                            (String) dataSnapshot.child("birthday").getValue(),
                            (String) dataSnapshot.child("bloodtype").getValue(),
                            (String) dataSnapshot.child("weight").getValue(),
                            (String) dataSnapshot.child("profileUrl").getValue(),
                            (String) dataSnapshot.child("myDonors").getValue(),
                            (String) dataSnapshot.child("myRecipients").getValue(),
                            (String) dataSnapshot.child("isProfileCreated").getValue(),
                            key
                    );

                    newRecyleUserItem.setDistanceToOther((mLastLocation.distanceTo(recipientLocation) / 1000));
                    newRecyleUserItem.setUserLocation(recipientLocation);
                    //testDialog("nom : " + newRecyleUserItem.fullname + "/// KEY: " + key);

                    myFragment.pushNewUser(newRecyleUserItem);
                    myFragment.setCurrentUser(currentUserObject);


                } else {
                    testDialog("snapshot is null " + key);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "The read failed: " + databaseError.getCode(), Toast.LENGTH_SHORT).show();

            }
        });
    }


    public void mainSearchClicked(View view) {
        //startNewSearch();

        fragmentManager = getFragmentManager();
        // ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.fragment_switch);
        //layout.removeAllViewsInLayout();

        FragmentTransaction ft = fragmentManager.beginTransaction();
        //if oldFragment already exits in fragmentManager use it


        // bloodSeachAnimationFragment = new SearchFragment();
        //  myFragment.setArguments(b);

        ft.replace(R.id.fragment_switch, bloodSeachAnimationFragment, "bloodSeachAnimationFrag");

        // ft.commit();

        ft.commitAllowingStateLoss();


        // RippleBackground rippleBackground =(RippleBackground)findViewById(R.id.content);
        // rippleBackground.startRippleAnimation();


        // startNewSearch();

    }

    private void startNewSearch() {
        RippleBackground rippleBackground = (RippleBackground) findViewById(R.id.content);
        rippleBackground.startRippleAnimation();


        try {


            if (myGeoLocation == null) {
                ///testDialog("gettin location ... ");
                getLocation();
            } else {
                count = 0;
                keys = new ArrayList<String>();
                userList = new ArrayList<User>();
                this.myFragment = new SearchResultFragment();
                driverFound = false;
                doFragment = 0;
                stopSearch = false;


                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {


                            fragmentManager = getFragmentManager();
                            //layout=(ConstraintLayout)findViewById(R.id.fragment_switch);
                            //layout.removeAllViewsInLayout();


                            ft = fragmentManager.beginTransaction();

                            //if oldFragment already exits in fragmentManager use it
                            ft.replace(R.id.fragment_switch, myFragment, "usersListFrag");
                            ft.commitAllowingStateLoss();


                            getClosestUsers();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "ERRONO", Toast.LENGTH_LONG);
                        }

                    }
                }, 5000);

            }


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "ERRONO", Toast.LENGTH_LONG);
        }
    }


    //========================[ PERMISSIONS ] =================================
    public void askForPermission() {
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
    }

    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(SearchActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();

                    }
                })
                .create()
                .show();
    }


    private void testDialog(String message) {
        new AlertDialog.Builder(SearchActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("SearchActivity", "onConnectionFailed:" + connectionResult.getErrorCode() + "," + connectionResult.getErrorMessage());


    }

    public void setMapsButtonAndActivity(Location userLocation) {
        try {
            Intent mapIntent = new Intent(SearchActivity.this, MapsActivity.class);
            mapIntent.putExtra("myLastLocation", mLastLocation);
            mapIntent.putExtra("userLocation", userLocation);

            startActivity(mapIntent);

        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }


    public void chatSearch(View view)
    {

            if (!conversationFragment.isVisible())
            {
                fragmentManager = getFragmentManager();
                ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.fragment_switch);
                //layout.removeAllViewsInLayout();

                ft = fragmentManager.beginTransaction();
                //ft.setCustomAnimations(R.transition.)


                //if oldFragment already exits in fragmentManager use it


                //ft.replace(R.id.fragment_switch, userProfileFragment);
                //ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, 0, 0);
                //ft.addToBackStack(null);
                //ft.commit();
                //ft.commitAllowingStateLoss();


                //ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right, 0, 0);
                ft.replace(R.id.fragment_switch, conversationFragment);
                //ft.addToBackStack("profileSettings");
                ft.commit();

            }

    }

    public void updateUserList(User u) {
        try {
            if (myFragment != null && myFragment.getAdapter() != null)
                myFragment.getAdapter().newAddeddata(u);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void animationClick(View view) {
        //  pl.droidsonroids.gif.GifTextView imageView=(pl.droidsonroids.gif.GifTextView)findViewById(R.id.imageView4);
        //  imageView.setOnClickListener(new View.OnClickListener() {
        //      @Override
        //      public void onClick(View view) {
        //          rippleBackground.startRippleAnimation();
        //      }});

        pl.droidsonroids.gif.GifImageView v = (pl.droidsonroids.gif.GifImageView) findViewById(R.id.imageView4);

        v.setImageResource(R.drawable.searchanimation);

        // RippleBackground rippleBackground =(RippleBackground)findViewById(R.id.content);
        //rippleBackground.startRippleAnimation();


        startNewSearch();
    }


    public void ShowDialog() {

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        final View Viewlayout = inflater.inflate(R.layout.seekbardialog,
                (ViewGroup) findViewById(R.id.layout_dialog));

        final TextView item1 = (TextView) Viewlayout.findViewById(R.id.txtItem1); // txtItem1
        final TextView item2 = (TextView) Viewlayout.findViewById(R.id.txtItem2); // txtItem2

        popDialog.setIcon(android.R.drawable.btn_star_big_on);
        popDialog.setTitle("Please Select Rank 1-100 ");
        popDialog.setView(Viewlayout);

        //  seekBar1
        SeekBar seek1 = (SeekBar) Viewlayout.findViewById(R.id.seekBar1);
        seek1.setProgress(userLimit);
        item1.setText("Users Limit : " + userLimit);

        seek1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Do something here with new value
                userLimit = progress;
                item1.setText("Users Limit : " + progress);
            }

            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }
        });

        //  seekBar2
        SeekBar seek2 = (SeekBar) Viewlayout.findViewById(R.id.seekBar2);
        seek2.setProgress(radius);
        item2.setText("Distance Limit : " + radius);

        seek2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Do something here with new value
                radius = progress;
                item2.setText("Distance Limit : " + progress);
            }

            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }
        });


        // Button OK
        popDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //startNewSearch();
                        dialog.dismiss();
                    }

                });


        popDialog.create();
        popDialog.show();

    }

    public void changeSettingClick(View view) {

        ShowDialog();
    }

    @SuppressLint("ResourceType")
    public void profileSettings(View view) {
        //startActivity(new Intent(SearchActivity.this,SettingsActivity.class));


        if (!userProfileFragment.isVisible()) {
            fragmentManager = getFragmentManager();
            ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.fragment_switch);
            //layout.removeAllViewsInLayout();

            ft = fragmentManager.beginTransaction();
            //ft.setCustomAnimations(R.transition.)


            //if oldFragment already exits in fragmentManager use it


            //ft.replace(R.id.fragment_switch, userProfileFragment);
            //ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, 0, 0);
            //ft.addToBackStack(null);
            //ft.commit();
            //ft.commitAllowingStateLoss();


            ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right, 0, 0);
            ft.replace(R.id.fragment_switch, userProfileFragment);
            //ft.addToBackStack("profileSettings");
            ft.commit();


        }

        //openActivity2();
    }


    @Override
    public void onBackPressed() {

        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }


    public void  setCurrentUserObject()
    {
        users.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                      currentUserObject = new User((String) dataSnapshot.child("fullname").getValue(),
                            (String) dataSnapshot.child("gender").getValue(),
                            (String) dataSnapshot.child("birthday").getValue(),
                            (String) dataSnapshot.child("bloodtype").getValue(),
                            (String) dataSnapshot.child("weight").getValue(),
                            (String) dataSnapshot.child("profileUrl").getValue(),
                            (String) dataSnapshot.child("myDonors").getValue(),
                            (String) dataSnapshot.child("myRecipients").getValue(),
                            (String) dataSnapshot.child("isProfileCreated").getValue(),
                            userId
                    );
                    currentUserObject.setUserLocation(mLastLocation);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "The read failed: " + databaseError.getCode(), Toast.LENGTH_SHORT).show();
            }
        });
    }



}