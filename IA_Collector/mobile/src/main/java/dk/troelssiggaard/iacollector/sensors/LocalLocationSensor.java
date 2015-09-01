package dk.troelssiggaard.iacollector.sensors;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dk.troelssiggaard.iacollector.DataLogger;
import dk.troelssiggaard.iacollector.FileUploader;
import dk.troelssiggaard.iacollector.MainActivity;

/**
 * Created by thesis on 07/12/14.
 */
public class LocalLocationSensor {

    private final BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private Context context;
    BluetoothLeScanner bluetoothLeScanner;
    private DataLogger dataLogger;
    private final static String sensorType = "LOCALLOCATION.csv";


    public LocalLocationSensor(Context context) {

        this.context = context;
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        try {
            dataLogger = new DataLogger(sensorType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isBluetoothEnabled() {
        BluetoothAdapter isBluetoothEnabledAdapter = BluetoothAdapter.getDefaultAdapter();

        if (isBluetoothEnabledAdapter == null || !isBluetoothEnabledAdapter.isEnabled()) {
            return false;
        }
        return true;
    }

    // Scan() Callback
    private ScanCallback callback = new ScanCallback() {
        public void onScanResult(int callbackType, ScanResult result) {
          //  Log.i("Troels", "Signal Strength: " + result.getRssi());

            String rssi = ""+result.getRssi();
            String device = ""+result.getDevice();
            // String record = ""+result.getScanRecord();
            // long timestamp = result.getTimestampNanos();
            long systemTime = System.currentTimeMillis();

          //  Log.i("rssi",rssi);
          //  Log.i("device",device);

            try {
                // Unix-timestamp, Device-MAC-address, RSSI-signal-strength-for-device
                dataLogger.saveString(systemTime+","+device+","+rssi);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    // Do a BLE Scan
    public void scan(int scanMode) {
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        // ScanFilter filter = new ScanFilter.Builder().setDeviceName("jaalee").build();
        List<ScanFilter> filters = new ArrayList<ScanFilter>();
        //filters.add(filter);

        ScanSettings settings = new ScanSettings.Builder().setScanMode(scanMode).build();

        bluetoothLeScanner.startScan(filters, settings, callback);

    }

    public void stopScan() {

        String filePath = dataLogger.getFilePath();

        bluetoothLeScanner.stopScan(callback);


        try {
            dataLogger.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(MainActivity.uploadData){
            FileUploader fileUploader = new FileUploader();
            fileUploader.execute(filePath);
        }

        Log.i("Troels", "Stopping the BLE scan");
    }




}
