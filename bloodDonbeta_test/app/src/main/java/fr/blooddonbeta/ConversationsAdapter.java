package fr.blooddonbeta;

/**
 * Created by simohaj17 on 1/21/18.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.MyViewHolder>
{
    private Context mContext;
    public ArrayList<Conv> convList;
    public Conv currentConv ;





    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        public TextView fullName, bloodType , distance ;
        public de.hdodenhof.circleimageview.CircleImageView ProfilePicture;
        public boolean seen;
        public long timestamp;

        public MyViewHolder(View view) {
            super(view);
            ProfilePicture = (de.hdodenhof.circleimageview.CircleImageView) view.findViewById(R.id.cardProfilePicture);

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



    public ConversationsAdapter(Context mContext, ArrayList<Conv> convList )
    {
        this.convList = convList;
        this.mContext = mContext;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.conversationitem, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position)
    {
        currentConv = convList.get(position);
        //holder.fullName.setText(currentConv.);
        //holder.bloodType.setText("Type :"+viewHolderCurrentUser.bloodtype);
        //holder.distance.setText(viewHolderCurrentUser.distanceToOther + " km away");
//
    }

    public void newAddeddata(User user)
    {
        if(user != null)
        {
           // this.userList.add(user);
           // notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount()
    {
        return convList.size();
    }


}
