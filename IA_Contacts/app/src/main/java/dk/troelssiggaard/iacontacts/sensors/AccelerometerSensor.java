package dk.troelssiggaard.iacontacts.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.LinkedList;

import dk.troelssiggaard.iacontacts.classification.Classifier;
import dk.troelssiggaard.iacontacts.preprocessing.StandAloneFeat;

/**
 * Created by ts.
 */

public class AccelerometerSensor implements SensorEventListener {

    private static boolean preProcessing = false;
    private Sensor accelerometer;
    private SensorManager sensorManager;
    private LinkedList<String> dataSetList = null;
    private int c = 0;
    private Context context;

    public AccelerometerSensor(SensorManager manager, int delay, Context context) {
        Log.i("AccelerometerSensor","Constructor called");
        this.context = context;
        this.sensorManager = manager;
        dataSetList = new LinkedList<>();

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, delay);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {


            // If not Preprocessing, collect 100 data samples, then preprocess
            if(!getIsPreprocessing()) {

                float[] axis = event.values;
                float x = axis[0];
                float y = axis[1];
                float z = axis[2];
                long systemTime = System.currentTimeMillis();

                StringBuilder sensorString = new StringBuilder();
                sensorString.append(systemTime+",");
                sensorString.append(x+",");
                sensorString.append(y+",");
                sensorString.append(z+"\n");

                String data = sensorString.toString();

                processSensorData(data);
            }
        }
    }

    private void processSensorData(String data) {
        if(c < 100) {
            // Add sample to the List
            dataSetList.add(data);
            c++;

        }else if(c == 100){

//            Log.i("AccelerometerSensor","processSensorData() got 100 samples");

            // Do the preprocessing and return Feature Tuple as string
            setPreProcessing(true); // Stop the collection of sensor samples until the preprocessing is done
            StandAloneFeat standAloneFeat = new StandAloneFeat(dataSetList);
            String sensorTuple = standAloneFeat.getTuple();

            // Do the classification on the tuple (in AsyncTask)
            new Classifier(context).execute(sensorTuple);

            dataSetList.clear();
            c=0;
//            stopSensor();
        }
    }

    private static boolean getIsPreprocessing() {
        return preProcessing;
    }

    public static void setPreProcessing(boolean isPreProcessing) {
        preProcessing = isPreProcessing;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void stopSensor() {
        Log.i("AccelerometerSensor", "stop() called");

        sensorManager.unregisterListener(this, accelerometer);
//        sensorManager.flush(this);
//        accelerometer = null;
    }

}
