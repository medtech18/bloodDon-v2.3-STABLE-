package fr.blooddonbeta;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class SettingsActivity extends AppCompatPreferenceActivity
{
    private static String userId;
    private static DatabaseReference dRef;


    private static User curentUserInfo;


    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");

        // load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();


        userId =FirebaseAuth.getInstance().getCurrentUser().getUid();
        dRef = FirebaseDatabase.getInstance().getReference();
        dRef.keepSynced(true);
        createMyCurrentInfoListener(userId);


    }

    public void createMyCurrentInfoListener(final String key )
    {
        FirebaseDatabase.getInstance().getReference("users").child(key).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    curentUserInfo = new User(  (String) snapshot.child("fullname").getValue(),
                                                (String) snapshot.child("gender").getValue(),
                                                (String) snapshot.child("birthday").getValue(),
                                                (String) snapshot.child("bloodtype").getValue(),
                                                (String) snapshot.child("weight").getValue(),
                                                (String) snapshot.child("profileUrl").getValue(),
                                                (String) snapshot.child("myDonors").getValue(),
                                                (String) snapshot.child("myRecipients").getValue(),
                                                (String) snapshot.child("isProfileCreated").getValue(),
                                            key);



                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                databaseError.getMessage();
            }
        });

    }

    public static class MainPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

            final DatePreference dp= (DatePreference) findPreference("key_birthday");
            dp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference,Object newValue) {
                    //your code to change values.
                    dp.setSummary((String) newValue);
                    try {
                        dRef.child("users").child(userId).child("birthday").setValue((String) newValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return true;
                }
            });




            EditTextPreference fullnamePref = (EditTextPreference) findPreference("key_fullName");
            // gallery EditText change listener
            bindPreferenceSummaryToValue(fullnamePref);


            ListPreference bloodType = (ListPreference)findPreference(getString(R.string.key_bloodType));
            bloodType.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            ListPreference gender = (ListPreference)findPreference(getString(R.string.key_gender));
            gender.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);





            if(curentUserInfo != null)
            {
                fullnamePref.setText(curentUserInfo.fullname);
                bloodType.setValue(curentUserInfo.bloodtype);
                bloodType.setSummary(curentUserInfo.bloodtype);
                gender.setSummary(curentUserInfo.gender);

                //calculate Age
                dp.setText(curentUserInfo.birthday);
                dp.setSummary(curentUserInfo.birthday);



            }






           // bindPreferenceSummaryToValue(findPreference(getString(R.string.key_upload_quality)));






            // notification preference change listener
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_notifications_new_message_ringtone)));

            // feedback preference click listener
            Preference myPref = findPreference(getString(R.string.key_send_feedback));
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    sendFeedback(getActivity());
                    return true;
                }
            });
        }


    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue)
        {
            String stringValue = newValue.toString();



            if (preference instanceof ListPreference)
            {
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(stringValue);

                    // Set the summary to reflect the new value.
                    preference.setSummary(
                            index >= 0
                                    ? listPreference.getEntries()[index]
                                    : null);

                if (preference.getKey().equals("key_gender"))
                {
                    try {
                        dRef.child("users").child(userId).child("gender").setValue(stringValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (preference.getKey().equals("key_bloodType"))
                {
                    try {
                        dRef.child("users").child(userId).child("bloodtype").setValue(stringValue);
                        dRef.child("users").child(userId).child("myDonors").setValue(MatchBloodType.getDonorsOf(stringValue));
                        dRef.child("users").child(userId).child("myRecipients").setValue(MatchBloodType.getRecipientOf(stringValue));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }





            } else if (preference instanceof RingtonePreference)
            {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(R.string.summary_choose_ringtone);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else if (preference instanceof EditTextPreference)
            {
                if (preference.getKey().equals("key_fullName"))
                {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);

                    try {
                        dRef.child("users").child(userId).child("fullname").setValue(stringValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            return true;
        }
    };

    /**
     * Email client intent to send support mail
     * Appends the necessary device information to email body
     * useful when providing support
     */
    public static void sendFeedback(Context context) {
        String body = null;
        try {
            body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
        } catch (PackageManager.NameNotFoundException e) {
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"contact@androidhive.info"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Query from android app");
        intent.putExtra(Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.choose_email_client)));
    }




}
