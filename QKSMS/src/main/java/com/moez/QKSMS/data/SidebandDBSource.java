package com.moez.QKSMS.data;


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

    public void openWrite() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void openRead() throws SQLException {
        database = dbHelper.getReadableDatabase();
    }


    public void close() {
        dbHelper.close();
    }

    public Boolean createNewMessageSidebandDBEntry(String messagedb_id, String extra_info) {
        ContentValues values = new ContentValues();
        values.put(MessageSidebandDBHelper.COLUMN_MESSAGEDB_ID, messagedb_id);
        values.put(MessageSidebandDBHelper.COLUMN_EXTRAINFO, extra_info);
        openWrite();
        long insertId = database.insert(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB, null, values);
        Cursor cursor = database.query(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                allColumns, MessageSidebandDBHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        cursor.close();
        close();
        return true;
    }

    public String getMessageSidebandDBEntryByArg(String messagedb_id, String field) {

        String [] columns = { field };
        String [] sqlArgs = { messagedb_id};
        String returnval = "";

        openRead();
        Cursor cursor = database.query(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                            columns  , MessageSidebandDBHelper.COLUMN_MESSAGEDB_ID + "=?",
                            sqlArgs,null,null,null);
        if (cursor.moveToFirst()) {
            returnval = cursor.getString(cursor.getColumnIndex(field));
        }
        cursor.close();
        close();
        return returnval;

    }

    public int setMessageSidebandDBEntryByArg(String messagedb_id, String field, String newVal) {

        ContentValues dataToUpdate = new ContentValues();
        dataToUpdate.put(field,newVal);
        String where = MessageSidebandDBHelper.COLUMN_MESSAGEDB_ID + "=?";
        String [] whereArgs = { messagedb_id};
        int returnval = -1;

        openWrite();

        returnval += database.update(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                     dataToUpdate, where, whereArgs);

        close();
        return returnval;

    }


}