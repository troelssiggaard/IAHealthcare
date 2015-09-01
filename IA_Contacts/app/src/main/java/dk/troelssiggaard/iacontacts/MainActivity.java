package dk.troelssiggaard.iacontacts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import java.util.ArrayList;
import java.util.List;

import dk.troelssiggaard.iacontacts.database.CurrentPrediction;
import dk.troelssiggaard.iacontacts.estimation.InterruptibilityLogic;

public class MainActivity extends Activity implements CompoundButton.OnCheckedChangeListener {

    protected SlidingUpPanelLayout slidingUpPanelLayout;
    protected ImageView imageView;
    private SharedPreferences sharedPreferences;
    private Switch locationDisabledSwitch;
    private Switch dndPatientRoomSwitch;
    private Switch dndOfficeSwitch;
    private Switch dndMeetingRoomSwitch;
    private Switch dndOperatingTheaterSwitch;
    private Switch dndScannerRoomSwitch;

    // TextViews & ImageView of the users current interruptibility
    protected TextView timeTextView;
    protected TextView activityTextView;
    protected TextView locationTextview;
    protected ImageView interruptibilityImageView;

    // Dummy data for the listView
    private static final int[] profilePicture = new int[]{R.drawable.p4, R.drawable.p2, R.drawable.p3, R.drawable.p5, R.drawable.p6, R.drawable.p10, R.drawable.p9, R.drawable.p8};
    private static final String[] names = new String[]{"Liselotte Fries", "Martin B. Olsen", "Hans Ø. Jensen", "Jason Hattford", "Thorbjorn E. Larsen", "Sophie M. Nielsen", "Malthe Hende", "Richard W. White"};
    private static final String[] titles = new String[]{"Anesthesia", "Senior Medical Adviser", "Surgeon", "Physical Therapy", "Occupational Therapist", "Psychology", "Respiratory Care", "Midwife"};
    private static final String[] departments = new String[]{"Anesthesilogogy, A32", "ICU, Y14", "Orthopaedic, B32", "Orthopaedic, B31", "ICU, Y13", "Maternaty Center, Z12", "ICU, Y13", "Maternaty Center, Z12"};
    private static final int[] intPic = new int[]{R.drawable.other_green, R.drawable.other_yellow, R.drawable.other_red, R.drawable.other_red, R.drawable.other_red, R.drawable.other_green, R.drawable.other_red, R.drawable.other_yellow};

    private CurrentPrediction currentPrediction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         *  USER INTERFACE ELEMENTS
         */
        try {
            getActionBar().hide(); // Hide the ActionBar UI element
        } catch (NullPointerException e) {
            e.getStackTrace();
        }
        // SearchView UI element, set hint text
        SearchView searchView = (SearchView) findViewById(R.id.search);
        searchView.setQueryHint("Search for name, title, room etc...");

        // Buttons: History & Dictation
        Button historybtn = (Button) findViewById(R.id.button);
        historybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivity(intent);
            }
        });
        Button dictationbtn = (Button) findViewById(R.id.button2);
        dictationbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SpeechNoteActivity.class);
                startActivity(intent);
            }
        });


        // Hide SoftKeyboard UI element
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // SlideUp Panel Layout, to hold user information and settings UI element
        imageView = (ImageView) findViewById(R.id.sliderArrow);
        imageView.setRotation(180);
        imageView.setAlpha(0.4f);
        initializeSlideUpPanel();
        timeTextView = (TextView) findViewById(R.id.textView_10);
        activityTextView = (TextView) findViewById(R.id.textView_8);
        locationTextview = (TextView) findViewById(R.id.textView_9);
        interruptibilityImageView = (ImageView) findViewById(R.id.interruptStatusIcon_1);
        initializeSwitcheListeners();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        loadSwitchSettings();

        // Iterate over dummy data, and put it into the ListView UI element
        List<ListItem> listItems = new ArrayList<ListItem>();
        for (int i = 0; i < names.length; i++) {
            ListItem item = new ListItem(profilePicture[i], names[i], titles[i], departments[i], intPic[i]);
            listItems.add(item);
        }
        ListView listView = (ListView) findViewById(R.id.listView);
        IAAdapter iaAdapter = new IAAdapter(this, listItems);
        listView.setAdapter(iaAdapter);

        // Call buttons for all users
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Do you really want to call?");
                builder.setPositiveButton("Call", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Call the doctor
                        Toast.makeText(getBaseContext(),"Calling doctor...", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing
                    }
                });
                builder.create().show();
            }
        });


        /*
         *  FUNCTIONALITY
         */
        currentPrediction = new CurrentPrediction(this);

        // LocalBroadcastManager handles the messages (intents) from the Sensors
        loadUIBroadcastManager();


        // Start the IAService which is running in the background
        startService(new Intent(this, IAService.class));
    }

    private void loadUIBroadcastManager() {
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                // If localBroadcast has UPDATED_SENSOR = true, then
                // update the UI elements with the new information from the DB
                if (intent.getBooleanExtra(IAService.UPDATED_SENSOR, false)) {


                if(currentPrediction == null) { currentPrediction = new CurrentPrediction(getApplicationContext()); }
                    // Initialize database
                    String activity = currentPrediction.getLatestActivity();
                    String location = currentPrediction.getLatestLocationName();
                    int interruptibility = currentPrediction.getLatestInterruptibilityPrediction();

                    activityTextView.setText("Activity: " + activity);
                    locationTextview.setText("Location: " + location);

                    // Set the image corresponding to the interruptibility integer
                    switch (interruptibility) {
                        case 0:
                            interruptibilityImageView.setImageDrawable(getDrawable(R.drawable.you_yellow));
                            break;
                        case 1:
                            interruptibilityImageView.setImageDrawable(getDrawable(R.drawable.you_green));
                            break;
                        case -1:
                            interruptibilityImageView.setImageDrawable(getDrawable(R.drawable.you_red));
                            break;
                    }
                }

            }
        }, new IntentFilter(IAService.LOCAL_BROADCAST));
    }

    private void initializeSwitcheListeners() {
        locationDisabledSwitch = (Switch) findViewById(R.id.switch1);
        locationDisabledSwitch.setOnCheckedChangeListener(this);
        dndPatientRoomSwitch = (Switch) findViewById(R.id.switch2);
        dndPatientRoomSwitch.setOnCheckedChangeListener(this);
        dndOfficeSwitch = (Switch) findViewById(R.id.switch3);
        dndOfficeSwitch.setOnCheckedChangeListener(this);
        dndMeetingRoomSwitch = (Switch) findViewById(R.id.switch4);
        dndMeetingRoomSwitch.setOnCheckedChangeListener(this);
        dndOperatingTheaterSwitch = (Switch) findViewById(R.id.switch5);
        dndOperatingTheaterSwitch.setOnCheckedChangeListener(this);
        dndScannerRoomSwitch = (Switch) findViewById(R.id.switch6);
        dndScannerRoomSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        switch (buttonView.getId()) {
            case R.id.switch1:
                spEditor.putBoolean("locationDisabled", isChecked).apply();
                break;
            case R.id.switch2:
                spEditor.putBoolean("dndPatientRoom", isChecked).apply();
                break;
            case R.id.switch3:
                spEditor.putBoolean("dndOffice", isChecked).apply();
                break;
            case R.id.switch4:
                spEditor.putBoolean("dndMeetingRoom", isChecked).apply();
                break;
            case R.id.switch5:
                spEditor.putBoolean("dndOperatingTheater", isChecked).apply();
                break;
            case R.id.switch6:
                spEditor.putBoolean("dndScannerRoom", isChecked).apply();
                break;
        }
        new InterruptibilityLogic(this).update();
    }

    private void saveDefaultSettings() {
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        spEditor.putBoolean("FIRST_RUN", false);
        spEditor.putBoolean("locationDisabled", false);
        spEditor.putBoolean("dndPatientRoom", false);
        spEditor.putBoolean("dndOffice", false);
        spEditor.putBoolean("dndMeetingRoom", false);
        spEditor.putBoolean("dndOperatingTheater", false);
        spEditor.putBoolean("dndScannerRoom", false);
        spEditor.apply();
    }

    // Idealy these settings should be loaded from a server, and retrieved when user is logged into the app
    private void loadSwitchSettings() {

        if (sharedPreferences.getBoolean("FIRST_RUN", true)) {
            // Save default settings on first run of the application
            saveDefaultSettings();
        } else {
            // Load the settings from SharedPreferences and asign values to Switches
            boolean locationDisabled = sharedPreferences.getBoolean("locationDisabled", false);
            boolean dndPatientRoom = sharedPreferences.getBoolean("dndPatientRoom", false);
            boolean dndOffice = sharedPreferences.getBoolean("dndOffice", false);
            boolean dndMeetingRoom = sharedPreferences.getBoolean("dndMeetingRoom", false);
            boolean dndOperatingTheater = sharedPreferences.getBoolean("dndOperatingTheater", false);
            boolean dndScannerRoom = sharedPreferences.getBoolean("dndScannerRoom", false);

            locationDisabledSwitch.setChecked(locationDisabled);
            dndPatientRoomSwitch.setChecked(dndPatientRoom);
            dndOfficeSwitch.setChecked(dndOffice);
            dndMeetingRoomSwitch.setChecked(dndMeetingRoom);
            dndOperatingTheaterSwitch.setChecked(dndOperatingTheater);
            dndScannerRoomSwitch.setChecked(dndScannerRoom);
        }
    }

    public void initializeSlideUpPanel() {
        // SlideUp Panel Layout, to hold user information and settings UI
        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.setPanelState(PanelState.HIDDEN);
        slidingUpPanelLayout.setPanelSlideListener(new PanelSlideListener() {

            @Override
            public void onPanelSlide(View view, float v) {
                imageView.setRotation(360);
                imageView.setAlpha(0.3f);
            }

            @Override
            public void onPanelCollapsed(View view) {
                imageView.setRotation(180);
                imageView.setAlpha(0.4f);
            }

            @Override
            public void onPanelExpanded(View view) {
            }

            @Override
            public void onPanelAnchored(View view) {
            }

            @Override
            public void onPanelHidden(View view) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Hide SlideUp Panel on back-press
        if (slidingUpPanelLayout != null && (slidingUpPanelLayout.getPanelState() == PanelState.EXPANDED || slidingUpPanelLayout.getPanelState() == PanelState.ANCHORED)) {
            slidingUpPanelLayout.setPanelState(PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(currentPrediction == null){
            currentPrediction = new CurrentPrediction(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(currentPrediction != null) {
            currentPrediction.close();
            currentPrediction = null;
        }
        // stopService(new Intent(this, IAService.class));
    }
}
