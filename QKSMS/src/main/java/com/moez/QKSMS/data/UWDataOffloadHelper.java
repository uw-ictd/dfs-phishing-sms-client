package com.moez.QKSMS.data;

import android.content.ContentResolver;
import android.database.sqlite.SQLiteDatabase;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.moez.QKSMS.R;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.dialog.QKDialog;
import java.util.*;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.content.ContentValues;
import android.net.Uri;
import android.telephony.TelephonyManager;

public class UWDataOffloadHelper {

    private static final String SERVER_PROTOCOL = "https://";
    private static final String SERVER_DOMAIN = "prothean.cs.washington.edu/sms/";
    private static final String SERVER_ADDRESS_ADD = SERVER_PROTOCOL + SERVER_DOMAIN + "add/";
    private static final String SERVER_ADDRESS_KILL = SERVER_PROTOCOL + SERVER_DOMAIN + "kill/";

    private static final String UW_GET_UNSENT_RECORDS = MessageSidebandDBHelper.SIDEBAND_COLUMN_SENT_TO_UW + " = '0'";
    private MessageSidebandDBHelper messageDB = null;
    private SQLiteDatabase myDb;

    // Map the name of the column on the server database to the name of the
    // local field. This helps with collisions like "type" for SMS and
    // "message_type" for MMS.
    private static final Map<String, String> SMS_AND_MMS_FIELDS;
    private static final Map<String, String> SMS_FIELDS;
    private static final Map<String, String> MMS_FIELDS;
    // Initialize the fields.
    static
    {
        // Server DB --> Local DB
        SMS_AND_MMS_FIELDS = new HashMap<String, String>();
        // Android content provider.
        SMS_AND_MMS_FIELDS.put("creator", "creator");
        SMS_AND_MMS_FIELDS.put("date", "date");
        SMS_AND_MMS_FIELDS.put("date_sent", "date_sent");
        SMS_AND_MMS_FIELDS.put("read", "read");
        SMS_AND_MMS_FIELDS.put("subject", "subject");
        SMS_AND_MMS_FIELDS.put("status", "status");
        SMS_AND_MMS_FIELDS.put("thread_id", "thread_id");
        SMS_AND_MMS_FIELDS.put("subscription_id", "subscription_id");
        // UW Sideband DB extras.
        SMS_AND_MMS_FIELDS.put("smishing_marked_as", "smishing_marked_as");
        SMS_AND_MMS_FIELDS.put("messagedb_id", "messagedb_id");
        // TODO: Add network state and location columns to SidebandDB.
        SMS_AND_MMS_FIELDS.put("longitude", "longitude");
        SMS_AND_MMS_FIELDS.put("latitude", "latitude");
        SMS_AND_MMS_FIELDS.put("network_state", "network_state");

        // SMS
        SMS_FIELDS = new HashMap<String, String>();
        // Android content provider.
        SMS_FIELDS.put("address", "address");
        SMS_FIELDS.put("body", "body");
        SMS_FIELDS.put("error_code", "error_code");
        SMS_FIELDS.put("person", "person");
        SMS_FIELDS.put("protocol", "protocol");
        SMS_FIELDS.put("reply_path_present", "reply_path_present");
        SMS_FIELDS.put("service_center", "service_center");
        SMS_FIELDS.put("type", "type");
        // UW Sideband DB extras.
        SMS_FIELDS.put("is_email", "is_email");
        SMS_FIELDS.put("email_from", "email_from");
        SMS_FIELDS.put("email_body", "email_body");
        SMS_FIELDS.put("origin_address", "origin_address");

        MMS_FIELDS = new HashMap<String, String>();
        // Android content provider.
        // TODO: body?
        MMS_FIELDS.put("type", "message_type");
        MMS_FIELDS.put("text_only", "text_only");
        // UW Sideband DB extras.
        // Addressee is is the address of the sender. This is specific to MMS
        // because SMS equivalent is pulled from the Android content provider.
        // It's called addressee so it doesn't interfere with Android content.
        MMS_FIELDS.put("address", "addressee");
        // TODO: Get full recipients list from MMS.
        // MMS_FIELDS.put("all_recipients", "?");
    }
    // OTHER SENT FIELDS, just for reference here.
    //      is_mms, all_recipients


    public void startOffload(QKActivity context) {

        if(messageDB == null) {
            messageDB = new MessageSidebandDBHelper(context.getApplicationContext());
        }

        myDb = messageDB.getReadableDatabase();
        Cursor myCursor = myDb.query(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB, null, UW_GET_UNSENT_RECORDS,null,null,null,null );

        ContentResolver resolver = context.getContentResolver();

        TelephonyManager tMgr = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
        String myPhoneNumber = tMgr.getLine1Number();

        if(myCursor.moveToFirst()) {
            do {
                ContentValues tempVals = new ContentValues();
                tempVals.put("user_phone_number", myPhoneNumber );
                DatabaseUtils.cursorRowToContentValues(myCursor, tempVals);
                Uri oneMessage = Uri.parse(myCursor.getString(myCursor.getColumnIndex(MessageSidebandDBHelper.SIDEBAND_COLUMN_MESSAGEDB_ID)));
                Cursor cursor = resolver.query(oneMessage,null,null,null,null);
                if(cursor.moveToFirst()) {
                    DatabaseUtils.cursorRowToContentValues(cursor,tempVals);
                }

                StringRequest request = new StringRequest(Request.Method.POST, SERVER_ADDRESS_ADD, response -> {

                    //Update the flag for sent to make sure that it doesn't get sent again.
                    SidebandDBSource sidebandDb = new SidebandDBSource(context.getApplicationContext());
                    sidebandDb.setMessageSidebandDBEntryByArg(response, MessageSidebandDBHelper.SIDEBAND_COLUMN_SENT_TO_UW, "1");

                }, error -> {
                    VolleyError i = error;
                        //context.makeToast();
                }){
                    @Override
                    protected Map<String,String> getParams() throws com.android.volley.AuthFailureError {
                        Map<String,String> params = new HashMap<String, String>();
                        //Telephony input
                        params.put("user_phone",myPhoneNumber);
                        // Add the common fields from Android and SidebandDB.
                        for (String serverField : SMS_AND_MMS_FIELDS.keySet()) {
                            String localField = SMS_AND_MMS_FIELDS.get(serverField);
                            if (tempVals.getAsString(localField) == null) {
                                params.put(serverField, "NULL");
                            } else {
                                params.put(serverField,
                                        tempVals.getAsString(localField));
                            }
                        }
                        // Add the SMS-specific fields.
                        if (oneMessage.toString().contains("sms")) {
                            for (String serverField : SMS_FIELDS.keySet()) {
                                String localField = SMS_FIELDS.get(serverField);
                                if (tempVals.getAsString(localField) == null) {
                                    params.put(serverField, "NULL");
                                } else {
                                    params.put(serverField,
                                            tempVals.getAsString(localField));
                                }
                            }
                            // Set as SMS.
                            params.put("is_mms", "0");
                        }

                        // MMS
                        if (oneMessage.toString().contains("mms")) {
                            for (String serverField : MMS_FIELDS.keySet()) {
                                String localField = MMS_FIELDS.get(serverField);
                                if (tempVals.getAsString(localField) == null) {
                                    params.put(serverField, "NULL");
                                } else {
                                    params.put(serverField,
                                            tempVals.getAsString(localField));
                                }
                            }
                            // TODO: Get all recipients from MMS.
                            // Android sms content.
                            params.put("all_recipients", " ");
                            // Set as MMS.
                            params.put("is_mms", "1");
                        }

                        return params;
                    }

                    /*@Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
                        return params;
                    }*/
                };

                context.getRequestQueue().add(request);
            } while(myCursor.moveToNext());
        }
        myCursor.close();

        Cursor killCursor = myDb.query(MessageSidebandDBHelper.TABLE_NAME_PRIVACYDB, null, null, null, null, null, null );
        if(killCursor.moveToFirst()) {
            do {
                //String addressee = killCursor.getString(killCursor.getColumnIndex(MessageSidebandDBHelper.PRIVACY_COLUMN_ADDRESSEE));
                long thread_id = killCursor.getLong(killCursor.getColumnIndex(MessageSidebandDBHelper.PRIVACY_COLUMN_THREAD_ID));

                StringRequest request = new StringRequest(Request.Method.POST, SERVER_ADDRESS_KILL, response -> {
                    //Nothing to do on response...server sends num of items discarded, we don't need or care
                    String i = response;
                }, error -> {
                    //context.makeToast();
                }){
                    @Override
                    protected Map<String,String> getParams() throws com.android.volley.AuthFailureError {
                        Map<String,String> params = new HashMap<String, String>();
                        //Telephony input
                        params.put("user_phone",myPhoneNumber);
                        //get the addressee of the kill list item
                        params.put("thread_id",Long.toString(thread_id));

                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
                        return params;
                    }
                };

                context.getRequestQueue().add(request);
            } while(killCursor.moveToNext());
        }


        new QKDialog()
                .setContext(context)
                .setTitle(R.string.messagas_synced)
                .show();
    }
}
