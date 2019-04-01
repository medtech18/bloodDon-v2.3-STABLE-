package fr.blooddonbeta;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversationFragment extends android.app.Fragment implements InternetConnectivityListener {

    private Context mContext;

    public ConversationFragment() {
        // Required empty public constructor
    }

    private RecyclerView mConvList;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    private ValueEventListener statusListener;

    private InternetAvailabilityChecker mInternetAvailabilityChecker;

    private TextView connectionStatusText;
    private View connectionStatus;


    private final Handler handler = new Handler();

    private ConnectivityManager conMgr ;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_conversation, container, false);

            connectionStatusText = mMainView.findViewById(R.id.connectionStatusText);
            connectionStatus = (View)mMainView.findViewById(R.id.connectionStatus);



        mConvList = (RecyclerView) mMainView.findViewById(R.id.conv_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);

        mConvDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mUsersDatabase.keepSynced(true);

        conMgr = (ConnectivityManager)getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getActivity().getApplicationContext(), linearLayoutManager.getOrientation());
        mConvList.addItemDecoration(itemDecoration);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);


        InternetAvailabilityChecker.init(getActivity());

        mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
        mInternetAvailabilityChecker.addInternetConnectivityListener(this);



        // Inflate the layout for this fragment
        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mConvDatabase.orderByChild("timestamp");

        FirebaseRecyclerAdapter<Conv, ConvViewHolder> firebaseConvAdapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(
                Conv.class,
                R.layout.conversationitem,
                ConvViewHolder.class,
                conversationQuery
        )


        {
            @Override
            protected void populateViewHolder(final ConvViewHolder convViewHolder, final Conv conv, int i) {


                final String list_user_id = getRef(i).getKey();

                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String data = dataSnapshot.child("message").getValue().toString();
                        convViewHolder.setMessage(data, conv.isSeen(),conv.getNotseenCount());

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("fullname").getValue().toString();
                        // String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        if (dataSnapshot.hasChild("online")) {
                            if (dataSnapshot.child("online").getValue().equals(true)) {
                                convViewHolder.setUserOnline(true);
                            } else {
                                convViewHolder.setUserOnline(false);
                            }
                        }
                        convViewHolder.setName(userName);
                        if (dataSnapshot.hasChild("profileUrl")) {
                            convViewHolder.setUserImage(dataSnapshot.child("profileUrl").getValue().toString(), getActivity().getApplicationContext());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        Intent chatIntent = new Intent(getActivity().getApplicationContext(), ChatActivity.class);
                        chatIntent.putExtra("sendFromConv", true);
                        chatIntent.putExtra("list_user_id", list_user_id);
                        chatIntent.putExtra("mCurrent_user_id", mCurrent_user_id);

                        startActivity(chatIntent);

                    }
                });

            }
        };

        mConvList.setAdapter(firebaseConvAdapter);

    }

    @Override
    public void onResume() {
        super.onResume();

        mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
        mInternetAvailabilityChecker.addInternetConnectivityListener(this);

    }

    @Override
    public void onPause()
    {
        super.onPause();
        mInternetAvailabilityChecker.removeInternetConnectivityChangeListener(this);

    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ConvViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setMessage(String message, boolean isSeen , int notSeenCount) {

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            TextView notSeenCounter = (TextView) mView.findViewById(R.id.notSeenCounter);
            ImageView notSeenCounterIcon = (ImageView) mView.findViewById(R.id.notSeenCounterIcon);




            userStatusView.setText(message);
            notSeenCounter.setText(String.valueOf(notSeenCount));

            if (!isSeen) {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
                mView.setBackgroundColor(mView.getResources().getColor(R.color.notSeenColor));

                if(notSeenCount > 0)
                {
                    notSeenCounter.setVisibility(View.VISIBLE);
                    notSeenCounterIcon.setVisibility(View.VISIBLE);
                }


            } else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
                mView.setBackgroundColor(mView.getResources().getColor(R.color.white));
                notSeenCounter.setVisibility(View.INVISIBLE);
                notSeenCounterIcon.setVisibility(View.INVISIBLE);

            }

        }

        public void setName(String name) {

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image, Context ctx) {

            final CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.chatUserPicture);
//
            if (thumb_image != null && !thumb_image.isEmpty()) {
                try {
                    StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(thumb_image);

                   Glide
                           .with(ctx)
                           .using(new FirebaseImageLoader())
                           .load(gsReference)
                           .asBitmap()
                           .error(ctx.getResources().getDrawable(R.drawable.defaultprofileimage))
                           .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                           .into(new SimpleTarget<Bitmap>() {
                               @Override
                               public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                   userImageView.setImageBitmap(resource);
                               }
                           });

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

//

        }

        public void setUserOnline(boolean online_status) {

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_icon);

            if (online_status) {

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }


    }

    private void testDialog(String message) {
        new AlertDialog.Builder(getActivity())
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
    public void onInternetConnectivityChanged(boolean isConnected)
    {
        //do something based on connectivity

        if ( conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ) {

            // notify user you are online
            connectionStatus.setVisibility(View.GONE);
            connectionStatus.setVisibility(View.VISIBLE);
            connectionStatusText.setText("you are now connected to internet");
            connectionStatus.setBackgroundColor(connectionStatus.getResources().getColor(R.color.withInternet));

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    connectionStatus.setVisibility(View.GONE);
                }

            }, 3500);
        }
        else if ( conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED) {

            // notify user you are not online

            connectionStatus.setVisibility(View.VISIBLE);
            connectionStatusText.setText("No internet is connected");
            connectionStatus.setBackgroundColor(connectionStatus.getResources().getColor(R.color.noInternet));


        }


    }



}
