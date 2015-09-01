package dk.troelssiggaard.iacontacts.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ts.
 */
public class DBHelper extends SQLiteOpenHelper {


    public static final String TABLE_USERS = "users";

    public static final String COL_USER_ID = "_id";                     // row 1
    public static final String COL_PREDICTION_TIMESTAMP = "timestamp";  // row 2

    public static final String COL_ACTIVITY_PREDICTION = "activity";    // row 3

    public static final String COL_LOCATION_PREDICTION = "location";    // row 4
    public static final String COL_LOCATION_DEPARTMENT = "department";  // row 5

    public static final String COL_PROXIMITY = "proximity";             // row 6

    public static final String COL_INTERRUPTIBILITY = "interruptibility"; // row 7

    public static final String COL_IN_A_CALL = "in_a_call";               // row 8

    public static final String COL_PHONE_IN_USE = "in_use";              // row 9
    public static final String COL_PHONE_ON_SILENT = "on_silent";        // row 10
    public static final String COL_PHONE_IS_CHARGING = "is_charging";    // row 11

    public static final String COL_USER_IS_DICTATING = "is_dictating";    // row 12

    private static final String DB_NAME = "user_prediction.db";
    private static final int DB_VERSION = 1; // Used when updating the App

    public static final int HARDCODED_USER_ID = 1;


    // Database creation sql statement
    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE_USERS
            + "("
            + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_PREDICTION_TIMESTAMP + " INTEGER, " // store type long as unsigned integer
            + COL_ACTIVITY_PREDICTION + " TEXT, "
            + COL_LOCATION_DEPARTMENT + " TEXT, "
            + COL_LOCATION_PREDICTION + " TEXT, "
            + COL_PROXIMITY + " INTEGER, "            // store type boolean as integer (0=false/1=true)
            + COL_INTERRUPTIBILITY + " INTEGER, "
            + COL_IN_A_CALL + " INTEGER, "            // store type boolean as integer (0=false/1=true)
            + COL_PHONE_IN_USE + " INTEGER, "         // store type boolean as integer (0=false/1=true)
            + COL_PHONE_ON_SILENT + " INTEGER, "      // store type boolean as integer (0=false/1=true)
            + COL_PHONE_IS_CHARGING + " INTEGER, "      // store type boolean as integer (0=false/1=true)
            + COL_USER_IS_DICTATING + " INTEGER"      // store type boolean as integer (0=false/1=true)
            + ");";


    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);

        // Check if the database is empty
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
        if(!cursor.moveToFirst()) {
            // INSERT user data
            ContentValues values = new ContentValues();
            //values.put(DBHelper.COL_USER_ID,"1");
            values.put(DBHelper.COL_PREDICTION_TIMESTAMP, "0");
            values.put(DBHelper.COL_ACTIVITY_PREDICTION, "Unknown/Idle");
            values.put(DBHelper.COL_LOCATION_DEPARTMENT, "Unknown");
            values.put(DBHelper.COL_LOCATION_PREDICTION, "Unknown");
            values.put(DBHelper.COL_PROXIMITY, "0");
            values.put(DBHelper.COL_INTERRUPTIBILITY, "0");
            values.put(DBHelper.COL_IN_A_CALL, "0");
            values.put(DBHelper.COL_PHONE_IN_USE, "0");
            values.put(DBHelper.COL_PHONE_ON_SILENT, "0");
            values.put(DBHelper.COL_PHONE_IS_CHARGING, "0");
            values.put(DBHelper.COL_USER_IS_DICTATING,"0");
            long rowID = db.insert(DBHelper.TABLE_USERS, null, values);

            Log.i("DBHelper", "Inserted rowId: " + rowID);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}
