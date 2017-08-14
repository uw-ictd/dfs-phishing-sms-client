package com.moez.QKSMS;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class QKSMSApp extends QKSMSAppBase {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }


    }

    private class CrashReportingTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {

            if (priority == Log.WARN || priority == Log.ERROR) {
                Crashlytics.getInstance().core.log(priority, tag, message);
            }
            if (t != null) {
                Crashlytics.getInstance().core.logException(t);
            }
        }
    }
}
