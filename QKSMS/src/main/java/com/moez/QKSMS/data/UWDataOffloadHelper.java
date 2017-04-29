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
import android.content.Context;
import com.moez.QKSMS.data.MessageSidebandDBHelper;

public class UWDataOffloadHelper {

    private static final String SERVER_PROTOCOL = "https://";
    private static final String SERVER_DOMAIN = "prothean.cs.washington.edu/sms/";
    private static final String SERVER_ADDRESS_ADD = SERVER_PROTOCOL + SERVER_DOMAIN + "add/";
    private static final String SERVER_ADDRESS_KILL = SERVER_PROTOCOL + SERVER_DOMAIN + "kill/";

    private static final String UW_GET_UNSENT_RECORDS = MessageSidebandDBHelper.SIDEBAND_COLUMN_SENT_TO_UW + " = '0'";
    private MessageSidebandDBHelper messageDB = null;
    private SQLiteDatabase myDb;

    public void startOffload(QKActivity context) {

        if(messageDB == null) {
            messageDB = new MessageSidebandDBHelper(context.getApplicationContext());
        }

        myDb = messageDB.getReadableDatabase();
        Cursor myCursor = myDb.query(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB, null, UW_GET_UNSENT_RECORDS,null,null,null,null );

        ContentResolver smsResolver = context.getContentResolver();

        TelephonyManager tMgr = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
        String myPhoneNumber = tMgr.getLine1Number();

        if(myCursor.moveToFirst()) {
            do {
                ContentValues tempVals = new ContentValues();
                tempVals.put("user_phone_number", myPhoneNumber );
                DatabaseUtils.cursorRowToContentValues(myCursor, tempVals);
                Uri oneMessage = Uri.parse(myCursor.getString(myCursor.getColumnIndex(MessageSidebandDBHelper.SIDEBAND_COLUMN_MESSAGEDB_ID)));
                Cursor smsCursor = smsResolver.query(oneMessage,null,null,null,null);
                if(smsCursor.moveToFirst()) {
                    DatabaseUtils.cursorRowToContentValues(smsCursor,tempVals);
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
                        //Android sms content provider
                        params.put("address",(tempVals.getAsString("address") == null) ? " " : tempVals.getAsString("address"));
                        params.put("creator",(tempVals.getAsString("creator") == null) ? " " : tempVals.getAsString("creator"));
                        params.put("body",(tempVals.getAsString("body") == null) ? " " : tempVals.getAsString("body"));
                        params.put("date_sent",(tempVals.getAsString("date_sent") == null) ? " " : tempVals.getAsString("date_sent"));
                        params.put("error_code",(tempVals.getAsString("error_code") == null) ? " " : tempVals.getAsString("error_code"));
                        params.put("person",(tempVals.getAsString("person") == null) ? " " : tempVals.getAsString("person"));
                        params.put("read",(tempVals.getAsString("read") == null) ? " " : tempVals.getAsString("read"));
                        params.put("reply_path_present",(tempVals.getAsString("reply_path_present") == null) ? " " : tempVals.getAsString("reply_path_present"));
                        params.put("subject",(tempVals.getAsString("subject") == null) ? " " : tempVals.getAsString("subject"));
                        params.put("status",(tempVals.getAsString("status") == null) ? " " : tempVals.getAsString("status"));
                        params.put("protocol",(tempVals.getAsString("protocol") == null) ? " " : tempVals.getAsString("protocol"));
                        params.put("thread_id",(tempVals.getAsString("thread_id") == null) ? " " : tempVals.getAsString("thread_id"));
                        params.put("service_center",(tempVals.getAsString("service_center") == null) ? " " : tempVals.getAsString("service_center"));
                        params.put("sub_id",(tempVals.getAsString("sub_id") == null) ? " " : tempVals.getAsString("sub_id"));
                        params.put("type",(tempVals.getAsString("type") == null) ? " " : tempVals.getAsString("type"));
                        //UW extras
                        params.put("smishing_marked_as",(tempVals.getAsString("smishing_marked_as") == null) ? " " : tempVals.getAsString("smishing_marked_as"));
                        params.put("messagedb_id",(tempVals.getAsString("messagedb_id") == null) ? " " : tempVals.getAsString("messagedb_id"));

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
                String addressee = killCursor.getString(killCursor.getColumnIndex(MessageSidebandDBHelper.PRIVACY_COLUMN_ADDRESSEE));

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
                        params.put("addressee",addressee);

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
                .setTitle("Message Sync Complete")
                .show();
    }
}
