package com.moez.QKSMS.data;


import android.content.ContentValues;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Field variables in this class should match with the SmsInfo model on the backend.
 */
public class SMSInfo {

    // SMS/MMS fields on the server. Should match with the fields in SmsInfo Model on the backend
    private String creator;
    private String date;
    private String date_sent;
    private String read;
    private String subject;
    private String status;
    private String thread_id;
    private String subscription_id;
    private String longitude;
    private String latitude;
    private String network_state;
    private String uw_smishing_marked_as;
    private String uw_messagedb_id;
    private boolean is_mms;
    private String sms_body;
    private String sms_error_code;
    private String sms_person;
    private String sms_protocol;
    private String sms_reply_path_present;
    private String sms_service_center;
    private String sms_is_email;
    private String sms_email_from;
    private String sms_email_body;
    private String sms_origin_address;
    private String sms_address;
    private String sms_message_type;
    private String mms_text_only;
    private String all_recipients;
    private String mms_address;
    private String mms_message_type;


    // SMS/MMS fields in phone database
    // https://developer.android.com/reference/android/provider/Telephony.TextBasedSmsColumns.html

    //------- Fields common to SMS and MMS.
    // Android content provider.
    private static final String CREATOR = "creator";
    private static final String DATE = "date";
    private static final String DATE_SENT = "date_sent";
    private static final String READ = "read";
    private static final String SUBJECT = "subject";
    private static final String STATUS = "status";
    private static final String THREAD_ID = "thread_id";
    private static final String SUBSCRIPTION_ID = "subscription_id";

    // UW Sideband DB extras.
    private static final String SMISHING_MARKED_AS = "smishing_marked_as";
    private static final String MESSAGEDB_ID = "messagedb_id";
    // TODO: Add network state and location columns to SidebandDB.
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String NETWORK_STATE = "network_state";

    //------- SMS specific fields
    // Android content provider.
    private static final String ADDRESS = "address";
    private static final String BODY = "body";
    private static final String ERROR_CODE = "error_code";
    private static final String PERSON = "person";
    private static final String PROTOCOL = "protocol";
    private static final String REPLY_PATH_PRESENT = "reply_path_present";
    private static final String SERVICE_CENTER = "service_center";
    private static final String TYPE = "type";

    // UW Sideband DB extras.
    private static final String IS_EMAIL = "is_email";
    private static final String EMAIL_FROM = "email_from";
    private static final String EMAIL_BODY = "email_body";
    private static final String ORIGIN_ADDRESS = "origin_address";

    //------- MMS specific fields
    // Android content provider.
    // TODO: body?
    private static final String MESSAGE_TYPE = "message_type";
    private static final String TEXT_ONLY = "text_only";

    // UW Sideband DB extras.
    // Addressee is is the address of the sender. This is specific to MMS
    // because SMS equivalent is pulled from the Android content provider.
    // It's called addressee so it doesn't interfere with Android content.
    private static final String MMS_ADDRESS = "addressee";
    // TODO: Get full recipients list from MMS.
    // private static final String all_recipients = ?;

    public SMSInfo(String userID, ContentValues cv, Uri message) {
        sms_address = cv.getAsString(ADDRESS);
        uw_messagedb_id = cv.getAsString(MESSAGEDB_ID);

        creator = cv.getAsString(CREATOR);
        date = cv.getAsString(DATE);
        date_sent = cv.getAsString(DATE_SENT);
        read = cv.getAsString(READ);
        subject = cv.getAsString(SUBJECT);
        status = cv.getAsString(STATUS);
        thread_id = cv.getAsString(THREAD_ID);
        subscription_id = cv.getAsString(SUBSCRIPTION_ID);
        uw_smishing_marked_as = cv.getAsString(SMISHING_MARKED_AS);
        longitude = cv.getAsString(LONGITUDE);
        latitude = cv.getAsString(LATITUDE);
        network_state = cv.getAsString(NETWORK_STATE);

        if (message.toString().contains("sms")) {
            sms_body = cv.getAsString(BODY);
            sms_error_code = cv.getAsString(ERROR_CODE);
            sms_person = cv.getAsString(PERSON);
            sms_protocol = cv.getAsString(PROTOCOL);
            sms_reply_path_present = cv.getAsString(REPLY_PATH_PRESENT);
            sms_service_center = cv.getAsString(SERVICE_CENTER);
            sms_is_email = cv.getAsString(IS_EMAIL);
            sms_email_from = cv.getAsString(EMAIL_FROM);
            sms_email_body = cv.getAsString(EMAIL_BODY);
            sms_origin_address = cv.getAsString(ORIGIN_ADDRESS);
            sms_message_type = cv.getAsString(TYPE);
        }

        is_mms = message.toString().contains("mms");
        if (is_mms) {
            mms_text_only = cv.getAsString(TEXT_ONLY);
            mms_address = cv.getAsString(MMS_ADDRESS);
            mms_message_type = cv.getAsString(MESSAGE_TYPE);
        }
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public JsonElement toJson() {
        Gson gson = new Gson();
        return gson.toJsonTree(this);
    }
}
