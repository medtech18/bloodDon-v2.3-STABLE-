package fr.blooddonbeta;

/**
 * Created by simohaj17 on 1/21/18.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.MyViewHolder>
{
    private Context mContext;
    private Location mlastLocation;
    public ArrayList<User> userList;
    private Location otherUserLocation;
    private User viewHolderCurrentUser;
    public User currentUser;





    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        public TextView fullName, bloodType , distance ;
        public ImageButton mapButton, messageButton;
        public de.hdodenhof.circleimageview.CircleImageView ProfilePicture;
        public ImageButton sendMessageButton;
        public int pos;
        public User uniqueViewHolderCurrentUser;
        public Bitmap defaultPicture;

        public MyViewHolder(View view) {
            super(view);
            fullName = (TextView) view.findViewById(R.id.cardFullName);
            bloodType = (TextView) view.findViewById(R.id.cardBloodType);
            distance = (TextView) view.findViewById(R.id.cardDistance);
            mapButton = (ImageButton) view.findViewById(R.id.mapButton);
            messageButton = (ImageButton) view.findViewById(R.id.messageButton);
            ProfilePicture = (de.hdodenhof.circleimageview.CircleImageView) view.findViewById(R.id.cardProfilePicture);
            sendMessageButton = (ImageButton) view.findViewById(R.id.sendMessageButton);

            mapButton = (ImageButton)view.findViewById(R.id.mapButton);
            mapButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    SearchActivity userhome = (SearchActivity)mContext;
                    userhome.setMapsButtonAndActivity(uniqueViewHolderCurrentUser.getUserLocation());
                }
            });

            sendMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Intent sendMessageIntent = new Intent(mContext, ChatActivity.class);

                    HashMap<String,String> myHM  = new HashMap<String,String>();

                    myHM.put("CurrentUserFullname", currentUser.getfullname());
                    myHM.put("CurrentUserBirthday", currentUser.birthday);
                    myHM.put("CurrentUserBloodtype", currentUser.bloodtype);
                    sendMessageIntent.putExtra("CurrentUserUserLocation",currentUser.getUserLocation() );
                    myHM.put("CurrentUserGender", currentUser.gender);
                    myHM.put("CurrentUserWeight", currentUser.getWeight());
                    myHM.put("CurrentUserUserId", currentUser.userId);
                    myHM.put("CurrentProfileUrl", currentUser.getProfileUrl());


                    myHM.put("viewHolderCurrentUserFullname", uniqueViewHolderCurrentUser.getfullname());
                    myHM.put("viewHolderCurrentUserBirthday", uniqueViewHolderCurrentUser.birthday);
                    myHM.put("viewHolderCurrentUserBloodtype", uniqueViewHolderCurrentUser.bloodtype);
                    sendMessageIntent.putExtra("viewHolderCurrentUserUserLocation",uniqueViewHolderCurrentUser.getUserLocation() );
                    myHM.put("viewHolderCurrentUserGender", uniqueViewHolderCurrentUser.gender);
                    myHM.put("viewHolderCurrentUserWeight", uniqueViewHolderCurrentUser.getWeight());
                    myHM.put("viewHolderCurrentUserUserId", uniqueViewHolderCurrentUser.userId);
                    myHM.put("otherUserPicture", uniqueViewHolderCurrentUser.profileUrl);


                    sendMessageIntent.putExtra("myHM",myHM);
                    sendMessageIntent.putExtra("sentFromSearchResult",true);
                    mContext.startActivity(sendMessageIntent);




                }
            });

        }
    }

    private void testDialog(String message) {
        new android.support.v7.app.AlertDialog.Builder(mContext)
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



    public SearchResultAdapter(Context mContext,ArrayList<User> userList )
    {
        this.userList = userList;
        this.mContext = mContext;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position)
    {
        viewHolderCurrentUser = holder.uniqueViewHolderCurrentUser = userList.get(position);
        holder.fullName.setText(viewHolderCurrentUser.fullname);
        holder.bloodType.setText("Type :"+viewHolderCurrentUser.bloodtype);
        holder.distance.setText(viewHolderCurrentUser.distanceToOther + " km away");
      try
      {
              FirebaseStorage storage = FirebaseStorage.getInstance();

              if(viewHolderCurrentUser.profileUrl != null && !viewHolderCurrentUser.profileUrl.isEmpty())
              {
                 StorageReference gsReference = storage.getReferenceFromUrl(viewHolderCurrentUser.profileUrl);
                 //Glide.with(holder.ProfilePicture.getContext())
                 //        .using(new FirebaseImageLoader())
                 //        .load(gsReference)
                 //        .into(holder.ProfilePicture);

                 Glide
                         .with(holder.ProfilePicture.getContext())
                         .using(new FirebaseImageLoader())
                         .load(gsReference)
                         .asBitmap()
                         .error(holder.itemView.getResources().getDrawable(R.drawable.defaultprofileimage))
                         .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                         .into(new SimpleTarget<Bitmap>() {
                             @Override
                             public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                 holder.ProfilePicture.setImageBitmap(resource);
                             }
                         });




              }


             // Glide.with(holder.ProfilePicture.getContext()).load(gsReference).into(holder.ProfilePicture);


      }catch (Exception e)
      {
          e.printStackTrace();
      }


    }

    public void newAddeddata(User user)
    {
        if(user != null)
        {
            this.userList.add(user);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount()
    {
        return userList.size();
    }


}
