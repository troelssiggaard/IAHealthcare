package dk.troelssiggaard.iacontacts.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v4.content.LocalBroadcastManager;

import dk.troelssiggaard.iacontacts.IAService;
import dk.troelssiggaard.iacontacts.database.CurrentPrediction;
import dk.troelssiggaard.iacontacts.estimation.InterruptibilityLogic;

/**
 * Created by ts.
 */
public class PhoneStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Open DB
        CurrentPrediction currentPrediction = new CurrentPrediction(context);

        switch (intent.getAction()){
            case Intent.ACTION_NEW_OUTGOING_CALL:
                currentPrediction.setInACall(true);
                break;

            case Intent.ACTION_SCREEN_OFF:
                currentPrediction.setPhoneInUse(false);
                break;

            case Intent.ACTION_USER_PRESENT:
                currentPrediction.setPhoneInUse(true);
                break;

            case Intent.ACTION_POWER_CONNECTED:
                currentPrediction.setIsCharging(true);
                break;

            case Intent.ACTION_POWER_DISCONNECTED:
                currentPrediction.setIsCharging(false);
                break;
        }

        switch(intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1)){
            case AudioManager.RINGER_MODE_NORMAL:
                currentPrediction.setOnSilent(false);
                break;

            case AudioManager.RINGER_MODE_VIBRATE:
                currentPrediction.setOnSilent(true);
                break;

            case AudioManager.RINGER_MODE_SILENT:
                currentPrediction.setOnSilent(true);
        }

        // Update interruptibility and send new information as local broadcast
        new InterruptibilityLogic(context).update();

        // Close DB
        currentPrediction.close();
    }


}
