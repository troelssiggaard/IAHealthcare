package dk.troelssiggaard.iacollector;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import dk.troelssiggaard.iacollector.R;
import dk.troelssiggaard.iacollector.sensors.LocalLocationSensor;

/* NOTES TO SELF:
 *
 * TYPES OF SENSORS TO INCORPORATE:
 * --------------------------------
 * TYPE_ACCELEROMETER
 * TYPE_GYROSCOPE
 * TYPE_LIGHT
 * TYPE_PRESSURE
 *
 * CHECK FOR OS version, and restrict app to 5.0 Lollipop
 *
 * ORIENTATION IS FIXED TO PORTRAIT TO ELIMINATE ERRORS IN DATA WHEN TURNING THE DEVICE
 *
 * ACCELEROMETER DELAY "GAME": 20000 microseconds (20 ms)
 *
 */

public class MainActivity extends Activity {

    // UI elements
    private TextView textView;
    private ToggleButton toggleButton;
    private CheckBox checkBox;
    public static boolean uploadData = true;

    private ForegroundService foregroundService;
    private boolean bound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        checkBox = (CheckBox) findViewById(R.id.checkBox);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    startCollecting();
                } else {
                    stopCollecting();
                }
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    uploadData = true;
                }else {
                    uploadData = false;
                }
            }
        });

        if (isOSVersionSupported() && LocalLocationSensor.isBluetoothEnabled()) {

            textView.setText("Click start to begin data collection.");
            toggleButton.setEnabled(true);

        } else {

            textView.setText("Your OS version isn't supported.\n Android v5.0 Lollipop or higher required!");
            if(!LocalLocationSensor.isBluetoothEnabled()) { textView.setText("Please enable Bluetooth!");}
            //toggleButton.setEnabled(false);
        }
    }

    // Check if the OS version is 4.4.x (Kit Kat) or above
    protected boolean isOSVersionSupported() {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return true;
        }

        return false;
    }

    ////////// MENU /////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.note) {
            Intent intent = new Intent(this, NoteActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    ///////// LIFECYCLE ///////////

    @Override
    protected void onPause() {
        super.onPause();
    //   Toast.makeText(this, "onPause called: " + System.currentTimeMillis(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
    //    Toast.makeText(this, "onStop called: " + System.currentTimeMillis(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        stopCollecting();
    }

    @Override
    protected void onResume() {
        super.onResume();
    //    Toast.makeText(this, "onResume called: " + System.currentTimeMillis(), Toast.LENGTH_SHORT).show();
    }

    /////////// START DATA COLLECTION //////////////

    public void startCollecting() {
        if(LocalLocationSensor.isBluetoothEnabled()) {
            Toast.makeText(this, "Starting data collector...", Toast.LENGTH_SHORT).show();

            Intent serviceIntent = new Intent(getApplicationContext(), ForegroundService.class);
            startService(serviceIntent);

            bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);

        }else {
            Toast.makeText(this,"Please enable Bluetooth!", Toast.LENGTH_LONG).show();
            toggleButton.setChecked(false);
        }

    }

    //////////// STOP DATA COLLECTION /////////////

    protected void stopCollecting() {
        Toast.makeText(this, "Stopping...", Toast.LENGTH_SHORT).show();

        stopService(new Intent(this, ForegroundService.class));
        unbindService(serviceConnection);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ForegroundService.ForegroundServiceBinder binder = (ForegroundService.ForegroundServiceBinder) service;
            foregroundService = binder.getService();
            bound = true;

        }
    };


}
