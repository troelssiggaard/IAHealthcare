package dk.troelssiggaard.iacontacts.preprocessing;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * WISDM project research program
 * http://storm.cis.fordham.edu/~gweiss/wisdm
 *
 * This class takes raw data from the client app and outputs an .arff file suitable
 * for weka interpretation.
 *
 * @author Jeff Lockhart <a href="mailto:lockhart@cis.fordham.edu">lockhart@cis.fordham.edu</a>
 * @author Jess Timko
 * @version 4.0
 * @date 7 July 2014
 *
 * --------------------------
 * @date 2015
 * NOTE:
 * This file was edited for use in IA Contacts application (Master Thesis) by Troels Siggaard
 * The original work was done by Jeff Lockhart & Jess Timko of the EISDM project research program.
 *
 */

public class StandAloneFeat {

    /**
     * a threadsafe queue for SplitResults objects added to by TupleSorter and taken from by FeatGen
     */
    private LinkedList<TupFeat> que = new LinkedList<TupFeat>();

    // windowSize = number of seconds for window frame
    private static int windowSize = 2;

    //samplingRate = Hz (number of samples collected per second)
    //currently use 50 Hz sampling rate
    private static int samplingRate = 50;

    // windowSize*20 entries is this much change in timestamps
    private static long duration = windowSize*1000;

    private TupFeat tmp = null;

    public StandAloneFeat(LinkedList<String> list) {

        readList(list);

        for(int i = 0; i < que.size(); i++){
            tmp = que.get(i);
            FeatureLib.processTup(tmp);
        }
    }

    /**
     * get the Feature Tuple
     */
    public String getTuple() {

        // Temporary tuple variables
        String tuple = "";
        TupFeat tup = null;
        float[] f = null;
        int c = 0;

        // Go through the entire result set
        while (!que.isEmpty())  {
            tup = que.pop();
            f = tup.getFeat();
            tuple = "";
            tuple += "?,";
            for (int i = 0; i < 13; i++){ // the data itself
                tuple += f[i];
                if(i<12) tuple += ",";
            }
            que = null;
            return tuple + "\n";
        }
        return "Error, no result.";
    }

    /**
     * Read the list of sensor data
     * and generate a Feature Tuple for later retrieval and classification
     */
    private void readList(LinkedList<String> read){

        float[] x = new float[(windowSize*samplingRate)]; // holds the accelerometer data for a single tuple
        float[] y = new float[(windowSize*samplingRate)];
        float[] z = new float[(windowSize*samplingRate)];
        String tmpLn = null, tmpLna = null, lastLn = "fakeLine";
        long cTime = 0, tmpt = 1, lastTime = 0; // time of start of current tuple, and temp time
        long[] t = new long[(windowSize*samplingRate)];

        int i = 0; // counter for tuple members
        int savTCount = 0; //saved tuple count
        int repCount = 0;

        Iterator<String> iterator = read.iterator();

        while(iterator.hasNext()){

            tmpLna = iterator.next(); // Read the lines in the ArrayList

            if(tmpLna.equals(lastLn)){
                repCount++; // Used for debugging

            } else{
                lastLn = tmpLna; // we know the line is good
                tmpLn = tmpLna.replace(';', ',');
                String[] values = tmpLn.split(",");

                cTime = Long.parseLong(values[0]);
                tmpt = Long.parseLong(values[0]);

                if(tmpt <= (cTime + duration)){
                    x[i] = Float.valueOf(values[1].trim()).floatValue();
                    y[i] = Float.valueOf(values[2].trim()).floatValue();
                    z[i] = Float.valueOf(values[3].trim()).floatValue();
                    t[i] = tmpt;
                    // lastTime = tmpt;
                    i++;
                }

                if(i == (windowSize*samplingRate)){ // if we reach (windowSize*samplingRate) samples, then the windowSize tuple is done and should be saved

                    savTCount++;

                    TupFeat ttup = new TupFeat(cTime);
                    ttup.setCount(i);

                    // all arrays must be copied into new ones because java is pass by reference always
                    float[] xt = new float[(windowSize*samplingRate)], yt = new float[(windowSize*samplingRate)], zt = new float[(windowSize*samplingRate)];
                    long[] tt = new long[(windowSize*samplingRate)];
                    for(int j = 0; j<(windowSize*samplingRate); j++){
                        xt[j] = x[j];
                        yt[j] = y[j];
                        zt[j] = z[j];
                        tt[j] = t[j];
                    }

                    ttup.setRaw(xt, yt, zt, tt);
                    que.add(ttup);

                    iterator.remove();

                    i = 0; // reset count
                } // end if
            } // end else

            if(savTCount==1) {
                // Used for debugging
            }

        } // end while
    } // end function readlist
} // end class