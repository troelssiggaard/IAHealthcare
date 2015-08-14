# InterruptAware System
The InterruptAware System (PROTOTYPE) was developed as part of my Master Thesis on the IT University of Copenhagen

The system consists of three applications:

IA Collector : An Android App for collecting sensor data: Accelerometer, Gyroscope, Microphone (audio) & nearby Bluetooth Smart (BLE) beacons. The app was developed and tested for use on LG Nexus 5 (for collecting) and Asus Nexus 7 FHD (for labelling ground truth). It's recommended to sync clocks on the two devices if used for data collection & labelling. All data are captured and stored locally on the phone in sepearate CSV-files. You have the possibility to let the app upload the CSV files via HTTP POST to a (hardcoded) server address (please change code to reflect your server). The app needs Android 5.0 Lollipop or above and hasn't been tested on other than mentioned devices. Code is free to use within the limits of the Apache 2.0 License. 

IA Contacts : An Android App for context and interruptibility sensing, created for healthcare use. The app uses: Accelerometer sensor to capture sensor data. A build in preprocessor is implemented (modified from WISDM at Fordham University) and Weka (Data Mining Toolkit from University of Waikato) is used to for Activity Recognition of four activities by leveraging my J48 or RandomForest classifiers (modify code to change classifyer model). Furthermore, Bluetooth Smart (BLE) is used to estimate nearest beacon (room) and the Proximity sensor is used to estimate phone in pocket/face-down. A BroadcastReceiver is registering system wide phone events, like phone-in-use, phone-in-a-call and phone-is-charging. All the above collected knowledge is sent to the IA Monitor web service, for displaying activity, location and interruptibility. Interruptibility is estimated from the knowledge gathered by the phone and estimates: Uniterruptible (red), Unknown (yellow) and Interruptible (green).

!! Note, the following code libaries are not licensed under Apache 2.0: !!
 * Weka: GNU General Public License
 * StandAloneFeat.java, FeatureLib.java & TupFeat.java : No license provided but read more in this paper: "Design Considerations for the WISDM Smart Phone-Based Sensor Mining Architecture" by Jeffrey W. Lockhart, Gary M. Weiss, Jack C. Xue, Shaun T. Gallagher, Andrew B. Grosner, and Tony T. Pulickal (2011)
 * Materialize CSS: MIT License
 
IA Monitor: A PHP/MySQL webservice using REST (JSON) and the jQuery API. This service was build to receive and distribute/display the activity, location, interruptibilty and time since last update - formation received from the IA Contacts app. The application is build with the Materialize CSS Framework (MIT Licensed).

More information about the thesis, along with downloadable binaries and IA Monitor demo, can be found here:
http://www.troelssiggaard.com/masterthesis/

Best regards,
Troels



