package dk.troelssiggaard.iacollector.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.io.IOException;

import dk.troelssiggaard.iacollector.DataLogger;
import dk.troelssiggaard.iacollector.FileUploader;
import dk.troelssiggaard.iacollector.MainActivity;

/**
 * Created by thesis on 03/12/14.
 */
public class GyroscopeSensor implements SensorEventListener {

    private Sensor gyroscope;
    private SensorManager sensorManager;
    private final static String sensorType = "GYROSCOPE.csv";
    private DataLogger dataLogger;

    public GyroscopeSensor(SensorManager manager, int delay) {
        this.sensorManager = manager;

        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyroscope, delay);

        try {
            dataLogger = new DataLogger(sensorType);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            float[] axis = event.values;
            float x = axis[0];
            float y = axis[1];
            float z = axis[2];
            // long timestamp = event.timestamp;
            long systemTime = System.currentTimeMillis();

            try {
                // Unix-timestamp , X , Y , Z
                dataLogger.saveString(systemTime+","+x+","+y+","+z);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        sensorManager.unregisterListener(this);
        gyroscope = null;

        String filePath = dataLogger.getFilePath();

        try{
            dataLogger.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        if(MainActivity.uploadData){
            FileUploader fileUploader = new FileUploader();
            fileUploader.execute(filePath);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

           Log.d("Troels", "Gyroscope sensor accruracy changed: " + accuracy);

    }
}
