package fr.blooddonbeta; /**
 * Created by simohaj17 on 1/22/18.
 */

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SearchResultFragment extends android.app.Fragment
{
    private Context mContext;
    private RecyclerView recyclerView;
    private SearchResultAdapter adapter;
    private ArrayList<User> userList;
    private static final String TAG = "SearchResultFragment";
    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.search_list, container, false);
        rootView.setTag(TAG);

        try
        {

            // Context
            mContext = getActivity();
            rootView = inflater.inflate(R.layout.search_list, container, false);


            userList = new ArrayList<User>();
            //userList.add( new User(" ","","","","",""));
           // Bundle b = this.getArguments();
           // userList.add((User) b.getParcelable("user"));



            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
            mRecyclerView.setHasFixedSize(true);

            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());

            adapter = new SearchResultAdapter(getActivity(),userList);

            mRecyclerView.setAdapter(adapter);

        }catch (Exception e)
        {
            e.printStackTrace();
        }


        return rootView;
    }

    public SearchResultAdapter getAdapter(){
        return adapter;
    }

    public void pushNewUser(User u)
    {
        if(adapter != null)
        {
            adapter.userList.add(u);
            //adapter.newAddeddata(u);
            adapter.notifyDataSetChanged();
        }
    }

    public void setCurrentUser(User currentUser)
    {
        adapter.currentUser = currentUser;
    }
}
