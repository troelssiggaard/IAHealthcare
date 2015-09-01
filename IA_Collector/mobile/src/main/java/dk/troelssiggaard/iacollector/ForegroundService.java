package dk.troelssiggaard.iacollector;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import dk.troelssiggaard.iacollector.sensors.AccelerometerSensor;
import dk.troelssiggaard.iacollector.sensors.SoundSensor;
import dk.troelssiggaard.iacollector.R;
import dk.troelssiggaard.iacollector.sensors.GyroscopeSensor;
import dk.troelssiggaard.iacollector.sensors.LocalLocationSensor;


public class ForegroundService extends Service {

    // Declare sensor manager
    private SensorManager sensorManager;
    private int soundSensorSampleRate = 8000; // 8000 Hz
    private int accelerometerDelay = 20000;  // ~50Hz
    private int gyroscopeDelay = 20000; // ~50Hz
    private int scanMode = ScanSettings.SCAN_MODE_LOW_LATENCY; // ScanSettings.SCAN_MODE_LOW_LATENCY = ~100 ms    |    SCAN_MODE_LOW_POWER = ~4400 ms
    private SoundSensor soundSensor;
    private AccelerometerSensor accelerometerSensor;
    private GyroscopeSensor gyroscopeSensor;
    private LocalLocationSensor localLocationSensor;

    private final IBinder binder = new ForegroundServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class ForegroundServiceBinder extends Binder {
        ForegroundService getService() {
            return ForegroundService.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("Collecting data...")
                .setContentText("Interruption Data Collector")
                .setSmallIcon(R.drawable.ic_launcher)
                .build();

        Intent mainIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        startForeground(110386, notification);

        // Setup the sensor manager and the sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Sound Sensor
        soundSensor = new SoundSensor(soundSensorSampleRate);
        soundSensor.start(); // start thread

        //Accelerometer Sensor
        accelerometerSensor = new AccelerometerSensor(sensorManager, accelerometerDelay);

        //Gyroscope Sensor
        gyroscopeSensor = new GyroscopeSensor(sensorManager, gyroscopeDelay);

        //Bluetooth 4.0 LE Sensor / Location Sensor
        localLocationSensor = new LocalLocationSensor(this);

        if (localLocationSensor.isBluetoothEnabled()) {
            localLocationSensor.scan(scanMode); // ScanSettings.SCAN_MODE_LOW_LATENCY or ScanSettings.SCAN_MODE
        }else {
            Toast.makeText(this,"Please enable Bluetooth.",Toast.LENGTH_LONG).show();
            //final int REQUEST_ENABLE_BT = 110386; // Unique result id to identify request by onActivityResult()

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }


        return 0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);



        Log.i("Troels", "Foreground Service destroyed!");
        soundSensor.stopRecording(); // Stopping the sound sensor
        accelerometerSensor.stop();  // Stopping the accelerometer
        gyroscopeSensor.stop();      // Stopping the gyroscope
        localLocationSensor.stopScan(); // Stopping bluetooth scanning
    }
}
