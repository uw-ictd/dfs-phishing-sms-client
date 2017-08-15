package com.moez.QKSMS.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.telephony.TelephonyManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.moez.QKSMS.R;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.dialog.QKDialog;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.util.concurrent.ExecutionException;

import timber.log.Timber;

@EBean
public class UWDataOffloadHelper {

    private static final String SERVER_PROTOCOL = "https://";
    private static final String SERVER_DOMAIN = "prothean.cs.washington.edu/sms/";
    private static final String SERVER_ADDRESS_SYNC = SERVER_PROTOCOL + SERVER_DOMAIN + "sync/";

    private static final String UW_GET_UNSENT_RECORDS = MessageSidebandDBHelper.SIDEBAND_COLUMN_SENT_TO_UW + " = '0'";
    private MessageSidebandDBHelper messageDB = null;
    private SQLiteDatabase myDb;

    @Background
    public void startOffloadInBackground(QKActivity context) {

        Timber.d("startOffloadInBackground");

        if (messageDB == null) {
            messageDB = new MessageSidebandDBHelper(context.getApplicationContext());
        }

        myDb = messageDB.getReadableDatabase();
        Cursor myCursor = myDb.query(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB, null, UW_GET_UNSENT_RECORDS, null, null, null, null);

        ContentResolver resolver = context.getContentResolver();

        TelephonyManager tMgr = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        String myPhoneNumber = tMgr.getLine1Number();

        // Collect all messages to be added/updated on the server in this array.
        JsonArray messagesToAddOrUpdate = new JsonArray();

        if (myCursor.moveToFirst()) {
            do {
                ContentValues tempVals = new ContentValues();
                tempVals.put("user_phone_number", myPhoneNumber);
                DatabaseUtils.cursorRowToContentValues(myCursor, tempVals);
                Uri oneMessage = Uri.parse(myCursor.getString(myCursor.getColumnIndex(MessageSidebandDBHelper.SIDEBAND_COLUMN_MESSAGEDB_ID)));
                Cursor cursor = resolver.query(oneMessage, null, null, null, null);
                if (cursor.moveToFirst()) {
                    DatabaseUtils.cursorRowToContentValues(cursor, tempVals);
                }

                SMSInfo sms = new SMSInfo(myPhoneNumber, tempVals, oneMessage);
                messagesToAddOrUpdate.add(sms.toJson());

            } while (myCursor.moveToNext());
        }
        myCursor.close();

        Timber.d("messages json array: %s", messagesToAddOrUpdate.toString());

        // Collect all private thread IDs that are to be deleted from the server in this array
        JsonArray privateMessages = new JsonArray();

        Cursor killCursor = myDb.query(MessageSidebandDBHelper.TABLE_NAME_PRIVACYDB, null, null, null, null, null, null);
        if (killCursor.moveToFirst()) {
            do {
                //String addressee = killCursor.getString(killCursor.getColumnIndex(MessageSidebandDBHelper.PRIVACY_COLUMN_ADDRESSEE));
                long thread_id = killCursor.getLong(killCursor.getColumnIndex(MessageSidebandDBHelper.PRIVACY_COLUMN_THREAD_ID));
                String threadID = thread_id + ""; // this field is stored as text on the server.

                privateMessages.add(threadID);

            } while (killCursor.moveToNext());
        }


        // Prepare JSON with messages that are to be added/udpated/delete
        JsonObject jsonToPost = new JsonObject();
        jsonToPost.addProperty("user_id", myPhoneNumber);
        if (messagesToAddOrUpdate.size() > 0) {
            jsonToPost.add("add", messagesToAddOrUpdate);
        }
        if (privateMessages.size() > 0) {
            jsonToPost.add("delete", privateMessages);
        }

        if (jsonToPost.has("add") || jsonToPost.has("delete")) {
            // If we we have anything to add or delete, make the POST request

            JsonObject result = null;
            Timber.d("json to server: %s", jsonToPost.toString());

            // This is a blocking POST request. Use only on a background thread.
            try {
                result = Ion.with(context)
                        .load(SERVER_ADDRESS_SYNC)
                        .setJsonObjectBody(jsonToPost)
                        .asJsonObject()
                        .get();
            } catch (InterruptedException | ExecutionException ex) {
                Timber.e(ex, "Error while trying to upload messages : %s", messagesToAddOrUpdate.toString());
            }
            Timber.d("Response from server: %s", result);

            /*
              Serve returns a JSON with list of message IDs that were successfully added.

              Example result JSON: {"added": [{"content://sms/2", "content://sms/4", "content://mms/10"}]}
             */
            if (result != null && result.has("added")) {
                SidebandDBSource sidebandDb = new SidebandDBSource(context.getApplicationContext());
                JsonArray messageIDs = result.getAsJsonArray("added");
                for (int i = 0; i < messageIDs.size(); i++) {
                    String message_id = messageIDs.get(i).getAsString();
                    sidebandDb.setMessageSidebandDBEntryByArg(message_id, MessageSidebandDBHelper.SIDEBAND_COLUMN_SENT_TO_UW, "1");
                }
            } else if (messagesToAddOrUpdate.size() > 0) {
                Timber.w("Got a null or failed response for the POST request to added messages");
            }
        } else {
            Timber.d("Nothing to sync");
        }
        showSyncDoneMessage(context);

    }

    @UiThread
    public void showSyncDoneMessage(QKActivity context) {
        new QKDialog()
                .setContext(context)
                .setTitle(R.string.messagas_synced)
                .show();
    }
}
