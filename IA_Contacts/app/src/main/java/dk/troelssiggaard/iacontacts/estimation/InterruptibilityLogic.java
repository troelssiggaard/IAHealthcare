package dk.troelssiggaard.iacontacts.estimation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import dk.troelssiggaard.iacontacts.IAService;
import dk.troelssiggaard.iacontacts.database.CurrentPrediction;

/**
 * Created by ts.
 */

public class InterruptibilityLogic {

    private SharedPreferences sharedPreferences;
    protected CurrentPrediction currentPrediction;
    private Context context;

    private static final String UNKNOWN_ACTIVITY = "Unknown/Idle";

    // Accelerometer (Activity Recognition)
    private String activityPredictedName;

    // Location (Bluetooth Proximity)
    private String locationRoomType;

    // User Defined Settings
    private boolean userLocationDisabled;
    private boolean userDndPatientRoom;
    private boolean userDndOffice;
    private boolean userDndMeetingRoom;
    private boolean userDndOperatingTheater;
    private boolean userDndScannerRoom;

    private static final int UNINTERRUPTIBLE = -1;
    private static final int UNKNOWN = 0;
    private static final int INTERRUPTIBLE = 1;

    private static int oldInterruptibility = 0;
    private static String oldActivity = "";
    private static String oldLocation = "";

    public InterruptibilityLogic(Context context) {
        this.context = context;
    }

    public void update(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context); // SharedPreferences: User Settings
        getUserSettings(); // Updates variables with sharedPreferences saved in the app

        currentPrediction = new CurrentPrediction(context);  // Init DB and open

        // Do the interruptibility calculation and set activity and location readings
        int interruptibility = interruptibility();

        // If User turned off location sharing then
        if (userLocationDisabled) {
            currentPrediction.setLatestLocationName("Location Disabled");
        }

        // Update the interruptibility value if it has changed
        if(interruptibility != currentPrediction.getLatestInterruptibilityPrediction()) {
            currentPrediction.setLatestInterruptibilityPrediction(interruptibility);
        }

        int newInterruptibility = currentPrediction.getLatestInterruptibilityPrediction();
        String newActivity = currentPrediction.getLatestActivity();
        String newLocation = currentPrediction.getLatestLocationName();

        if(newInterruptibility != oldInterruptibility || !newActivity.equals(oldActivity) || !newLocation.equals(oldLocation)) {
            oldInterruptibility = newInterruptibility;
            oldActivity = newActivity;
            oldLocation = newLocation;

            // Send LocalBroadcast to let MainActivity and IAService know that the information was updated
            Intent intent = new Intent(IAService.LOCAL_BROADCAST);
            intent.putExtra(IAService.UPDATED_SENSOR, true);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            Log.i("InterruptibilityLogic","Sent updated information");
        }else{
            Log.i("InterruptibilityLogic", "Skipped information, no news here.");
        }

        currentPrediction.close(); // Close DB connection
    }


    private void getUserSettings() {
        userLocationDisabled = sharedPreferences.getBoolean("locationDisabled", false);
        userDndPatientRoom = sharedPreferences.getBoolean("dndPatientRoom", false);
        userDndOffice = sharedPreferences.getBoolean("dndOffice", false);
        userDndMeetingRoom = sharedPreferences.getBoolean("dndMeetingRoom", false);
        userDndOperatingTheater = sharedPreferences.getBoolean("dndOperatingTheater", false);
        userDndScannerRoom = sharedPreferences.getBoolean("dndScannerRoom", false);
    }


    private int interruptibility() {

        currentPrediction.setTimestamp(System.currentTimeMillis());

          if(!currentPrediction.getIsCharging()) { // -> Phone is running on battery (not charging)

        if(!currentPrediction.getIsDictating()) {

            if (!currentPrediction.getInACall()) { // -> Phone is not used in a call (interruptible)

                // Get the value for activity and location combined
                int activityLocation = validateActivityLocation();

                // Get the phone state (silent mode and in use)
                int phoneState = validatePhoneState();


                // Do Not Distrub setting will overwrite the Interruptibility value returned, but keep the activity and location
                if (isDoNotDisturbSettingUsed()) {

                    locationRoomType = currentPrediction.getLatestLocationName();

                    if (locationRoomType.equals("Patient") && userDndPatientRoom) {
                        return UNINTERRUPTIBLE;
                    } else if (locationRoomType.equals("Office") && userDndOffice) {
                        return UNINTERRUPTIBLE;
                    } else if (locationRoomType.equals("Meeting") && userDndMeetingRoom) {
                        return UNINTERRUPTIBLE;
                    } else if (locationRoomType.equals("Scanner") && userDndScannerRoom) {
                        return UNINTERRUPTIBLE;
                    } else if (locationRoomType.equals("Operating") && userDndOperatingTheater) {
                        return UNINTERRUPTIBLE;
                    }
                }

                // Sum the interruptibility
                int summedInterruptibility = activityLocation + phoneState;

                if (summedInterruptibility <= UNINTERRUPTIBLE) { // Values: -1, -2
                    return UNINTERRUPTIBLE;
                } else if (summedInterruptibility >= INTERRUPTIBLE) { // Values: 1, 2
                    return INTERRUPTIBLE;
                }
                return summedInterruptibility;

            } else { // -> Phone is used in a call (occupied)
                currentPrediction.setLatestActivity("On the phone");

                return UNINTERRUPTIBLE;
            }

        }else{
            currentPrediction.setLatestActivity("Dictation");
            return UNINTERRUPTIBLE;
        }

        }else{
            // -> Phone is in a charger, this invalidates the results
            // from the activity recognition, and the location of the doctor cannot be known
            // Time: Gives an idea of how long the phone has been charging

            currentPrediction.setLatestActivity("Phone is charging");
            return UNKNOWN;
        }
    }

    private boolean isDoNotDisturbSettingUsed() {
        return (userDndMeetingRoom || userDndOffice || userDndOperatingTheater || userDndPatientRoom || userDndScannerRoom);
    }

    private int validateActivityLocation() {

        switch (currentPrediction.getRoomIdentifier()) {

            case "Patient":
                currentPrediction.setLatestActivity(activityPredictedName); // SET: Eg. Walking, Diagnosing, Reporting
                if (currentPrediction.getLatestLocationName().equals("Walking")) {
                    return INTERRUPTIBLE;
                } else {
                    return UNINTERRUPTIBLE;
                }

            case "Office":
                if (currentPrediction.getLatestActivity().equals("Reporting")) {
                    return UNINTERRUPTIBLE;
                } else {
                    currentPrediction.setLatestActivity(UNKNOWN_ACTIVITY); // Set activity Unknown, because it doesn't make sense to talk about diagnosing/treating patients
                    return INTERRUPTIBLE;
                }

            case "Staff":
                if (!currentPrediction.getLatestActivity().equals("Walking")) {
                    currentPrediction.setLatestActivity(UNKNOWN_ACTIVITY);
                }
                return INTERRUPTIBLE;

            case "Scanner":
                if (!currentPrediction.getLatestActivity().equals("Walking")) {
                    currentPrediction.setLatestActivity(UNKNOWN_ACTIVITY);
                }
                return UNINTERRUPTIBLE;

            case "Operating":
                currentPrediction.setLatestActivity(UNKNOWN_ACTIVITY);
                return UNINTERRUPTIBLE;

            case "Meeting":
                currentPrediction.setLatestActivity(UNKNOWN_ACTIVITY);
                if (activityPredictedName.equals("Reporting")) {
                    return UNINTERRUPTIBLE; // Sitting at a desk, probably in a meeting
                }
                return UNKNOWN;

            case "Unknown":
                // Placeholder...
                // Do nothing, go to default (no break or return statement)

            default:
                currentPrediction.setLatestLocationName("Unknown"); // If location is unknown, then get the department

                if (currentPrediction.getLatestActivity().equals("Walking")) {
                    return INTERRUPTIBLE;
                } else {
                    currentPrediction.setLatestActivity(UNKNOWN_ACTIVITY);
                    return UNKNOWN;
                }
        }
    }

    private int validatePhoneState() {

        int evaluationCount = 0;

        if (currentPrediction.getOnSilent()) {
            evaluationCount += UNINTERRUPTIBLE;
        } else {
            evaluationCount += INTERRUPTIBLE;
        }

        if (currentPrediction.getPhoneInUse()) {
            evaluationCount += INTERRUPTIBLE;
        } else {
            evaluationCount += UNINTERRUPTIBLE;
        }

        // Proximity sensor, eg. in pocket / phone screen towards table / phone against ear
        if (currentPrediction.getPhoneInPocket()) {
            evaluationCount += UNINTERRUPTIBLE;
        }

        if (evaluationCount <= UNINTERRUPTIBLE) {
            return UNINTERRUPTIBLE;
        } else if (evaluationCount >= INTERRUPTIBLE) {
            return INTERRUPTIBLE;
        }

        return UNKNOWN;
    }
}