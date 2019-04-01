package fr.blooddonbeta;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.github.bassaer.chatmessageview.model.IChatUser;
import com.github.bassaer.chatmessageview.model.Message;
import com.github.bassaer.chatmessageview.util.ChatBot;
import com.github.bassaer.chatmessageview.view.ChatView;
import com.github.bassaer.chatmessageview.view.MessageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

import java.io.IOException;
import java.security.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import fr.blooddonbeta.R;
import fr.blooddonbeta.User;




public class ChatActivity extends AppCompatActivity implements InternetConnectivityListener
{

    @VisibleForTesting
    protected static final int RIGHT_BUBBLE_COLOR = R.color.colorAccent;
    @VisibleForTesting
    protected static final int LEFT_BUBBLE_COLOR = R.color.gray300;
    @VisibleForTesting
    protected static final int BACKGROUND_COLOR = R.color.white;
    @VisibleForTesting
    protected static final int SEND_BUTTON_COLOR = R.color.blueGray500;
    @VisibleForTesting
    protected static final int SEND_ICON = R.drawable.ic_action_send;
    @VisibleForTesting
    protected static final int OPTION_BUTTON_COLOR = R.color.teal500;
    @VisibleForTesting
    protected static final int RIGHT_MESSAGE_TEXT_COLOR = Color.WHITE;
    @VisibleForTesting
    protected static final int LEFT_MESSAGE_TEXT_COLOR = Color.BLACK;
    @VisibleForTesting
    protected static final int USERNAME_TEXT_COLOR = Color.WHITE;
    @VisibleForTesting
    protected static final int SEND_TIME_TEXT_COLOR = Color.WHITE;
    @VisibleForTesting
    protected static final int DATA_SEPARATOR_COLOR = Color.WHITE;
    @VisibleForTesting
    protected static final int MESSAGE_STATUS_TEXT_COLOR = Color.WHITE;
    @VisibleForTesting
    protected static final String INPUT_TEXT_HINT = "New message..";
    @VisibleForTesting
    protected static final int MESSAGE_MARGIN = 5;

    private ChatView mChatView;
    private MessageList mMessageList;
    private ArrayList<ChatUser> mUsers;

    private int mReplyDelay = -1;

    private static final int READ_REQUEST_CODE = 100;



    /// Firebase

    private DatabaseReference users;
    private DatabaseReference rootRef;
    private DatabaseReference onlineRef;
    private DatabaseReference isOnline;
    DatabaseReference connectedRef;

    private DatabaseReference messages;
    private String userId;
    private FirebaseUser currentUser;
    private FirebaseStorage storage ;
    private DatabaseReference otherUserMessageRef ;
    private DatabaseReference chatRef ;
    private DatabaseReference mNotificationRefence ;


    private DatabaseReference LastMessageRef;



    //StorageReference gsReference;


    // Chat Components
    private DateFormat df ;
    private com.github.bassaer.chatmessageview.view.ChatView mChatRecyleView ;

    // User status  componant

    private TextView chatUserStatus;
    private TextView chatUserFullName;
    private de.hdodenhof.circleimageview.CircleImageView youPicture;
    private SwipeRefreshLayout  mSwipeRefreshLayout;



    // Users Variable
    User currentUserObject;
    User viewHolderCurrentUserObject;
    private  ChatUser me;
    private ChatUser you;


    // get Messages Limit Variables

    private static  int TOTAL_ITEMS_TO_LOAD = 10 ;
    private static int mCurrentPage = 1;



    private  ArrayList<Message>  messagesList = new ArrayList();

    ChildEventListener newListener;

    InternetAvailabilityChecker mInternetAvailabilityChecker;


    private String lastSeen = "";

    private ValueEventListener statusListener;
    private ValueEventListener LastMessageRefListener;

    ConnectivityManager conMgr ;


    private String myLastSentMessageHash = "";

    private int notSeenMessageCounter = 0 ;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        SearchActivity.anotherActivityOpened = true;

        chatUserFullName = (TextView) findViewById(R.id.chatUserName);

        initUsers();

        /// Firebase
        users = FirebaseDatabase.getInstance().getReference("users");
        messages = FirebaseDatabase.getInstance().getReference("messages");
        rootRef = users = FirebaseDatabase.getInstance().getReference();
        onlineRef = FirebaseDatabase.getInstance().getReference("users").child(you.getUserId()).child("online");
        mNotificationRefence = FirebaseDatabase.getInstance().getReference("notifications");

        //connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser.getUid();
        isOnline = FirebaseDatabase.getInstance().getReference("users").child(userId).child("online");
        isOnline.onDisconnect().setValue(ServerValue.TIMESTAMP);

        conMgr = (ConnectivityManager)getSystemService(this.CONNECTIVITY_SERVICE);


        //connectedRef.addValueEventListener(new ValueEventListener() {
        //    @Override
        //    public void onDataChange(DataSnapshot snapshot) {
        //        boolean connected = snapshot.getValue(Boolean.class);
        //        if (connected) {
        //            isOnline.setValue(true);
        //        } else {
        //            chatUserStatus.setText("");
        //        }
        //    }

        //    @Override
        //    public void onCancelled(DatabaseError error) {
        //        System.err.println("Listener was cancelled");
        //    }
        //});


        //storage = FirebaseStorage.getInstance();


        // USER HEADER

        df = new SimpleDateFormat("HH:mm");


        mChatRecyleView = findViewById(R.id.chat_view);
        mSwipeRefreshLayout = findViewById(R.id.chatContainer);
        chatUserStatus = (TextView) findViewById(R.id.chatUserStatus);

        chatUserFullName.setText(you.getName());

        youPicture = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.chatUserPicture);


        if (you.getOtherUserProfileUrl() != null && !you.getOtherUserProfileUrl().isEmpty()) {
            //Glide.with(holder.ProfilePicture.getContext())
            //        .using(new FirebaseImageLoader())
            //        .load(gsReference)
            //        .into(holder.ProfilePicture);

            try {
                StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(you.getOtherUserProfileUrl());

                Glide
                        .with(this)
                        .using(new FirebaseImageLoader())
                        .load(gsReference)
                        .asBitmap()
                        .error(this.getResources().getDrawable(R.drawable.defaultprofileimage))
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                youPicture.setImageBitmap(resource);
                            }
                        });

            } catch (Exception e) {
                e.printStackTrace();
            }


        }


        onlineRef.keepSynced(true);
        setStatusListener();



        mChatView = findViewById(R.id.chat_view);

        //Load saved messages
        loadMessages();

        //Set UI parameters if you need
        mChatView.setRightBubbleColor(ContextCompat.getColor(this, RIGHT_BUBBLE_COLOR));
        mChatView.setLeftBubbleColor(ContextCompat.getColor(this, LEFT_BUBBLE_COLOR));
        mChatView.setBackgroundColor(ContextCompat.getColor(this, BACKGROUND_COLOR));
        mChatView.setSendButtonColor(ContextCompat.getColor(this, SEND_BUTTON_COLOR));
        mChatView.setSendIcon(SEND_ICON);
        mChatView.setOptionIcon(R.drawable.ic_account_circle);
        mChatView.setOptionButtonColor(OPTION_BUTTON_COLOR);
        mChatView.setRightMessageTextColor(RIGHT_MESSAGE_TEXT_COLOR);
        mChatView.setLeftMessageTextColor(LEFT_MESSAGE_TEXT_COLOR);
        mChatView.setUsernameTextColor(USERNAME_TEXT_COLOR);
        mChatView.setSendTimeTextColor(SEND_TIME_TEXT_COLOR);
        mChatView.setDateSeparatorColor(DATA_SEPARATOR_COLOR);
        mChatView.setMessageStatusTextColor(MESSAGE_STATUS_TEXT_COLOR);
        mChatView.setInputTextHint(INPUT_TEXT_HINT);
        mChatView.setMessageMarginTop(MESSAGE_MARGIN);
        mChatView.setMessageMarginBottom(MESSAGE_MARGIN);
        mChatView.setMaxInputLine(5);
        mChatView.setMessageMaxWidth(600);
        mChatView.setUsernameFontSize(getResources().getDimension(R.dimen.font_small));
        mChatView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mChatView.setInputTextColor(ContextCompat.getColor(this, R.color.red500));
        mChatView.setInputTextSize(TypedValue.COMPLEX_UNIT_SP, 20);


        //====================================[ CUSTOMS ] ==========================================

        mChatView.setOnBubbleClickListener(new Message.OnBubbleClickListener() {
            @Override
            public void onClick(Message message) {
                mChatView.updateMessageStatus(message, MyMessageStatusFormatter.STATUS_SEEN);
                Toast.makeText(
                        ChatActivity.this,
                        "click : " + message.getUser().getName() + " - " + message.getText(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        mChatView.setOnBubbleLongClickListener(new Message.OnBubbleLongClickListener() {
            @Override
            public void onLongClick(Message message) {
                Toast.makeText(
                        ChatActivity.this,
                        "Removed this message \n" + message.getText(),
                        Toast.LENGTH_SHORT
                ).show();
                mChatView.getMessageView().remove(message);
            }
        });

        mChatView.setOnIconClickListener(new Message.OnIconClickListener() {
            @Override
            public void onIconClick(Message message) {
                Toast.makeText(
                        ChatActivity.this,
                        "click : icon " + message.getUser().getName(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        mChatView.setOnIconLongClickListener(new Message.OnIconLongClickListener() {
            @Override
            public void onIconLongClick(Message message) {
                Toast.makeText(
                        ChatActivity.this,
                        "Long click : icon " + message.getUser().getName(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });


        //====================================[ --------- ] ==========================================


        //Click Send Button
        mChatView.setOnClickSendButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initUsers();
                //new message
                Message message = new Message.Builder()
                        .setUser(mUsers.get(0))
                        .setRight(true)
                        .setText(mChatView.getInputText())
                        .hideIcon(true)
                        .setStatusIconFormatter(new MyMessageStatusFormatter(ChatActivity.this))
                        .setStatusTextFormatter(new MyMessageStatusFormatter(ChatActivity.this))
                        .setStatusStyle(Message.Companion.getSTATUS_ICON())
                        .setStatus(MyMessageStatusFormatter.STATUS_DELIVERED)
                        .build();

                //Set to chat view
                //mChatView.send(message);
                //Add message list
                //mMessageList.add(message);

                // send message to FIREBASE
                sendMessage(mChatView.getInputText() );


                //Reset edit text
                mChatView.setInputText("");

                //receiveMessage(message.getText());

            }

        });

        // Refrech Recyle view Listener

        //mSwipeRefreshLayout.setRefreshing(false);
        //mSwipeRefreshLayout.setEnabled(false);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setEnabled(true);

                mCurrentPage++;
                mChatView.getMessageView().removeAll();
                loadMessagesForSignleTime();
            }
        });

        //Click option button
        mChatView.setOnClickOptionButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        // mChatView.setRefreshing(true);
        // mChatView.setEnableSwipeRefresh(true);
//
        // mChatView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        //     @Override
        //     public void onRefresh() {
//
        //         mCurrentPage ++ ;
        //         mChatView.getMessageView().removeAll();
        //         loadMessagesForSignleTime();
        //     }
        // });


        mChatView.getMessageView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mChatView.getMessageView().getChildAt(0) != null) {
                    mSwipeRefreshLayout.setEnabled(mChatView.getMessageView().getFirstVisiblePosition() == 0 && mChatView.getMessageView().getChildAt(0).getTop() == 0);
                }
            }
        });

        InternetAvailabilityChecker.init(this);

        mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
        mInternetAvailabilityChecker.addInternetConnectivityListener(this);



    }

    @Override
    public void onStart() {
        super.onStart();

        isOnline.setValue(true);

    }

    @Override
    public void onResume() {
        super.onResume();
        initUsers();

        isOnline.setValue(true);

    }

    @Override
    public void onStop()
    {
        super.onStop();
        SearchActivity.anotherActivityOpened = false;

        mInternetAvailabilityChecker.removeInternetConnectivityChangeListener(this);


    }



    @Override
    public void onPause() {
        super.onPause();
        isOnline.setValue(System.currentTimeMillis());
        //mMessageList = new MessageList();
        //mMessageList.setMessages(mChatView.getMessageView().getMessageList());
        //AppData.putMessageList(this, mMessageList);

    }

    private void openGallery() {
        Intent intent;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    public void setStatusListener()
    {
        if(statusListener!= null)
            onlineRef.removeEventListener(statusListener);
        statusListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) ;
                {

                    if (dataSnapshot.getValue() != null) {
                        if (dataSnapshot.getValue().equals(true)) {
                            chatUserStatus.setText("Online");


                        } else if (dataSnapshot.getValue().equals(false)) {
                            chatUserStatus.setText("");
                            lastSeen = "";
                        } else {

                            try {
                                Long test = (Long) dataSnapshot.getValue();
                                if (test != null) {
                                    lastSeen = GetTimeAgo.getTimeAgo(test);

                                    if (lastSeen != null) {
                                        chatUserStatus.setText(lastSeen);
                                    } else {
                                        chatUserStatus.setText("");
                                    }

                                } else {
                                    chatUserStatus.setText("");

                                }


                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                testDialog("error");
            }
        };

        onlineRef.addValueEventListener(statusListener);
    }

    private void receiveMessage(String sendText)
    {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != READ_REQUEST_CODE || resultCode != RESULT_OK || data == null) {
            return;
        }
        Uri uri = data.getData();
        try {
            Bitmap picture = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            Message message = new Message.Builder()
                    .setRight(true)
                    .setText(Message.Type.PICTURE.name())
                    .setUser(mUsers.get(0))
                    .hideIcon(true)
                    .setPicture(picture)
                    .setType(Message.Type.PICTURE)
                    .setStatusIconFormatter(new MyMessageStatusFormatter(ChatActivity.this))
                    .setStatusStyle(Message.Companion.getSTATUS_ICON())
                    .setStatus(MyMessageStatusFormatter.STATUS_DELIVERED)
                    .build();
            mChatView.send(message);
            //Add message list
            mMessageList.add(message);
            receiveMessage(Message.Type.PICTURE.name());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
        }

    }

    private void initUsers() {
        mUsers = new ArrayList<ChatUser>();
        //User id
        //int myId = 0;
        //User icon
        Bitmap myIcon = BitmapFactory.decodeResource(getResources(), R.drawable.face_2);
        //User name
        //String myName = "Michael";

        //int yourId = 1;
        Bitmap yourIcon = BitmapFactory.decodeResource(getResources(), R.drawable.face_1);
        //String yourName = "Emily";


        // get infromation from extras



        if(getIntent().getExtras().getBoolean("sentFromSearchResult"))
        {
            HashMap<String,String> myHM =  (HashMap<String, String>) getIntent().getSerializableExtra("myHM");

            me = new ChatUser(0, "", myIcon);
            me.setFromHashMap(myHM,0);
            me.setUserLocation((Location)getIntent().getParcelableExtra("CurrentUserUserLocation"));
            you = new ChatUser(1, "", yourIcon);
            you.setFromHashMap(myHM,1);
            you.setUserLocation((Location)getIntent().getParcelableExtra("viewHolderCurrentUserUserLocation"));
        }else if(getIntent().getExtras().getBoolean("sendFromConv"))
        {

            me = new ChatUser(0, "", myIcon);
            you = new ChatUser(1, "", yourIcon);

            me.setUserId(getIntent().getExtras().getString("mCurrent_user_id"));
            you.setUserId(getIntent().getExtras().getString("list_user_id"));



            FirebaseDatabase.getInstance().getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.hasChild(me.getUserId()))
                        me.setFromDataSnapshot(dataSnapshot.child(me.getUserId()),0,chatUserFullName);
                    if(dataSnapshot.hasChild(you.getUserId()))
                        you.setFromDataSnapshot(dataSnapshot.child(you.getUserId()),1,chatUserFullName);


                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });

        }else if(getIntent().getExtras().getBoolean("sentFromNotification"))
        {
            me = new ChatUser(0, "", myIcon);
            you = new ChatUser(1, "", yourIcon);

            me.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
            you.setUserId(getIntent().getExtras().getString("from_user_id"));



            FirebaseDatabase.getInstance().getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.hasChild(me.getUserId()))
                        me.setFromDataSnapshot(dataSnapshot.child(me.getUserId()),0,chatUserFullName);
                    if(dataSnapshot.hasChild(you.getUserId()))
                        you.setFromDataSnapshot(dataSnapshot.child(you.getUserId()),1,chatUserFullName);


                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });

        }

        mUsers.add(me);
        mUsers.add(you);

        chatUserFullName.setText(you.getName());

    }

    /**
     * Load saved messages
     */
    private void loadMessages()
    {
        //testDialog(me.userId + "=====" + you.userId);


        DatabaseReference messageRef = rootRef.child("messages").child(me.userId).child(you.userId) ;

        otherUserMessageRef = rootRef.child("messages").child(you.userId).child(me.userId) ;
        chatRef = rootRef.child("Chat").child(you.userId).child(me.userId) ;


        Query messageQuery = messageRef.limitToLast(5);


        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                if(dataSnapshot.exists() && dataSnapshot != null)
                {
                    if(dataSnapshot.child("right").getValue() != null)
                    {
                        messagesList = new ArrayList<>();

                        final Message message = new Message.Builder()
                                .setUser(mUsers.get(0))
                                .setRight((boolean)dataSnapshot.child("right").getValue())
                                .setText(dataSnapshot.child("message").getValue().toString())
                                .hideIcon(true)
                                .setStatusIconFormatter(new MyMessageStatusFormatter(ChatActivity.this))
                                .setStatusTextFormatter(new MyMessageStatusFormatter(ChatActivity.this))
                                .setStatusStyle(Message.Companion.getSTATUS_ICON())
                                .setStatus(MyMessageStatusFormatter.STATUS_DELIVERED)
                                .build();

                        message.setStatusStyle(Message.Companion.getSTATUS_ICON_RIGHT_ONLY());
                        message.setStatusIconFormatter(new MyMessageStatusFormatter(ChatActivity.this));
                        message.setStatus(MyMessageStatusFormatter.STATUS_DELIVERED);



                        notSeenMessageCounter = 0;



                        // if(dataSnapshot.child("right").getValue().equals(false))
                        // {
                        //     message.hideIcon(false);
                        //     Glide
                        //            .with(ChatActivity.this)
                        //            .using(new FirebaseImageLoader())
                        //            .load(gsReference)
                        //            .asBitmap()
                        //            .error(ChatActivity.this.getResources().getDrawable(R.drawable.defaultprofileimage))
                        //            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        //            .into(new SimpleTarget<Bitmap>() {
                        //                @Override
                        //                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation)
                        //                {
                        //                    message.getUser().setIcon(resource);
                        //                }
                        //            });
                        // }

                        //mChatView.send(message);

                        myLastSentMessageHash = dataSnapshot.getKey();

                        if((boolean)dataSnapshot.child("right").getValue())
                        {
                            listenToLastMessageIsSeen(myLastSentMessageHash);
                        }

                        if(!(boolean)dataSnapshot.child("right").getValue())
                        {
                            otherUserMessageRef.child(myLastSentMessageHash).child("seen").setValue(true);
                            chatRef.child("seen").setValue(true);
                            chatRef.child("notseenCount").setValue(0);
                        }






                        messagesList.add(message);

                        MessageView messageView = mChatView.getMessageView();
                        messageView.init(messagesList);
                        messageView.setSelection(messageView.getCount() - 1);



                    }
                }
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

    }


    public void listenToLastMessageIsSeen( final String key)
    {
        if(LastMessageRefListener != null)
            LastMessageRef.removeEventListener(LastMessageRefListener);

        if(!key.isEmpty() && key != null)
        {
            LastMessageRef = rootRef.child("messages").child(me.userId).child(you.userId).child(key).child("seen") ;

            LastMessageRefListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.getValue() != null)
                    {
                        if((Boolean)dataSnapshot.getValue())
                        {
                            setMessageStatusForReadMessages();

                            //testDialog("i see your message");

                        }else
                        {
                            //testDialog("i don't see your message");
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            LastMessageRef.addValueEventListener(LastMessageRefListener);

        }

    }



    private void loadMessagesForSignleTime()
    {
        //testDialog(me.userId + "=====" + you.userId);



        DatabaseReference messageRef = rootRef.child("messages").child(me.userId).child(you.userId) ;

        final Query messageQuery = messageRef.limitToLast(mCurrentPage*TOTAL_ITEMS_TO_LOAD);

        otherUserMessageRef = rootRef.child("messages").child(you.userId).child(me.userId) ;


        if(newListener != null)
            messageQuery.removeEventListener(newListener);


        newListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                if(dataSnapshot.exists() && dataSnapshot != null)
                {
                    if(dataSnapshot.child("right").getValue() != null)
                    {
                        messagesList = new ArrayList<>();

                        final Message message = new Message.Builder()
                                .setUser(mUsers.get(0))
                                .setRight((boolean)dataSnapshot.child("right").getValue())
                                .setText(dataSnapshot.child("message").getValue().toString())
                                .hideIcon(true)
                                .setStatusIconFormatter(new MyMessageStatusFormatter(ChatActivity.this))
                                .setStatusTextFormatter(new MyMessageStatusFormatter(ChatActivity.this))
                                .setStatusStyle(Message.Companion.getSTATUS_ICON())
                                .setStatus(MyMessageStatusFormatter.STATUS_DELIVERED)
                                .build();

                        message.setStatusStyle(Message.Companion.getSTATUS_ICON_RIGHT_ONLY());
                        message.setStatusIconFormatter(new MyMessageStatusFormatter(ChatActivity.this));
                        message.setStatus(MyMessageStatusFormatter.STATUS_DELIVERED);


                        otherUserMessageRef.child(dataSnapshot.getKey()).child("seen").setValue(true);


                        // if(dataSnapshot.child("right").getValue().equals(false))
                        // {
                        //     message.hideIcon(false);
                        //     Glide
                        //            .with(ChatActivity.this)
                        //            .using(new FirebaseImageLoader())
                        //            .load(gsReference)
                        //            .asBitmap()
                        //            .error(ChatActivity.this.getResources().getDrawable(R.drawable.defaultprofileimage))
                        //            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        //            .into(new SimpleTarget<Bitmap>() {
                        //                @Override
                        //                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation)
                        //                {
                        //                    message.getUser().setIcon(resource);
                        //                }
                        //            });
                        // }

                        //mChatView.send(message);

                        messagesList.add(message);

                        if((boolean)dataSnapshot.child("right").getValue())
                        {
                            myLastSentMessageHash = dataSnapshot.getKey();
                            listenToLastMessageIsSeen(myLastSentMessageHash);
                        }

                        if(!(boolean)dataSnapshot.child("right").getValue())
                        {
                            chatRef.child("seen").setValue(true);
                            chatRef.child("notseenCount").setValue(0);

                        }


                        MessageView messageView = mChatView.getMessageView();
                        messageView.init(messagesList);
                        messageView.setSelection(messageView.getCount() - 1);

                        //mChatView.setRefreshing(false);
                        mSwipeRefreshLayout.setRefreshing(false);
                        //mSwipeRefreshLayout.setEnabled(false);






                    }
                }
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
        };
        messageQuery.addChildEventListener(newListener);



    }





    @VisibleForTesting
    public ArrayList<ChatUser> getUsers() {
        return mUsers;
    }


    public void setReplyDelay(int replyDelay) {
        mReplyDelay = replyDelay;
    }

    private void showDialog() {
        final String[] items = {
                getString(R.string.send_picture),
                getString(R.string.clear_messages)
        };

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.options))
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        switch (position) {
                            case 0 :
                                openGallery();
                                break;
                            case 1:
                                mChatView.getMessageView().removeAll();
                                break;
                        }
                    }
                })
                .show();
    }

    private void sendMessage(String myMessage)
    {

        if(!TextUtils.isEmpty(myMessage)){

            String current_user_ref = "messages/" + me.userId + "/" + you.userId;
            String chat_user_ref = "messages/" + you.userId + "/" + me.userId;

            DatabaseReference user_message_push = messages.child(me.userId).child(you.userId).push();

            String push_id = user_message_push.getKey();

            Map currentMessageMap = new HashMap();
            currentMessageMap.put("message", myMessage);
            currentMessageMap.put("seen", false);
            currentMessageMap.put("right", true);
            currentMessageMap.put("type", "text");
            currentMessageMap.put("time", ServerValue.TIMESTAMP);
            currentMessageMap.put("from", me.userId);

            Map chatMessageMap = new HashMap();
            chatMessageMap.put("message", myMessage);
            chatMessageMap.put("seen", false);
            chatMessageMap.put("right", false);
            chatMessageMap.put("type", "text");
            chatMessageMap.put("time", ServerValue.TIMESTAMP);
            chatMessageMap.put("from", me.userId);


            HashMap notificationData = new HashMap();
            notificationData.put("from",you.getUserId());
            notificationData.put("type","request");


            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, currentMessageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, chatMessageMap);

            rootRef.child("Chat").child(me.userId).child(you.userId).child("notseenCount").setValue(++notSeenMessageCounter);
            rootRef.child("Chat").child(me.userId).child(you.userId).child("seen").setValue(false);
            rootRef.child("Chat").child(me.userId).child(you.userId).child("timestamp").setValue(ServerValue.TIMESTAMP);

            rootRef.child("Chat").child(you.userId).child(me.userId).child("notseenCount").setValue(notSeenMessageCounter);
            rootRef.child("Chat").child(you.userId).child(me.userId).child("seen").setValue(false);
            rootRef.child("Chat").child(you.userId).child(me.userId).child("timestamp").setValue(ServerValue.TIMESTAMP);


            mNotificationRefence.child(you.getUserId()).push().setValue(notificationData).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {

                }
            });

            rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError != null){

                    }

                }
            });
        }

    }

    private void testDialog(String message) {
        new android.support.v7.app.AlertDialog.Builder(ChatActivity.this)
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
            setStatusListener();

            isOnline.setValue(true);
        }
        else if ( conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED) {

            // notify user you are not online

            chatUserStatus.setText("");


        }

    }


    public void setMessageStatusForReadMessages()
    {
        for(int j = messagesList.size() - 1; j >= 0; j--)
        {
            if(!messagesList.get(j).isRight())
                return;
                messagesList.get(j).setStatus(MyMessageStatusFormatter.STATUS_SEEN);
        }
    }

}


