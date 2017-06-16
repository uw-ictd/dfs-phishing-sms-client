package com.moez.QKSMS.data;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.content.Context;

public class MessageSidebandDBHelper extends SQLiteOpenHelper {

    //Database name
    private static final String DATABASE_NAME = "smishing_sideband.db";

    //Database version
    private static final int DATABASE_VERSION = 3;

    //Table names
    public static final String TABLE_NAME_SIDEBANDDB = "sms_sideband_db";
    public static final String TABLE_NAME_PRIVACYDB = "sms_privacy_db";

    //sms_sideband_db columns
    public static final String SIDEBAND_COLUMN_ID = "_id";
    public static final String SIDEBAND_COLUMN_MESSAGEDB_ID = "messagedb_id";
    public static final String SIDEBAND_COLUMN_THREAD_ID = "thread_id";
    public static final String SIDEBAND_COLUMN_ADDRESSEE = "addressee";
    public static final String SIDEBAND_COLUMN_SMISHING_LABEL = "smishing_marked_as";
    public static final String SIDEBAND_COLUMN_SENT_TO_UW = "sent_to_uw";
    public static final String SIDEBAND_COLUMN_IS_EMAIL = "is_email";
    public static final String SIDEBAND_COLUMN_EMAIL_FROM = "email_from";
    public static final String SIDEBAND_COLUMN_EMAIL_BODY = "email_body";
    public static final String SIDEBAND_COLUMN_ORIGIN_ADDRESS = "origin_address";

    //sms_privacy_db columns
    public static final String PRIVACY_COLUMN_ID = "_id";
    //public static final String PRIVACY_COLUMN_ADDRESSEE = "addressee";
    public static final String PRIVACY_COLUMN_THREAD_ID = "thread_id";

    //Table Create Statements
    //sms_sideband_db create
    private static final String TABLE_SIDEBANDDB_CREATE =
            "CREATE TABLE " + TABLE_NAME_SIDEBANDDB + " (" +
                    SIDEBAND_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    SIDEBAND_COLUMN_ADDRESSEE + " TEXT," +
                    SIDEBAND_COLUMN_MESSAGEDB_ID + " TEXT," +
                    SIDEBAND_COLUMN_THREAD_ID + " INTEGER," +
                    SIDEBAND_COLUMN_SMISHING_LABEL + " TEXT," +
                    SIDEBAND_COLUMN_SENT_TO_UW + " TEXT DEFAULT '0'," +
                    SIDEBAND_COLUMN_IS_EMAIL + " INTEGER," +
                    SIDEBAND_COLUMN_EMAIL_FROM + " TEXT," +
                    SIDEBAND_COLUMN_EMAIL_BODY + " TEXT," +
                    SIDEBAND_COLUMN_ORIGIN_ADDRESS + " TEXT)";



    //sms_privacy_db create
    private static final String TABLE_PRIVACYDB_CREATE =
            "CREATE TABLE " + TABLE_NAME_PRIVACYDB + " (" +
                    PRIVACY_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    PRIVACY_COLUMN_THREAD_ID + " INTEGER NOT NULL UNIQUE)";
                    //PRIVACY_COLUMN_ADDRESSEE + " TEXT NOT NULL UNIQUE)";

    private static final String[] TABLE_SIDEBANDDB_ALTER_2 = {
            "ALTER TABLE " + TABLE_NAME_SIDEBANDDB + " ADD COLUMN " +
                    SIDEBAND_COLUMN_IS_EMAIL + " INTEGER " ,
            "ALTER TABLE " + TABLE_NAME_SIDEBANDDB + " ADD COLUMN " +
                    SIDEBAND_COLUMN_EMAIL_FROM + " TEXT " ,
            "ALTER TABLE " + TABLE_NAME_SIDEBANDDB + " ADD COLUMN " +
                    SIDEBAND_COLUMN_EMAIL_BODY + " TEXT " ,
            "ALTER TABLE " + TABLE_NAME_SIDEBANDDB + " ADD COLUMN " +
                    SIDEBAND_COLUMN_ORIGIN_ADDRESS + " TEXT " };

    public MessageSidebandDBHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(TABLE_SIDEBANDDB_CREATE);
        database.execSQL(TABLE_PRIVACYDB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MessageSidebandDBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        switch(oldVersion){
            case 2:
                for (String update_cmd:TABLE_SIDEBANDDB_ALTER_2) {
                    db.execSQL(update_cmd);
                }
                break;
            default:
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_SIDEBANDDB);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PRIVACYDB);
                onCreate(db);
        }
    }

}