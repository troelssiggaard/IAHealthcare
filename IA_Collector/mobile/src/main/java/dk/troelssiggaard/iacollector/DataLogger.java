package dk.troelssiggaard.iacollector;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DataLogger extends Thread {

    private BufferedOutputStream outputStream;
    private File file;
    String dateFormatted;

    public DataLogger(String filename) {
        Date date = new Date();
        dateFormatted = new SimpleDateFormat("dd-MM-yyyy").format(date);

        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DATA_" + dateFormatted + "_" + filename);
        int counter = 1;

        if (file.exists()) {
            while (file.exists()) {
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DATA("+counter+")_" + dateFormatted + "_" + filename);
                counter++;
            }
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public String getFilePath() {
        return file.getAbsolutePath();
    }

    public String getFilename() {
        return file.getAbsolutePath();
    }

    public void saveString(String s) throws IOException {

        outputStream.write(s.getBytes());
        outputStream.write(System.lineSeparator().getBytes()); // LineSeparator not supported in < API 4.4
        outputStream.flush();

    }

    public void saveBuffer(byte[] buffer) throws IOException {

        outputStream.write(buffer, 0, buffer.length);
        outputStream.flush();

    }

    public void close() throws IOException {
        outputStream.flush();
        outputStream.close();

    }

}
