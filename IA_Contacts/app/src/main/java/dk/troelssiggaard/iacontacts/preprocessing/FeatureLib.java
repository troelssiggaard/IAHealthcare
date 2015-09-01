package dk.troelssiggaard.iacontacts.preprocessing;

/**
 * WISDM Lab http://www.cis.fordham.edu/wisdm/
 *
 * library of functions to transform raw data into features for activity recognition. 
 *
 * @author Jeff Lockhart <a href="mailto:lockhart@cis.fordham.edu">lockhart@cis.fordham.edu</a>
 * @version 1.2.0
 * @date 25 October 2013
 *
 * --------------------------
 * @date 2015
 * NOTE:
 * This file was edited for use in IA Contacts application (Master Thesis) by Troels Siggaard
 * The original work was done by Jeff Lockhart & Jess Timko of the EISDM project research program.
 *
 */

public class FeatureLib {

    /**
     * the maximum number of peak values is half of the total values plus 1
     * because every other value could be a peak
     */
    private static int maxPeaks = (TupFeat.recordCount / 2) + 1;

    /**
     * governs feature generation
     * @param t
    // * @param bins
     * @return
     */
    public static TupFeat processTup(TupFeat t){

        int count = t.getCount();
        double[] x = TupFeat.toDoubles(t.getX());
        double[] y = TupFeat.toDoubles(t.getY());
        double[] z = TupFeat.toDoubles(t.getZ());

        return processTup(count, t.getT(), x, y, z, t);
    }

    /**
     * governs feature generation
     * @param count
     * @param t
     * @param x
     * @param y
     * @param z
     * @param tup
     * @return
     */
    public static TupFeat processTup(int count, long[] t, double[] x, double[] y, double[] z, TupFeat tup){

        float[] floatFeat = new float[13];

        floatFeat[0] = getAvr(count, x);
        floatFeat[1] = getAvr(count, y);
        floatFeat[2] = getAvr(count, z);
        floatFeat[3] = getPeakTime(t, x);
        floatFeat[4] = getPeakTime(t, y);
        floatFeat[5] = getPeakTime(t, z);
        floatFeat[6] = getAbsDev(count, x, floatFeat[0]);
        floatFeat[7] = getAbsDev(count, y, floatFeat[1]);
        floatFeat[8] = getAbsDev(count, z, floatFeat[2]);
        floatFeat[9] = getSDiv(count, x, floatFeat[0]);
        floatFeat[10] = getSDiv(count, y, floatFeat[1]);
        floatFeat[11] = getSDiv(count, z, floatFeat[2]);
        floatFeat[12] = getAvrMagnitude(count, x, y, z);

        // store results to tupfeat object
        tup.setFeat(floatFeat);
        tup.killraw(); // save some memory

        return tup;
    }

    /**
     * generates time between peaks for one axis over a 10 second tuple
     *
     * @param t
     *            the array of time stamps matching n
     * @param n
     *            the array of accelerometer values for one axis
     * @return average time between peaks
     */
    public static float getPeakTime(long[] t, double[] n) {
        double[] allPeaks = new double[maxPeaks];
        long[] peakTimes = new long[maxPeaks], highTimes = new long[maxPeaks];

        double tmp1 = n[0], tmp2 = n[1], tmp3 = n[2];
        int highPeakCount = 0;
        float favr = 0;
        double highest = 0, threshold = 0.9, avr = 0;

        // runs through array and grabs peaks
        for (int i = 3, j = 0; i < (n.length - 2); i++) {

            if (tmp2 > tmp1 && tmp2 > tmp3) {
                allPeaks[j] = tmp2;
                peakTimes[j] = t[i];
                j++;
                if (tmp2 > highest) {// remember the highest peak
                    highest = tmp2;
                }
            }

            tmp1 = tmp2;
            tmp2 = tmp3;
            tmp3 = n[i + 1];
        }

        // count peaks above threshold and store their timestamps
        for (int i = 0; i < allPeaks.length; i++) {
            if (allPeaks[i] > threshold * highest) {
                highTimes[highPeakCount] = peakTimes[i];
                highPeakCount++;
            }
        }
        // if not enough peaks are found, the loop executes
        while (highPeakCount < 3 && threshold > 0) {
            // lower the threshold incrementally until enough peaks are found
            threshold -= .05;
            highPeakCount = 0; // reset to avoid a double count

            for (int i = 0; i < allPeaks.length; i++) {
                if (allPeaks[i] > threshold * highest) {
                    // if the loop executes, it will write over the old values
                    highTimes[highPeakCount] = peakTimes[i];
                    highPeakCount++;
                }
            }
        }

        // calcs the actual average time between given peaks
        if (highPeakCount < 3) {
            avr = 0;
        } else {
            for (int i = 0; i < (highPeakCount - 1); i++) {
                // for now avr is the sum of each difference
                avr += (highTimes[i + 1] - highTimes[i]);
            }
            // avr becomes the average of those differences
            avr = avr / (highPeakCount - 1);
        }
        favr = (float) avr;
        return favr;
    }

    /**
     * returns the absolute deviation of a tuple
     *
     * @param count
     *            number of entries in the tuple
     * @param n
     *            the array of entries
     * @param navr
     *            the average value of all entries in the array
     * @return
     */
    public static float getAbsDev(int count, double[] n, float navr) {
        double aDev = 0;
        for (int i = 0; i < count; i++) {
            aDev += Math.abs(n[i] - navr);
        }
        aDev = aDev / count;

        return (float) aDev;
    }

    /**
     * generates standard deviation statistic for one axis in one 10 second
     * tuple
     *
     * @param count
     *            number of (real) entries in the array
     * @param n
     *            the array of accelerometer values
     * @param nAvr
     *            the average value of the entries in n
     * @return the standard deviation statistic
     */
    public static float getSDiv(int count, double[] n, float nAvr) {
        double nSDiv = 0; // standard deviations for each variable
        // generates sums of squares of differences to use an standard deviation
        for (int k = 0; k < count; k++) {
            nSDiv += ((n[k] - nAvr) * (n[k] - nAvr));
        }
        nSDiv = (Math.sqrt(nSDiv)) / count;

        return (float) nSDiv;
    }

    /**
     * returns the average of an input array's values
     *
     * @param count
     *            number of real values in the array
     * @param n
     *            the array
     * @return an average in type float
     */
    public static float getAvr(int count, double[] n) {
        float avr = 0;
        for (int i = 0; i < count; i++) {
            avr += n[i];
        }
        avr = avr / count;

        return avr;
    }

    /**
     * returns array of the sum of the absolute values
     *	of the 3 axes for each time in the tuple
     *
     * @param count
     *            number of entries in the tuple
     * @param x
     *            the array of entries for x axis
     * @param x
     *            the array of entries for y axis
     * @param x
     *            the array of entries for z axis
     * @return
     */
    public static double[] getAbsSum(int count, double[] x, double[] y, double[] z){
        double [] values = new double[count];
        for (int i = 0; i < count; i++){
            values[i] = ((Math.abs(x[i])) + (Math.abs(y[i])) + (Math.abs(z[i])));
        }

        return values;
    }

    /**
     * returns the mean of the sum of the absolute values
     *	of the 3 axes of a tuple
     *
     * @param count
     *            number of entries in the tuple
     * @param x
     *            the array of entries for x axis
     * @param x
     *            the array of entries for y axis
     * @param x
     *            the array of entries for z axis
     * @return
     */
    public static float getAvrAbsSum(int count, double[] x, double[] y, double[] z){
        double[] values = getAbsSum(count, x, y, z);

        return getAvr(count, values);
    }

    /**
     * returns the standard deviation of the sum of the absolute values
     *	of the 3 axes of a tuple
     *
     * @param count
     *            number of entries in the tuple
     * @param x
     *            the array of entries for x axis
     * @param x
     *            the array of entries for y axis
     * @param x
     *            the array of entries for z axis
     * @return
     */
    public static float getSDAbsSum(int count, double[] x, double[] y, double[] z){
        float avr = getAvrAbsSum(count, x, y, z);
        double[] values = getAbsSum(count, x, y, z);

        return getSDiv(count, values, avr);
    }

    /**
     *returns array of the root of the sum of the squares (magnitude)
     *	of the 3 axes for each time in the tuple
     *
     * @param count
     *            number of entries in the tuple
     * @param x
     *            the array of entries for x axis
     * @param y
     *            the array of entries for y axis
     * @param z
     *            the array of entries for z axis
     * @return
     */
    public static double[] getMagnitude(int count, double[] x, double[] y, double[] z){
        double [] values = new double [count];
        double sum = 0;
        for (int i = 0; i < count; i++){
            sum = ((Math.pow(x[i],2)) + (Math.pow(y[i],2)) + (Math.pow(z[i],2)));
            values[i] = (Math.sqrt(sum));
        }

        return values;
    }

    /**
     * returns the mean of the root of the squares
     *	of the 3 axes
     *
     * @param count
     *            number of entries in the tuple
     * @param x
     *            the array of entries for x axis
     * @param x
     *            the array of entries for y axis
     * @param x
     *            the array of entries for z axis
     * @return
     */
    public static float getAvrMagnitude(int count, double[] x, double[] y, double[] z){
        double[] values = getMagnitude(count, x, y, z);

        return getAvr(count, values);
    }
}
