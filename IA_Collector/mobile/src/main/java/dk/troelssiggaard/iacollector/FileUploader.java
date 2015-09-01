package dk.troelssiggaard.iacollector;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;

import java.io.File;
import java.io.IOException;


public class FileUploader extends AsyncTask<String, Void, String> {

    private HttpClient httpClient;

    @Override
    protected String doInBackground(String... params) {
        return uploadFile(params[0]);
    }

    private String uploadFile(String path) {

        File file = new File(path);
        Log.i("Troels", "Uploading file from path:" + path);

        String fileName = "unknown.csv";
        fileName = file.getName();
        String serverAddress = "http://troelssiggaard.dk/thesis/index.php?filename="+fileName;

        HttpResponse response = null;
        Log.i("Troels", fileName);

        try {
            httpClient = AndroidHttpClient.newInstance("InterruptionLimiter"); // User-Agent: InterruptionLimiter

            HttpPost httpPost = new HttpPost(serverAddress);
            httpPost.addHeader("Connection", "Keep-Alive");
            httpPost.setEntity(new FileEntity(file, "binary/octet-stream"));

            response = httpClient.execute(httpPost);

            String status = response.getStatusLine().toString();

            return status;


        } catch (IOException e) {
            e.printStackTrace();
        }


            return "Error";

    }

    protected void onPostExecute(String result) {
        Log.i("Troels", "Upload was done!! Result: " + result);
        httpClient.getConnectionManager().shutdown(); // Close connection
        ((AndroidHttpClient) this.httpClient).close();

    }


}
