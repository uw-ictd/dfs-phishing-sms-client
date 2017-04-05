package com.moez.QKSMS.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SidebandDBSource {

    // Database fields
    private SQLiteDatabase database;
    private MessageSidebandDBHelper dbHelper;
    private String[] allColumns = { MessageSidebandDBHelper.COLUMN_ID,
            MessageSidebandDBHelper.COLUMN_MESSAGEDB_ID,
            MessageSidebandDBHelper.COLUMN_EXTRAINFO,
            MessageSidebandDBHelper.COLUMN_SENT_TO_UW};

    public SidebandDBSource(Context context) {
        dbHelper = new MessageSidebandDBHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Boolean createNewMessageSidebandDBEntry(long messagedb_id, String extra_info) {
        ContentValues values = new ContentValues();
        values.put(MessageSidebandDBHelper.COLUMN_MESSAGEDB_ID, messagedb_id);
        values.put(MessageSidebandDBHelper.COLUMN_EXTRAINFO, extra_info);
        long insertId = database.insert(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB, null, values);
        Cursor cursor = database.query(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                allColumns, MessageSidebandDBHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        cursor.close();
        return true;
    }

    public long getMessageSidebandDBEntrySentToUW(long messagedb_id) {

        ContentValues values = new ContentValues();
        values.put(MessageSidebandDBHelper.COLUMN_SENT_TO_UW,1);
        int returnval = database.update(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                values, MessageSidebandDBHelper.COLUMN_MESSAGEDB_ID + " = " + messagedb_id, null);

        return returnval;

    }
    // Set the sent_to_uw flag in db
    public Boolean setMessageSidebandDBEntrySentToUW(long messagedb_id) {

        ContentValues values = new ContentValues();
        values.put(MessageSidebandDBHelper.COLUMN_SENT_TO_UW,1);
        int returnval = database.update(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                values, MessageSidebandDBHelper.COLUMN_MESSAGEDB_ID + " = " + messagedb_id, null);

        if(returnval == 1)
            return true;

        return false;

    }

    // Clear the sent_to-_uw flag in db
    public Boolean clearMessageSidebandDBEntrySentToUW(long messagedb_id) {

        ContentValues values = new ContentValues();
        values.put(MessageSidebandDBHelper.COLUMN_SENT_TO_UW,0);
        int returnval = database.update(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                values, MessageSidebandDBHelper.COLUMN_MESSAGEDB_ID + " = " + messagedb_id, null);

        if(returnval == 1)
            return true;

        return false;

    }
}