package com.moez.QKSMS.data;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.sqlite.SQLiteDatabase;

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

import com.moez.QKSMS.data.MessageSidebandDBHelper;

public class UWDataOffloadHelper {

    public final String URL_TO_UW_SERVER = "http://areaarea.pythonanywhere.com/";
    private final int CONNECTION_TIMEOUT = 1500;
    private final int DATARETRIEVAL_TIMEOUT = 1500;

    //private final String smsCols = { ""};

    private String UW_GET_UNSENT_RECORDS = MessageSidebandDBHelper.COLUMN_SENT_TO_UW + " = 0";
    private MessageSidebandDBHelper messageDB = null;
    private SQLiteDatabase myDb;

    public void startOffload(QKActivity context) {

        if(messageDB == null) {
            messageDB = new MessageSidebandDBHelper(context.getApplicationContext());
        }

        myDb = messageDB.getReadableDatabase();
        Cursor myCursor = myDb.query(MessageSidebandDBHelper.TABLE_NAME_SIDEBANDDB, null, UW_GET_UNSENT_RECORDS,null,null,null,null );

        ContentResolver smsResolver = context.getContentResolver();

        ArrayList<ContentValues> valList = new ArrayList<ContentValues>();
        ContentValues tempVals;
        ContentValues tempValsSmsDb;
        if(myCursor.moveToFirst()) {
            do {
                tempVals = new ContentValues();
                tempValsSmsDb = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(myCursor, tempVals);
                //valList.add(tempVals);
                Uri oneMessage = Uri.parse(myCursor.getString(myCursor.getColumnIndex(MessageSidebandDBHelper.COLUMN_MESSAGEDB_ID)));
                Cursor smsCursor = smsResolver.query(oneMessage,null,null,null,null);
                if(smsCursor.moveToFirst()) {
                    DatabaseUtils.cursorRowToContentValues(smsCursor,tempVals);
                    valList.add(tempVals);
                }

            } while(myCursor.moveToNext());
        }

        myCursor.close();

        String url = "http://areaarea.pythonanywhere.com/";
        StringRequest request = new StringRequest(url, response -> {

            new QKDialog()
                    .setContext(context)
                    .setTitle(response)
                    .show();
        }, error -> {
            context.makeToast(R.string.toast_changelog_error);
        });
        context.getRequestQueue().add(request);


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
