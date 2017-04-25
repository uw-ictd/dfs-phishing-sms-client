package com.moez.QKSMS.data;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.sqlite.SQLiteDatabase;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.moez.QKSMS.BuildConfig;
import com.moez.QKSMS.R;
import com.moez.QKSMS.data.Conversation;
import com.moez.QKSMS.model.ChangeModel;
import com.moez.QKSMS.transaction.SmsHelper;
import com.moez.QKSMS.ui.MainActivity;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.dialog.DefaultSmsHelper;
import com.moez.QKSMS.ui.dialog.QKDialog;
import com.moez.QKSMS.ui.messagelist.MessageListActivity;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.*;
import java.net.URI;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.content.ContentValues;
import android.content.ContentResolver;
import android.net.Uri;
import com.google.gson.Gson;
import android.telephony.TelephonyManager;
import android.content.Context;
import com.moez.QKSMS.data.MessageSidebandDBHelper;

public class UWDataOffloadHelper {

    public final String URL_TO_UW_SERVER = "http://areaarea.pythonanywhere.com/";
    private final int CONNECTION_TIMEOUT = 1500;
    private final int DATARETRIEVAL_TIMEOUT = 1500;
    private int messages_sent;
    //private final String smsCols = { ""};

    private String UW_GET_UNSENT_RECORDS = MessageSidebandDBHelper.COLUMN_SENT_TO_UW + " = 0";
    private MessageSidebandDBHelper messageDB = null;
    private SQLiteDatabase myDb;

    public void startOffload(QKActivity context) {

        if(messageDB == null) {
            messageDB = new MessageSidebandDBHelper(context.getApplicationContext());
        }

        String url = "http://areaarea.pythonanywhere.com/add/";

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
                Uri oneMessage = Uri.parse(myCursor.getString(myCursor.getColumnIndex(MessageSidebandDBHelper.COLUMN_MESSAGEDB_ID)));
                Cursor smsCursor = smsResolver.query(oneMessage,null,null,null,null);
                if(smsCursor.moveToFirst()) {
                    DatabaseUtils.cursorRowToContentValues(smsCursor,tempVals);
                }

                StringRequest request = new StringRequest(Request.Method.POST,url, response -> {

                    //TODO Bookkeeping
                    new QKDialog()
                            .setContext(context)
                            .setTitle(response)
                            .show();

                    //add_message();

                }, error -> {
                        context.makeToast(R.string.toast_changelog_error);
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

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
                        return params;
                    }
                };

                context.getRequestQueue().add(request);
            } while(myCursor.moveToNext());
        }

        myCursor.close();



       // new QKDialog()
       //         .setContext(context)
       //         .setTitle("There were " + messages_sent + " messages sent")
       //         .show();

    }

    private void  add_message() {
        messages_sent++;
    }
    //args passed are POST args
    public String makeRequest(String args) {

        HttpURLConnection urlConnection = null;
        try {
            // create connection
            URL urlToRequest = new URL(URL_TO_UW_SERVER);
            urlConnection = (HttpURLConnection) urlToRequest.openConnection();
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(DATARETRIEVAL_TIMEOUT);

            // handle POST parameters
            if (args != null) {

                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setFixedLengthStreamingMode(
                        args.getBytes().length);
                urlConnection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                //send the POST out
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(args);
                out.close();
            }

            // handle issues
           // int statusCode = urlConnection.getResponseCode();
           // if (statusCode != HttpURLConnection.HTTP_OK) {
                // throw some exception
           // }

            // read output (only for GET)
            if (args != null) {
                return null;
            } else {
                InputStream in =
                        new BufferedInputStream(urlConnection.getInputStream());
                return in.toString();
            }


        } catch (MalformedURLException e) {
            // handle invalid URL
        } catch (SocketTimeoutException e) {
            // hadle timeout
        } catch (IOException e) {
            // handle I/0
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }
}