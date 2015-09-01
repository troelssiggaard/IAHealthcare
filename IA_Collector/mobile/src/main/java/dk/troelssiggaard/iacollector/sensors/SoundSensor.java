package dk.troelssiggaard.iacollector.sensors;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import dk.troelssiggaard.iacollector.DataLogger;
import dk.troelssiggaard.iacollector.FileUploader;
import dk.troelssiggaard.iacollector.MainActivity;

/**
 * Created by thesis on 08/12/14.
 */
public class SoundSensor extends Thread {

    private static int sampleRate = 8000; // 8kHz
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int AUDIO_CHANNELS = AudioFormat.CHANNEL_IN_MONO; // Mono channel
    private static final int ENCODING_TYPE = AudioFormat.ENCODING_PCM_16BIT; // 16bit PCM encoding
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(sampleRate, AUDIO_CHANNELS, ENCODING_TYPE);

    private AudioRecord audioRecord;
    private boolean isRecording = true;
    private byte data;

    private DataLogger dataLogger;

    private final static String sensorType = "SOUND.pcm";

    public SoundSensor(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        try {
            dataLogger = new DataLogger(sensorType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] buffer = new byte[BUFFER_SIZE];
        Log.i("Troels", "Bytebuffer: " + BUFFER_SIZE);

        audioRecord = new AudioRecord(AUDIO_SOURCE, sampleRate, AUDIO_CHANNELS, ENCODING_TYPE, BUFFER_SIZE);

        audioRecord.startRecording();


        while (isRecording) {

            try {
                // init buffer array

                //          read(buffer, offset=0x00, bufferSize);
                int readingAudio = audioRecord.read(buffer, 0, BUFFER_SIZE); // MAYBE EVERY 625 samples do FFT/RMS on the data and save it as a datapoint ( ~50 Hz )

                if (readingAudio == AudioRecord.ERROR_INVALID_OPERATION ||
                        readingAudio == AudioRecord.ERROR_BAD_VALUE) {
                    Log.i("Troels", "Bad value in Audio Recording");
                    return;
                }

                if (isRecording) {
                    dataLogger.saveBuffer(buffer); // save the data
                }




            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!isRecording) {

            audioRecord.stop();  // stop the recording
            Log.i("Troels", "AudioRecord Stop");
            audioRecord.release(); // release the sound input
            Log.i("Troels", "AudioRecord release");
            audioRecord = null; // set the object to null for garbage collection
            Log.i("Troels", "AudioRecord null");

            // this step should be moved to the while loop, doing calculations
            // from byte to int in another thread and then saving this to a .csv file
            //  dataLogger.saveAudioAsInt(dataLogger.getFilePath());
            String filePath = dataLogger.getFilePath();

            try {
                dataLogger.close(); // close the connection to the file saver instance
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(MainActivity.uploadData){
                FileUploader fileUploader = new FileUploader();
                fileUploader.execute(filePath);
            }

        }

    }

    public void stopRecording() {
        isRecording = false;
    }


    public static double byteArrayToDoubles(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

}


