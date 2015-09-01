package dk.troelssiggaard.iacontacts.classification;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import dk.troelssiggaard.iacontacts.IAService;
import dk.troelssiggaard.iacontacts.database.CurrentPrediction;
import dk.troelssiggaard.iacontacts.estimation.InterruptibilityLogic;
import dk.troelssiggaard.iacontacts.sensors.AccelerometerSensor;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by ts.
 */

public class Classifier extends AsyncTask<String,Void,String> {

//    private RandomForest randomForest;
    private J48 j48;
    private Context context;
    protected static long timestamp = 0L;
    protected CurrentPrediction currentPrediction;
//    public static boolean isDoneClassifying = false;

    public Classifier(Context context) {
        this.context = context;
    }

    protected Instance convertFeatureToInstance(String featureTuple) {

        String[] stringArray = featureTuple.split(",");
        double values[] = new double[stringArray.length];

        for(int i=0;i<stringArray.length;i++) {
            try {
                values[i] = Double.parseDouble(stringArray[i]);
            } catch (Exception e) {
                values[i] = Double.NaN;
            }
        }
        return new Instance(1.0, values);
    }

    @Override
    protected String doInBackground(String... params) {

//        Log.i("Classifier", "doInBackground() called");

        String prediction = "";
        try {
            AssetManager assetManager = context.getAssets();

            // Load the pre-trained J48 model (Faster, but a little less accurate)
            j48 = (J48) weka.core.SerializationHelper.read(assetManager.open("J48.model"));

//             Load the pre-trained Random Forest model
//            randomForest = (RandomForest) weka.core.SerializationHelper.read(assetManager.open("RandomForest.model"));

//             Load an empty data set
            Instances dataset = new	Instances(new BufferedReader( new InputStreamReader(assetManager.open("header.arff"))));

            // Add a feature string as an instance to the dataset
            dataset.add(convertFeatureToInstance(params[0]));
            dataset.setClassIndex(0); // This is the activities: R, D, T, W

            // Classify Instance (Do the prediction on dataset tuple)
            double classifiedDoubleValue = j48.classifyInstance(dataset.lastInstance());
//            double classifiedDoubleValue = randomForest.classifyInstance(dataset.lastInstance());
            String predictionLabel = dataset.classAttribute().value((int) classifiedDoubleValue);


            switch (predictionLabel){
                case "R":
                    prediction = "Reporting";
                    break;
                case "D":
                    prediction = "Diagnosing";
                    break;
                case "T":
                    prediction = "Treating";
                    break;
                case "W":
                    prediction = "Walking";
                    break;
                default:
                    prediction = "Unknown";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prediction;
    }

    @Override
    protected void onPostExecute(String prediction) {
        currentPrediction = new CurrentPrediction(context);
//        Log.i("Classifier", "onPostExecute() called - finished classifying. Predicted: " + prediction);
//        Log.i("Classifier", "onPostExecute() called - old prediction: " + currentPrediction.getLatestActivity());

        if(!prediction.equals(currentPrediction.getLatestActivity())) {
            // Update the database with the new prediction
            currentPrediction.setLatestActivity(prediction);
            Log.i("Classifier", "Updated DB with new prediction: " + prediction);

            // Update interruptibility
            new InterruptibilityLogic(context).update();

            // Send LocalBroadcast to let MainActivity and IAService know that the information was updated
            Intent intent = new Intent(IAService.LOCAL_BROADCAST);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        // Set PreProcessing = false, to start capturing sensor samples
        AccelerometerSensor.setPreProcessing(false);
        currentPrediction.close();
//        Log.i("Classifier","Got to the end of onPostExecute");
    }

}