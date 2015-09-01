package dk.troelssiggaard.iacontacts.sensors;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dk.troelssiggaard.iacontacts.IAService;
import dk.troelssiggaard.iacontacts.database.CurrentPrediction;
import dk.troelssiggaard.iacontacts.estimation.InterruptibilityLogic;

/**
 * Created by ts.
 */

public class LocalLocationSensor {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothManager bluetoothManager;
    private Context context;
    protected CurrentPrediction currentPrediction;
    private Map<String,List<Integer>> beacons;
    private static long currentBeaconTimestamp = 0L;
    private final static int DISTANCE_CUT = 600; // Between 4-10 meters with a JaaLee Becaons
    private final static int DEPARTMENT_DISTANCE_CUT = 1000; // If no beacons are found with the low distance cut, do a department cut
    private final static int MINUTES_TO_MILLIS = 60000;

    private int beaconCounter = 0;
    private int unknownLocationCounter = 0;

    public LocalLocationSensor(int scanMode, Context context) {
        Log.i("LocalLocationSensor","Constructor");
        this.context = context;
        this.beacons = new HashMap<String,List<Integer>>();
        currentPrediction = new CurrentPrediction(context);

        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if(isBluetoothEnabled()) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

            ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(scanMode).build();
            List<ScanFilter> scanFilter = new ArrayList<>();
            bluetoothLeScanner.startScan(scanFilter, scanSettings, callback);
            Log.i("LocalLocationSensor","Started scanning for bluetooth.");
        }else{
            Toast.makeText(context,"Please turn on Bluetooth",Toast.LENGTH_LONG).show();
            bluetoothAdapter.enable();
        }
    }

    public static boolean isBluetoothEnabled() {
        Log.i("LocalLocationSensor","isBluetoothEnabled");

        BluetoothAdapter isBluetoothEnabledAdapter = BluetoothAdapter.getDefaultAdapter();

        if (isBluetoothEnabledAdapter == null || !isBluetoothEnabledAdapter.isEnabled()) {
            return false;
        }
        return true;
    }

    // Scan() Callback
    private ScanCallback callback = new ScanCallback() {

        public void onScanResult(int callbackType, ScanResult result) {
//            Log.i("LocalLocationSensor","ScanCallBack");

            int distance;
            int txLevel = 0;
            int rssiValue = result.getRssi(); // Signal Strength (RSSI)

            String beaconName = "";
            try {
                txLevel = result.getScanRecord().getTxPowerLevel(); // Transmit Level
                beaconName = result.getScanRecord().getDeviceName().trim(); // Device name, if given

            }catch (NullPointerException e) {
                e.getStackTrace();
            }
            distance = getApproximateDistance(rssiValue,txLevel); // Approximate distance of the beacon

            // Process the beacon
            processBeacons(distance, beaconName);
//            Log.i("LocalLocationSensor","Found beacon: "+beaconName);
        }
    };

    private void processBeacons(int distance, String beaconName) {
//        Log.i("LocalLocationSensor","Got beacon");

//        Log.i("LocalLocationSensor","distance: "+distance);


        // This doesn't take into account nearby BLE device which has a specified name, eg.
        // a smartwatch. If this is the case, the location will be called the name of the nearby device, eg. GALAXY Gear 2 (smartwatch).
        // A way of overcoming this would be to have a list of valid MAC addresses to search through or
        // make a SearchFilter (with serviceUUID or Manufacturer) like the one commented out in the scan() method

        unknownLocationCounter++;

        if(distance > 0 && distance < DISTANCE_CUT && !beaconName.equals("")) {

            // Get the timestamp of the first beacon reading in this batch
            if(beaconCounter == 0) { currentBeaconTimestamp = System.currentTimeMillis(); }

            // Put beacon and distances into a Map (One Beacon Map object contains a List of distances
            if (beacons.containsKey(beaconName)) {
                beacons.get(beaconName).add(distance);
            } else {
                beacons.put(beaconName, new ArrayList<Integer>());
            }
            beaconCounter++;

            // Do processing when we have reached 20 distance (RSSI etc.) collections
            if(beaconCounter == 4) {

                Log.i("LocalLocationSensor", "processBeacon() called");

                int smallestBeaconValue = Integer.MAX_VALUE;
                String smallestBeaconName = "";

                // Iterate over the map, to get all beacons and their distances
                for(Iterator beaconValues = beacons.entrySet().iterator(); beaconValues.hasNext();) {
                    Map.Entry mapEntry = (Map.Entry) beaconValues.next();
                    String key = (String) mapEntry.getKey();
                    List<Integer> values = (List<Integer>) mapEntry.getValue();

                    // Find the smallest distance of all (averaged) beacon distances
                    int avgValue = averageDistances(values);
                    if(smallestBeaconValue > avgValue) {
                        if(avgValue>0) {                    // Check for zero-sized array
                            smallestBeaconValue = avgValue;
                            smallestBeaconName = key;
                        }
                    }
                }

                if(!currentPrediction.getLatestLocationName().equals(smallestBeaconName)) {
                    Log.i("LocalLocationSensor", "Beacon was updated. Old: " + currentPrediction.getLatestLocationName() + ". New:" + smallestBeaconName);
                    currentPrediction.setLatestLocationName(smallestBeaconName);

                    // Update interruptibility and send new information as local broadcast
                    new InterruptibilityLogic(context).update();

//                    currentPrediction.close();

                }

                beacons.clear();
                beaconCounter = 0;
                unknownLocationCounter = 0;
               // stopSensor();

            }

            // If distance is 0, then there is an error in the reading
        } else if(distance > 0 && distance < DEPARTMENT_DISTANCE_CUT && !beaconName.equals("")){
            Log.i("LocalLocationSensor","Else If");

            currentPrediction.setLatestDepartmentName(beaconName); // Set the department
            unknownLocationCounter = 0;

            // Update interruptibility and send new information as local broadcast
            new InterruptibilityLogic(context).update();

        } else if(unknownLocationCounter == 1000){

            currentPrediction.setLatestLocationName("Unknown, maybe in"+currentPrediction.getLatestDepartmentName());
            unknownLocationCounter = 0;


            // Update interruptibility and send new information as local broadcast
            new InterruptibilityLogic(context).update();

        }
//        if(currentPrediction != null) { currentPrediction.close(); }
    }

    // Average list of distances
    protected static int averageDistances(List<Integer> list) {
        if(list.size() > 0) {
            int sumOverValue = 0;
            for (Integer i : list)
                sumOverValue = sumOverValue + i;

            return sumOverValue/list.size();
        }
        return 0;
    }

    // Approximate distance with txLevel, RSSI and a high signal propagation constant of 3
    private static int getApproximateDistance(int rssi, int txLevel) {
        return (int) Math.pow(10d, ((double) (txLevel - rssi) / (10 * 3)));
    }

    public void stopSensor() {
        if(isBluetoothEnabled()) {
            Log.i("LocalLocationSensor", "stopSensor() called");
            bluetoothLeScanner.stopScan(callback);
           // bluetoothLeScanner.flushPendingScanResults(callback);
        }
    }
}
