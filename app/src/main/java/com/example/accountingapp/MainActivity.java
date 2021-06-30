package com.example.accountingapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.accountingapp.database.AccountEntry;
import com.example.accountingapp.database.AppDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AccountAdapter.ItemClickListener {

    private static final int VERTICAL = 1;
    // Member variables for the adapter and RecyclerView
    private RecyclerView mRecyclerView;
    private AccountAdapter mAdapter;

    public static long recurring;  // in milliseconds

    private AppDatabase mDb;
    public static boolean notifyUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the RecyclerView to its corresponding view
        mRecyclerView = findViewById(R.id.recyclerViewTasks);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new AccountAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<AccountEntry> accounts = mAdapter.getAccounts();
                        mDb.accountDao().deleteAccount(accounts.get(position));
                        retrieveAccounts();
                    }
                });
            }
        }).attachToRecyclerView(mRecyclerView);

        /*
         Set the Floating Action Button (FAB) to its corresponding View.
         Attach an OnClickListener to it, so that when it's clicked, a new intent will be created
         to launch the AddAccountActivity.
         */
        FloatingActionButton fabButton = findViewById(R.id.fab);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addAccountIntent = new Intent(MainActivity.this, AddAccountActivity.class);
                startActivity(addAccountIntent);
            }
        });

        mDb = AppDatabase.getInstance(getApplicationContext());

        if(notifyUser){
            startAlarmBroadcastReceiver(this);
        }
    }

    /**
     * This method is called after this activity has been paused or restarted.
     * Often, this is after new data has been inserted through an AddAccountActivity,
     * so this re-queries the database data for any changes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        retrieveAccounts();
    }

    private void retrieveAccounts() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<AccountEntry> accounts = mDb.accountDao().loadAllAccounts();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.setAccounts(accounts);
                    }
                });
            }
        });
    }

    @Override
    public void onItemClickListener(int itemId) {
        // Launch AddAccountActivity with itemId as extra for the key AddAccountActivity.EXTRA_ACCOUNT_ID
        Intent intent = new Intent(MainActivity.this, AddAccountActivity.class);
        intent.putExtra(AddAccountActivity.EXTRA_ACCOUNT_ID, itemId);
        startActivity(intent);
    }

    public void startAlarmBroadcastReceiver(Context context) {
        Intent intent = new Intent(this, MyReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC, Calendar.getInstance().getTimeInMillis(), recurring, sender);
    }

    public static class SettingsScreen extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Context hostActivity = getActivity();
            SharedPreferences prefs = hostActivity.getSharedPreferences(hostActivity.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS); // context.getPackageName() + "_preferences" is the name of the sharePreferences file
            boolean notify = prefs.getBoolean("notification_switch",true);
            String notificationInterval = prefs.getString("notify_time_interval", "");
            SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putBoolean("notification_switch", notify);
            prefsEditor.putString("notify_time_interval", notificationInterval);
            prefsEditor.commit();
            //For testing
            Log.d("testing committing change onCreate", "success");
            addPreferencesFromResource(R.xml.settings_screen);
            prefs.registerOnSharedPreferenceChangeListener(this);
            //For testing
            Log.d("testing registering OnSharedPreferenceChangeListener onCreate", "success");
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
            Context hostActivity = getActivity();
            SharedPreferences prefs = hostActivity.getSharedPreferences(hostActivity.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS); // context.getPackageName() + "_preferences" is the name of the sharePreferences file
            boolean notify = prefs.getBoolean("notification_switch",true);
            String notificationInterval = prefs.getString("notify_time_interval", "");
            SwitchPreference ns = (SwitchPreference)findPreference("notification_switch");
            ListPreference ni = (ListPreference)findPreference("notify_time_interval");
            String recurringString = Boolean.toString(notify);
            if (recurringString.equals("true")) {
                ns.setSummary(R.string.Summary_enabled);
            }
            else{
                ns.setSummary(R.string.Summary_disabled);
            }
            ni.setSummary(notificationInterval);

            //For testing
            Log.d("testing registering OnSharedPreferenceChangeListener onResume", "success");
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);

            //For testing
            Log.d("testing unregistering OnSharedPreferenceChangeListener", "success");
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            //For testing
            Log.d("onSharedPreferenceChanged is called", "success");

            boolean notify = prefs.getBoolean("notification_switch",true);
            String notificationInterval = prefs.getString("notify_time_interval", "");
            SharedPreferences.Editor prefsEditor = prefs.edit();
            SwitchPreference ns = (SwitchPreference)findPreference("notification_switch");
            ListPreference ni = (ListPreference)findPreference("notify_time_interval");

            if (notify) {
                notifyUser = true;
                ns.setSummary(R.string.Summary_enabled);

                //For testing
                Log.d("notification_switch is now true", "success");
            }
            else {
                notifyUser = false;
                ns.setSummary(R.string.Summary_disabled);

                //For testing
                Log.d("notification_switch is now false", "success");
            }

            switch (notificationInterval) {
                case "1 minute":
                    recurring = 60000;
                    ni.setSummary("1 minute");

                    //For testing
                    Log.d("notify_time_interval is now 1 minute", "success");
                    break;
                case "30 minutes":
                    recurring = 1800000;
                    ni.setSummary("30 minute");

                    //For testing
                    Log.d("notify_time_interval is now 30 minutes", "success");
                    break;
                case "1 hour":
                    recurring = 3600000;
                    ni.setSummary("1 hour");

                    //For testing
                    Log.d("notify_time_interval is now 1 hour", "success");
                    break;
                case "5 hours":
                    recurring = 18000000;
                    ni.setSummary("5 hours");

                    //For testing
                    Log.d("notify_time_interval is now 5 hours", "success");
                    break;
                case "24 hours":
                    recurring = 86400000;
                    ni.setSummary("24 hours");

                    //For testing
                    Log.d("notify_time_interval is now 24 hours", "success");
                    break;
            }

            //For testing
            Log.d("testing registering OnSharedPreferenceChangeListener onCreate", "success");

            prefsEditor.putBoolean("notification_switch", notify);
            prefsEditor.putString("notify_time_interval", notificationInterval);
            prefsEditor.commit();

            //For testing
            Log.d("testing committing after changing settings", "success");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        ViewGroup parentView = getWindow().getDecorView().findViewById(android.R.id.content);
        if (id == R.id.mysettings) {
            parentView.removeAllViews();
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            SettingsScreen myPreference = new SettingsScreen();
            transaction.replace(android.R.id.content, myPreference, "MY_FRAGMENT"); //replace content with myPreference
            transaction.commit();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        FragmentManager manager = getFragmentManager();
        Fragment ft = manager.findFragmentByTag("MY_FRAGMENT");
        if(ft.isVisible())
        {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        FragmentManager manager = getFragmentManager();
        Fragment ft = manager.findFragmentByTag("MY_FRAGMENT");
        if(ft != null && !ft.isVisible())
        {
            retrieveAccounts();
        }
    }
}