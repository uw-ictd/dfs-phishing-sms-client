package com.moez.QKSMS.data;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.database.SQLException;
import android.provider.BaseColumns;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
//import android.graphics.Bitmap;
//import android.net.Uri;
import com.moez.QKSMS.R;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.dialog.DefaultSmsHelper;

public class MessageSidebandDBHelper extends SQLiteOpenHelper {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    //private MessageSidebandDB() {}

    /* Inner class that defines the table contents */
    public static final String TABLE_NAME_SIDEBANDDB = "sms_sideband_db";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MESSAGEDB_ID = "messagedb_id";
    public static final String COLUMN_EXTRAINFO = "extrainfo";
    public static final String COLUMN_SENT_TO_UW = "sent_to_uw";

    private static final String DATABASE_NAME = "smishing_sideband.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE =
            "CREATE TABLE " + TABLE_NAME_SIDEBANDDB + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_MESSAGEDB_ID + " TEXT," +
                    COLUMN_EXTRAINFO + " TEXT," +
                    COLUMN_SENT_TO_UW + " INTEGER DEFAULT 0);";

    public MessageSidebandDBHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MessageSidebandDBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_SIDEBANDDB);
        onCreate(db);
    }

}