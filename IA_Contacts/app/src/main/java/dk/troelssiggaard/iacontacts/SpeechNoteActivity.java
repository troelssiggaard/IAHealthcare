package dk.troelssiggaard.iacontacts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Locale;

import dk.troelssiggaard.iacontacts.database.CurrentPrediction;

public class SpeechNoteActivity extends Activity {

    private SpeechRecognizer speechRecognition;
    private Intent intent;
    private AudioManager audioManager;
    private int volumeLevel = 0;
    private EditText editText;
    private CurrentPrediction currentPrediction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_note);
        editText = (EditText) findViewById(R.id.editText);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        currentPrediction = new CurrentPrediction(getApplicationContext());

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        ToggleButton startStopDictation = (ToggleButton) findViewById(R.id.toggleButton);
        startStopDictation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    startSpeechRecognition();
                }else{
                    stopSpeechRecognition();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       return false;
    }

    private void startSpeechRecognition() {

        currentPrediction.setIsDictating(true);

        Log.i("SpeechNoteActivity", "startSpeechRecognition");

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "da");
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        speechRecognition = SpeechRecognizer.createSpeechRecognizer(getBaseContext());
        speechRecognition.setRecognitionListener(new IASpeechRecognitionListener());
        speechRecognition.startListening(intent);

        muteVolume();
    }

    private void stopSpeechRecognition() {
        currentPrediction.setIsDictating(false);

        if(speechRecognition != null) {
            speechRecognition.cancel();
            speechRecognition.destroy();
            speechRecognition = null;
        }
        unMuteVolume();
    }

    @Override
    public void onResume() {
        super.onResume();
        muteVolume();
    }

    @Override
    public void onPause() {
        super.onPause();
        unMuteVolume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSpeechRecognition();
    }

    private void muteVolume() {
        volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
    }

    private void unMuteVolume() {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeLevel, 0);
    }

    private void results(String speechTOText) {
        editText.append(speechTOText+" ");
    }

    private class IASpeechRecognitionListener implements RecognitionListener {

        public void onReadyForSpeech(Bundle params) {}
        public void onBeginningOfSpeech() {}
        public void onRmsChanged(float rmsdB) {
            // Maybe implement a processBar
        }
        public void onBufferReceived(byte[] buffer) {}
        public void onEndOfSpeech() {}

        public void onError(int error) {
            speechRecognition.startListening(intent);
        }

        public void onResults(Bundle results) {
            ArrayList<String> sttStrings = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            results(sttStrings.get(0));
            speechRecognition.startListening(intent);
        }
        public void onPartialResults(Bundle partialResults) {}
        public void onEvent(int eventType, Bundle params) {}
    }

}
