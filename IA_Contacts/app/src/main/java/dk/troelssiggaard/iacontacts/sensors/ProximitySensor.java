package dk.troelssiggaard.iacontacts.sensors;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import dk.troelssiggaard.iacontacts.IAService;
import dk.troelssiggaard.iacontacts.database.CurrentPrediction;
import dk.troelssiggaard.iacontacts.estimation.InterruptibilityLogic;

/**
 * Created by ts.
 */
public class ProximitySensor implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private CurrentPrediction currentPrediction;
    private Context context;

    public ProximitySensor(SensorManager manager, int delay, Context context) {
        this.context = context;
        this.sensorManager = manager;
        proximitySensor = manager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, proximitySensor, delay);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        currentPrediction = new CurrentPrediction(context);

        Log.i("ProximitySensor", "Proxmity changed");

        // Sensor changed
        float distance = event.values[0];
        if (distance > 1.0) {
            currentPrediction.setPhoneInPocket(false);
        } else {
            currentPrediction.setPhoneInPocket(true);
        }

        // Update interruptibility and send new information as local broadcast
        new InterruptibilityLogic(context).update();


        currentPrediction.close();

        //stopSensor();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void stopSensor() {
        sensorManager.unregisterListener(this);
        sensorManager.flush(this);
        proximitySensor = null;
        currentPrediction.close();
    }
}
