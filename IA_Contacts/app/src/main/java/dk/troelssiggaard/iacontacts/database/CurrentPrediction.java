package dk.troelssiggaard.iacontacts.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by ts.
 */
public class CurrentPrediction {


    // Database fields
    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public CurrentPrediction(Context context) {
        dbHelper = new DBHelper(context);
        open();
    }

    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        db.close();
        dbHelper.close();
    }

    public Cursor getDBResult(String dbColumn, int userID){

        String[] dbColumns = {dbColumn};
        String[] dbUser = {DBHelper.HARDCODED_USER_ID+""};

        return db.query(DBHelper.TABLE_USERS, dbColumns, null, null,null,null,null);
    }

    public void updateStringDB(String columnName, String columnValue , int userID) {
        ContentValues values = new ContentValues(1);
        values.put(columnName, columnValue);
        String[] users = {userID+""};
        db.update(DBHelper.TABLE_USERS,values,DBHelper.COL_USER_ID+"= ?",users);
    }

    public void updateIntegerDB(String columnName, int columnValue , int userID) {
        ContentValues values = new ContentValues(1);
        values.put(columnName, columnValue);
        String[] users = {userID+""};
        db.update(DBHelper.TABLE_USERS, values, DBHelper.COL_USER_ID+"= ?", users);
    }

    public void updateLongDB(String columnName, long columnValue , int userID) {
        ContentValues values = new ContentValues(1);
        values.put(columnName, columnValue);
        String[] users = {userID+""};
        db.update(DBHelper.TABLE_USERS, values, DBHelper.COL_USER_ID+"= ?", users);
    }

    public void updateBooleanDB(String columnName, boolean columnValue , int userID) {
        ContentValues values = new ContentValues(1);
        values.put(columnName, columnValue);
        String[] users = {userID+""};
        db.update(DBHelper.TABLE_USERS, values, DBHelper.COL_USER_ID+"= ?", users);
    }

    public String getLatestActivity() {
        Cursor cursor = getDBResult(DBHelper.COL_ACTIVITY_PREDICTION,DBHelper.HARDCODED_USER_ID);
        if(cursor.moveToFirst()) {
            String result = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ACTIVITY_PREDICTION));
            cursor.close();
            return result;
        }else{
            return "";
        }
    }

    public void setLatestActivity(String prediction) {
        if(prediction != null) {
            updateStringDB(DBHelper.COL_ACTIVITY_PREDICTION, prediction, DBHelper.HARDCODED_USER_ID);
        }
    }

//    public void setLatestActivityTimestamp(long timestamp){
//        latestActivityTimestamp = timestamp;
//    }

//    public long getLatestActivityTimestamp() {
//        return latestActivityTimestamp;
//    }

    public String getLatestLocationName(){
        Cursor cursor = getDBResult(DBHelper.COL_LOCATION_PREDICTION,DBHelper.HARDCODED_USER_ID);
        if(cursor.moveToFirst()) {
            String result = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_LOCATION_PREDICTION));
            cursor.close();
            return result;
        }else{
            return "";
        }
    }

    public void setLatestLocationName(String beaconName) {
        if(beaconName != null) {
            updateStringDB(DBHelper.COL_LOCATION_PREDICTION, beaconName, DBHelper.HARDCODED_USER_ID);
        }
    }


//    public long getLatestLocationTimestamp(){
//        return latestLocationTimestamp;
//    }

    public void setLatestDepartmentName(String currentBeaconName) {

        String latestDepartmentName = "Unknown";

        if(currentBeaconName != null) {
            String[] array = currentBeaconName.split(",");
            latestDepartmentName = array[array.length - 1];

            updateStringDB(DBHelper.COL_LOCATION_DEPARTMENT, latestDepartmentName, DBHelper.HARDCODED_USER_ID);
        }
    }

    public String getLatestDepartmentName(){
        Cursor cursor = getDBResult(DBHelper.COL_LOCATION_DEPARTMENT,DBHelper.HARDCODED_USER_ID);
        if(cursor.moveToFirst()) {
            String result = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_LOCATION_DEPARTMENT));
            cursor.close();
            return result;
        }else{
            return "";
        }
    }

    public String getRoomIdentifier() {

        // Get the first word of the Device (Bluetooth Beacon) Name
        if(getLatestLocationName() != null) {
            String[] array = getLatestLocationName().split(" ");

            return array[0]; // Return first name (string) of the location = identifyer (eg. Office, Patient, Scanner, Meeting etc.)
        }
        return "";
    }

    public void setPhoneInPocket(boolean inPocketValue) {
        updateBooleanDB(DBHelper.COL_PROXIMITY,inPocketValue,DBHelper.HARDCODED_USER_ID);
    }

    public boolean getPhoneInPocket() {
        Cursor cursor = getDBResult(DBHelper.COL_PROXIMITY,DBHelper.HARDCODED_USER_ID);
        if(cursor.moveToFirst()) {
            boolean result = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_PROXIMITY))>0;
            cursor.close();
            return result;
        }
        return false;
    }

    public void setInACall(boolean inCall){
        updateBooleanDB(DBHelper.COL_IN_A_CALL,inCall,DBHelper.HARDCODED_USER_ID);
    }

    public boolean getInACall(){
        Cursor cursor = getDBResult(DBHelper.COL_IN_A_CALL,DBHelper.HARDCODED_USER_ID);
        if(cursor.moveToFirst()) {
            boolean result = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_IN_A_CALL))>0;
            cursor.close();
            return result;
        }
        return false;
    }

    public void setOnSilent(boolean isSilent) {
        updateBooleanDB(DBHelper.COL_PHONE_ON_SILENT, isSilent,DBHelper.HARDCODED_USER_ID);
    }

    public boolean getOnSilent() {
        Cursor cursor = getDBResult(DBHelper.COL_PHONE_ON_SILENT,DBHelper.HARDCODED_USER_ID);
        if(cursor.moveToFirst()) {
            boolean result = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_PHONE_ON_SILENT))>0;
            cursor.close();
            return result;
        }
        return false;
    }

    public void setPhoneInUse(boolean phoneInUse){
        updateBooleanDB(DBHelper.COL_PHONE_IN_USE,phoneInUse,DBHelper.HARDCODED_USER_ID);
    }

    public boolean getPhoneInUse(){
        Cursor cursor = getDBResult(DBHelper.COL_PHONE_IN_USE,DBHelper.HARDCODED_USER_ID);
        if(cursor.moveToFirst()) {
            boolean result = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_PHONE_IN_USE))>0;
            cursor.close();
            return result;
        }
        return false;
    }

    public void setIsCharging(boolean isCharging){
        updateBooleanDB(DBHelper.COL_PHONE_IS_CHARGING,isCharging,DBHelper.HARDCODED_USER_ID);
    }

    public void setIsDictating(boolean isDictating){
        updateBooleanDB(DBHelper.COL_USER_IS_DICTATING,isDictating,DBHelper.HARDCODED_USER_ID);
    }

    public boolean getIsDictating(){
        Cursor cursor = getDBResult(DBHelper.COL_USER_IS_DICTATING,DBHelper.HARDCODED_USER_ID);
        if(cursor.moveToFirst()) {
            boolean result = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_USER_IS_DICTATING))>0;
            cursor.close();
            return result;
        }
        return false;
    }


    public boolean getIsCharging(){
        Cursor cursor = getDBResult(DBHelper.COL_PHONE_IS_CHARGING,DBHelper.HARDCODED_USER_ID);
        if(cursor.moveToFirst()) {
            boolean result = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_PHONE_IS_CHARGING))>0;
            cursor.close();
            return result;
        }
        return false;
    }

    public int getLatestInterruptibilityPrediction() {
        Cursor cursor = getDBResult(DBHelper.COL_INTERRUPTIBILITY,DBHelper.HARDCODED_USER_ID);
        if(cursor.moveToFirst()) {
            int result = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_INTERRUPTIBILITY));
            cursor.close();
            return result;
        }
        return 0;
    }

    public void setLatestInterruptibilityPrediction(int interruptibility) {
        updateIntegerDB(DBHelper.COL_INTERRUPTIBILITY, interruptibility,DBHelper.HARDCODED_USER_ID);
    }

    public void setTimestamp(long timestampNow) {
        if(timestampNow != 0) {
            updateLongDB(DBHelper.COL_PREDICTION_TIMESTAMP, timestampNow, DBHelper.HARDCODED_USER_ID);
        }
    }

    public long getTimestamp() {
        Cursor cursor = getDBResult(DBHelper.COL_PREDICTION_TIMESTAMP,DBHelper.HARDCODED_USER_ID);
        if(cursor.moveToFirst()) {
            long result = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_PREDICTION_TIMESTAMP));
            cursor.close();
            return result;
        }
        return 0L;
    }
}
