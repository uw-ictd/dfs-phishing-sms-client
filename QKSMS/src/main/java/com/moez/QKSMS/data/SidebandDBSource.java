package com.moez.QKSMS.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class SidebandDBSource {

    public static final String MESSAGE_SENT = "1";
    public static final String MESSAGE_UNSENT = "0";

    public static final String UW_MESSAGE_IS_SPAM = "SPAM,";
    public static final String UW_MESSAGE_IS_SCAM = "SCAM,";
    public static final String UW_MESSAGE_IS_FRAUD = "FRAUD,";
    public static final String UW_MESSAGE_IS_OK = "OK,";
    public static final String UW_MESSAGE_IS_UNKOWN = "unkown,";

    // Database fields
    private SQLiteDatabase database;
    private MessageSidebandDBHelper dbHelper;

    //full column list of sms_sideband_db
    private String[] allColumnsSideband = { MessageSidebandDBHelper.SIDEBAND_COLUMN_ID,
            MessageSidebandDBHelper.SIDEBAND_COLUMN_MESSAGEDB_ID,
            MessageSidebandDBHelper.SIDEBAND_COLUMN_THREAD_ID,
            MessageSidebandDBHelper.SIDEBAND_COLUMN_ADDRESSEE,
            MessageSidebandDBHelper.SIDEBAND_COLUMN_EXTRAINFO,
            MessageSidebandDBHelper.SIDEBAND_COLUMN_SENT_TO_UW};

    //full column list of sms_privacy_db
    private String[] allColumnsPrivacy = { MessageSidebandDBHelper.PRIVACY_COLUMN_ID,
            MessageSidebandDBHelper.PRIVACY_COLUMN_THREAD_ID};
            //MessageSidebandDBHelper.PRIVACY_COLUMN_ADDRESSEE};

    public SidebandDBSource(Context context) {
        dbHelper = new MessageSidebandDBHelper(context);
    }


    //database managment calls
    public void openWrite() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void openRead() throws SQLException {
        database = dbHelper.getReadableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    //sms_sideband_db accessors
    public Boolean createNewMessageSidebandDBEntry(String messagedb_id, String extra_info, long thread_id, String addressee) {
        ContentValues values = new ContentValues();
        addressee = stripChars(addressee);
        values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_MESSAGEDB_ID, messagedb_id);
        values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_EXTRAINFO, extra_info);
        values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_THREAD_ID, thread_id);
        values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_ADDRESSEE, addressee);
        if(getThreadIsPrivate(thread_id)) {
            values.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_SENT_TO_UW, MESSAGE_SENT);
        }
        openWrite();
        long insertId = database.insert(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB, null, values);
        Cursor cursor = database.query(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                allColumnsSideband, MessageSidebandDBHelper.SIDEBAND_COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        cursor.close();
        close();
        return true;
    }

    public String getMessageSidebandDbEntryByThreadID(long thread_id, String field) {
        String [] columns = { field };
        String where = MessageSidebandDBHelper.SIDEBAND_COLUMN_THREAD_ID + "=?";
        String [] whereArgs = { Long.toString(thread_id) };
        String returnval = "";

        openRead();
        Cursor cursor = database.query(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                columns, where, whereArgs, null, null, null);
        if (cursor.moveToFirst()) {
            returnval = cursor.getString(cursor.getColumnIndex(field));
        }
        cursor.close();
        close();
        return returnval;
    }


    public String getMessageSidebandDBEntryByArg(String messagedb_id, String field) {

        String [] columns = { field };
        String where = MessageSidebandDBHelper.SIDEBAND_COLUMN_MESSAGEDB_ID + "=?";
        String [] whereArgs = { messagedb_id};
        String returnval = "";

        openRead();
        Cursor cursor = database.query(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                            columns, where, whereArgs, null, null, null);
        if (cursor.moveToFirst()) {
            returnval = cursor.getString(cursor.getColumnIndex(field));
        }
        cursor.close();
        close();
        return returnval;

    }

    public int setConversationSidebandDBEntryByThreadID(long thread_id, String field, String newVal) {
        ContentValues dataToUpdate = new ContentValues();
        dataToUpdate.put(field,newVal);



        String where = MessageSidebandDBHelper.SIDEBAND_COLUMN_THREAD_ID + "=?";
        String [] whereArgs = {Long.toString(thread_id)};


        int returnval = -1;

        openWrite();

        returnval = database.update(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                dataToUpdate, where, whereArgs);

        close();
        return returnval;
    }


    public int setMessageSidebandDBEntryByArg(String messagedb_id, String field, String newVal) {

        ContentValues dataToUpdate = new ContentValues();
        dataToUpdate.put(field,newVal);
        String where = MessageSidebandDBHelper.SIDEBAND_COLUMN_MESSAGEDB_ID + "=?";
        String [] whereArgs = { messagedb_id};
        int returnval = -1;

        openWrite();

        returnval += database.update(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                     dataToUpdate, where, whereArgs);

        close();
        return returnval;

    }

    //sms_privacy_db accessors
    public int clearPrivacyDBEntry(long thread_id) {

        String where = MessageSidebandDBHelper.PRIVACY_COLUMN_THREAD_ID + "=?";
        String [] whereArgs = {Long.toString(thread_id)};
        int returnval;

        openRead();
        returnval = database.delete(MessageSidebandDBHelper.TABLE_NAME_PRIVACYDB, where, whereArgs);
        close();

        //mark all messages to the addressee as unsent, meaning they will be pushed up on next update
        markAllThreadIDMsgSent(thread_id);

        return returnval;

    }


    public int setPrivacyDBEntry(long thread_id) {

        ContentValues values = new ContentValues();
        values.put(MessageSidebandDBHelper.PRIVACY_COLUMN_THREAD_ID, thread_id);
        int returnval;

        //Insert this thread into the database or ignore it if is already there (only 1 _real_ column in table so only conflict is itself)
        openWrite();
        returnval = (int)database.insertWithOnConflict(MessageSidebandDBHelper.TABLE_NAME_PRIVACYDB, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        close();

        //mark all messages from this user as sent.  Effectively making them private
        markAllThreadIDMsgSent(thread_id);

        return returnval;

    }


    public boolean getThreadIsPrivate(long thread_id) {

        String [] columns = { MessageSidebandDBHelper.PRIVACY_COLUMN_THREAD_ID};
        String where = MessageSidebandDBHelper.PRIVACY_COLUMN_THREAD_ID + "=?";
        String [] whereArgs = {Long.toString(thread_id)};
        //String [] whereArgs = {stripChars(addressee)};
        Cursor myCursor;
        boolean returnval;

        openRead();
        myCursor = database.query(MessageSidebandDBHelper.TABLE_NAME_PRIVACYDB, columns, where, whereArgs, null, null, null);


        returnval = myCursor.moveToFirst();
        myCursor.close();
        close();


        Cursor myCursor1;
        boolean returnval1;

        openRead();
        myCursor1 = database.rawQuery("Select * from " + MessageSidebandDBHelper.TABLE_NAME_PRIVACYDB,  null);
        returnval1 = myCursor.moveToFirst();
        myCursor1.close();
        close();

        return returnval;
    }

    private int markAllThreadIDMsgSent(long thread_id) {
        ContentValues dataToUpdate = new ContentValues();
        dataToUpdate.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_SENT_TO_UW, MESSAGE_SENT);
        String where = MessageSidebandDBHelper.SIDEBAND_COLUMN_THREAD_ID + "=?";
        String [] whereArgs = { Long.toString(thread_id) };
        int returnval;

        openWrite();

        returnval = database.update(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                dataToUpdate, where, whereArgs);

        close();
        return returnval;
    }

    private int markAllThreadIDMsgUnsent (long thread_id) {

        //Prep for update
        ContentValues dataToUpdate = new ContentValues();
        dataToUpdate.put(MessageSidebandDBHelper.SIDEBAND_COLUMN_SENT_TO_UW, MESSAGE_UNSENT);
        String where = MessageSidebandDBHelper.SIDEBAND_COLUMN_THREAD_ID + "=?";
        String [] whereArgs = {Long.toString(thread_id)};

        int returnval;

        openWrite();
        returnval = database.update(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB,
                dataToUpdate, where, whereArgs);
        close();

        return returnval;
    }

    private String stripChars (String str) {
        str = str.replace(" ","");
        str = str.replace("-","");
        str = str.replace("(","");
        str = str.replace(")","");

        return str;
    }
}