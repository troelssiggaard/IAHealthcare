package dk.troelssiggaard.iacontacts;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import dk.troelssiggaard.iacontacts.database.CurrentPrediction;
import dk.troelssiggaard.iacontacts.database.DBHelper;
import dk.troelssiggaard.iacontacts.sensors.AccelerometerSensor;
import dk.troelssiggaard.iacontacts.sensors.LocalLocationSensor;
import dk.troelssiggaard.iacontacts.sensors.PhoneStateReceiver;
import dk.troelssiggaard.iacontacts.sensors.ProximitySensor;

/**
 * Created by ts.
 */
public class IAService extends Service {

    private BroadcastReceiver phoneStateReceiver;
    private CurrentPrediction currentPrediction;

    public static final String LOCAL_BROADCAST = IAService.class.getName() + "LocalBroadcast";
    private static final String SERVER = "http://troelssiggaard.com/iamonitor/rest/";

    public static final String UPDATED_SENSOR = "UpdatedSensor"; // Used as identifier for local broadcasts in MainActivity/IAService and Sensors
    String oldActivity = "";
    String oldLocation = "";
    int oldInterruptibility = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("IAService", "Created Service!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("IAService", "IAService.onStartCommand() called");
        Toast.makeText(this,"Started Service!", Toast.LENGTH_LONG).show();


        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("IA Contacts is running")
//                .setContentText("Interruption Data Collector")
                .setSmallIcon(R.drawable.ic_launcher)
                .build();

        startForeground(14865, notification);


        // Init DB
        currentPrediction = new CurrentPrediction(this);

        // Start the phoneStateReceiver that listens for system wide broadcasts
        phoneStateReceiver();

        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()){
            Toast.makeText(this,"Please turn on WiFi",Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }

        // This will start capturing sensor data
        startSensors();

        // Register LocalBroadcastManager for retrieval of information and post to the network (IA Monitor server)
        loadNetworkBroadcastManager();

        return Service.START_STICKY;
    }

    private void startSensors(){
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        AccelerometerSensor accelerometerSensor = new AccelerometerSensor(sensorManager, SensorManager.SENSOR_DELAY_GAME, this); // Accelerometer Sensor |  SENSOR_DELAY_GAME = ~50 Hz (on Nexus 5)
        ProximitySensor proximitySensor = new ProximitySensor(sensorManager, SensorManager.SENSOR_DELAY_NORMAL, this); // Proximity Sensor
        LocalLocationSensor localLocationSensor = new LocalLocationSensor(ScanSettings.SCAN_MODE_LOW_POWER, this);
    }

    private void loadNetworkBroadcastManager() {
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {


            @Override
            public void onReceive(Context context, Intent intent) {

                // If localBroadcast has UPDATED_SENSOR = true, then
                // send the new information from the DB to the server
                if (intent.getBooleanExtra(IAService.UPDATED_SENSOR, false)) {

                    long time = currentPrediction.getTimestamp();
                    String activity = currentPrediction.getLatestActivity();
                    String location = currentPrediction.getLatestLocationName();
                    int interruptibility = currentPrediction.getLatestInterruptibilityPrediction();

                    // Post information to REST Service
                    restServicePost(DBHelper.HARDCODED_USER_ID, time, activity, location, interruptibility);
                }

            }
        }, new IntentFilter(IAService.LOCAL_BROADCAST));
    }

    private void restServicePost(int userID, long time, String activity, String location, int interruptibility) {

        if(!activity.equals(oldActivity) || !location.equals(oldLocation) || interruptibility != oldInterruptibility) {

            boolean saveToHistory = false;
            AsyncHttpClient client = new AsyncHttpClient();


            // Post information to Rest Server (IA Monitor)
            RequestParams requestParams = new RequestParams();
            if(interruptibility != oldInterruptibility) {
                oldInterruptibility = interruptibility;
                requestParams.put("interruptibility", interruptibility);
            }
            if (time != 0) {
                requestParams.put("timestamp", time);
            }
            if (activity != null && !activity.equals(oldActivity)) {
                oldActivity = activity;
                requestParams.put("activity", activity);
                saveToHistory = true;

            }
            if (location != null && !location.equals(oldLocation)) {
                oldLocation = location;
                requestParams.put("location", location);
                saveToHistory = true;
            }

            // Save latest Activity
            if(saveToHistory){
                saveToHistory(oldActivity,oldLocation);
            }

            requestParams.setUseJsonStreamer(true);

            // Post data to the specific user (userID) to the REST Server (AI Monitor)
            client.post(SERVER + "/doctor/app/" + userID, requestParams, new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int i, Header[] headers, byte[] bytes) {

                    Log.i("Troels", "success on post:" + new String(bytes));
                }

                @Override
                public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
//                    Log.i("Troels", "fail on post"+headers+" "+new String(bytes));
                }
            });

        }else{
//            Log.i("HTTP POST", "Post was cancelled! No new entries.");
        }

    }

    // Save the location and activity in a list for the History Actitity.
    private void saveToHistory(String activity, String location) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
        Date date = new Date();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Set<String> set = sharedPreferences.getStringSet("history", new LinkedHashSet<String>(20));
        set.add(simpleDateFormat.format(date) + " " + activity+" in "+location);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        spEditor.putStringSet("history",set);
        spEditor.apply();
    }

    private void phoneStateReceiver() {

        // Setup PhoneStateListener for CALL STATE
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        PhoneStateListener listener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {

                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        currentPrediction.setInACall(false);
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        currentPrediction.setInACall(true);
                        break;
                }
            }
        };
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        // Start Broadcast Receiver for Screen, Charger, Silent mode etc.
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        phoneStateReceiver = new PhoneStateReceiver();
        registerReceiver(phoneStateReceiver, filter);
    }

    @Override
    public void onDestroy() {
        Log.i("IAService", "OnDestroy called");
        super.onDestroy();
        currentPrediction.close(); // Close DB
        unregisterReceiver(phoneStateReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("IAService", "IAService.onBind() called");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("IAService", "IAService.onUnbind() called");
        return super.onUnbind(intent);
    }
}

