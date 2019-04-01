package fr.blooddonbeta;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by simohaj17 on 1/9/18.
 */

public class User implements Parcelable
{


    public String   fullname ;
    public String   gender;
    public String   birthday;
    public String   bloodtype;
    public String   weight;
    public String   profileUrl;
    public String   distanceToOther;
    public String   userId;

    public Location userLocation;
    public String isProfileCreated;


    public String   myDonor ;
    public String   myRecipient ;
    private Map<String, String> map;

    //private String distanceToOther;


    //public String getDistanceToOther() {
    //    return distanceToOther;
    //}

    public User()
    {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String fullname, String gender , String birthday, String bloodtype , String weight , String profileUrl , String myDonor , String myRecipient , String isProfileCreated , String userId)
    {
        this.fullname       = fullname;
        this.gender         = gender;
        this.birthday       = birthday;
        this.bloodtype      = bloodtype;
        this.weight         = weight;
        this.profileUrl     = profileUrl;
        this.myDonor        = myDonor;
        this.myRecipient    = myRecipient;
        this.isProfileCreated = isProfileCreated;
        this.userId = userId;
        this.map = new HashMap<String, String>();

    }

    public User(Parcel in)
    {
        this.userLocation = in.readParcelable(Location.class.getClassLoader());
        this.fullname    = in.readString();
        this.gender      = in.readString();
        this.birthday    = in.readString();
        this.bloodtype   = in.readString();
        this.weight      = in.readString();
        this.profileUrl  = in.readString();
        this.myDonor     = in.readString();
        this.myRecipient =  in.readString();
        this.isProfileCreated = in.readString();
        this.userId = in.readString();


    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(this.userLocation, flags);
        dest.writeString(fullname );
        dest.writeString(gender);
        dest.writeString(birthday);
        dest.writeString(bloodtype);
        dest.writeString(weight);
        dest.writeString(profileUrl);
        dest.writeString(myDonor);
        dest.writeString(myRecipient);
        dest.writeString(isProfileCreated);

        dest.writeString(distanceToOther);
        dest.writeString(userId);
        dest.writeMap(map);
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };


    public void setDistanceToOther(Float distanceToOther)
    {
        this.distanceToOther = String.valueOf(new DecimalFormat("##.#").format(distanceToOther));
    }

    public void setUserLocation(Location userLocation)
    {
        this.userLocation = userLocation;
    }

    public Map<String, String> getUserObject()
    {
        map = new HashMap<>();
        map.put("fullname",  fullname);
        map.put("gender",    gender);
        map.put("birthday",  birthday);
        map.put("bloodtype", bloodtype);
        map.put("weight",    weight);
        map.put("profileUrl",profileUrl);
        map.put("myDonors",myDonor);
        map.put("myRecipients",myRecipient);
        map.put("isProfileCreated",isProfileCreated);

        return map;
    }


    public String getfullname() {
        return fullname;
    }

    public void setfullname(String fullname) {
        this.fullname = fullname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getbloodtype() {
        return bloodtype;
    }

    public void setbloodtype(String bloodtype) {
        this.bloodtype = bloodtype;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getDistanceToOther() {
        return distanceToOther;
    }

    public Location getUserLocation() {
        return userLocation;
    }


    public String getMyDonor() {
        return myDonor;
    }

    public void setMyDonor(String myDonor) {
        this.myDonor = myDonor;
    }

    public String getMyRecipient() {
        return myRecipient;
    }

    public void setMyRecipient(String myRecipient) {
        this.myRecipient = myRecipient;
    }


}
