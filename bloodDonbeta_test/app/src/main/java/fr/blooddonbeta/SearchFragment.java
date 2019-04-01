package fr.blooddonbeta;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class SearchFragment extends android.app.Fragment
{


    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState)
    {
        View inputFragmentView = inflater.inflate(R.layout.fragment_search,container, false);

        belka.us.androidtoggleswitch.widgets.ToggleSwitch toggleSwitch;


        toggleSwitch = inputFragmentView.findViewById(R.id.donationSwitcher);

        toggleSwitch.setCheckedTogglePosition(SearchActivity.userOperation);

        toggleSwitch.setOnToggleSwitchChangeListener(new belka.us.androidtoggleswitch.widgets.ToggleSwitch.OnToggleSwitchChangeListener(){

            @Override
            public void onToggleSwitchChangeListener(int position, boolean isChecked)
            {
                SearchActivity.userOperation = position;
            }
        });
        return inputFragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);
    }




}
