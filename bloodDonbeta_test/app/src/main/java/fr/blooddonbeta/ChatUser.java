package fr.blooddonbeta;

/**
 * Created by simohaj17 on 4/7/18.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.location.Location;
import android.widget.TextView;

import com.github.bassaer.chatmessageview.model.IChatUser;
import com.google.firebase.database.DataSnapshot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ChatUser implements IChatUser {
    Integer id;
    String name;
    Bitmap icon;
    String gender;
    String birthday;
    String bloodtype;
    String weight;
    String userId;
    Location userLocation;
    String   profileUrl;
    String   otherUserProfileUrl;


    public ChatUser(int id, String name, Bitmap icon)
    {
        this.id            = id;
        this.name          = name;
        this.icon          = icon;

    }

    public void setFromHashMap(HashMap<String,String> myHM,int userNumber)
    {
        if(userNumber == 0)
        {
            name        =myHM.get("CurrentUserFullname" );
            birthday    =myHM.get("CurrentUserBirthday" );
            bloodtype   =myHM.get("CurrentUserBloodtype");
            gender      =myHM.get("CurrentUserGender"   );
            weight      =myHM.get("CurrentUserWeight"   );
            userId      =myHM.get("CurrentUserUserId"   );
            profileUrl  =myHM.get("CurrentProfileUrl"   );


        }else
        {
             name      =myHM.get("viewHolderCurrentUserFullname" );
             birthday  =myHM.get("viewHolderCurrentUserBirthday" );
             bloodtype =myHM.get("viewHolderCurrentUserBloodtype");
             gender    =myHM.get("viewHolderCurrentUserGender"   );
             weight    =myHM.get("viewHolderCurrentUserWeight"   );
             userId    =myHM.get("viewHolderCurrentUserUserId"   );
             otherUserProfileUrl = myHM.get("otherUserPicture" );


        }


    }

    public void setFromDataSnapshot(DataSnapshot dataSnapshot, int userNumber , TextView mContext)
    {
        if(userNumber == 0 && dataSnapshot.child("fullname").getValue() != null )
        {
            mContext.setText(dataSnapshot.child("fullname").getValue().toString());
            name        =   dataSnapshot.child("fullname").getValue().toString();
            birthday    =   dataSnapshot.child("birthday").getValue().toString();
            bloodtype   =   dataSnapshot.child("bloodtype").getValue().toString();
            gender      =   dataSnapshot.child("gender").getValue().toString();
            weight      =   dataSnapshot.child("weight").getValue().toString();
            profileUrl  =   dataSnapshot.child("profileUrl").getValue().toString();




        }else if((userNumber == 1 && dataSnapshot.child("fullname").getValue() != null ))
        {
            mContext.setText(dataSnapshot.child("fullname").getValue().toString());
            name        =   dataSnapshot.child("fullname").getValue().toString();
            birthday    =   dataSnapshot.child("birthday").getValue().toString();
            bloodtype   =   dataSnapshot.child("bloodtype").getValue().toString();
            gender      =   dataSnapshot.child("gender").getValue().toString();
            weight      =   dataSnapshot.child("weight").getValue().toString();
            otherUserProfileUrl  =   dataSnapshot.child("profileUrl").getValue().toString();


        }


    }

    @NotNull
    @Override
    public String getId() {
        return this.id.toString();
    }

    @Nullable
    @Override
    public String getName() {
        return this.name;
    }

    @Nullable
    @Override
    public Bitmap getIcon() {
        return this.icon;
    }

    @Override
    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public String getGender() {
        return gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getBloodtype() {
        return bloodtype;
    }

    public String getWeight() {
        return weight;
    }

    public String getUserId() {
        return userId;
    }

    public Location getUserLocation() {
        return userLocation;
    }

    public String getProfileUrl() {
        return profileUrl;
    }


    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setBloodtype(String bloodtype) {
        this.bloodtype = bloodtype;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }


    public String getOtherUserProfileUrl() {
        return otherUserProfileUrl;
    }

    public void setOtherUserProfileUrl(String otherUserProfileUrl) {
        this.otherUserProfileUrl = otherUserProfileUrl;
    }

}