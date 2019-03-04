package jp.co.a_tm.moeyu.util;

import android.util.Log;

public class Logger {
    private static final String TAG = "Moeyu";
    private static Config sConfig;

    public static void setConfig(Config config) {
        sConfig = config;
    }

    public static void v(String msg) {
        v(TAG, msg);
    }

    public static void v(String tag, String msg) {
        if (sConfig != null && !sConfig.isProd()) {
            Log.v(TAG, msg);
        }
    }

    public static void d(String msg) {
        d(TAG, msg);
    }

    public static void d(String tag, String msg) {
        if (sConfig != null && !sConfig.isProd()) {
            Log.d(TAG, msg);
        }
    }

    public static void i(String msg) {
        i(TAG, msg);
    }

    public static void i(String tag, String msg) {
        if (sConfig != null && !sConfig.isProd()) {
            Log.i(TAG, msg);
        }
    }

    public static void w(String msg) {
        w(TAG, msg);
    }

    public static void w(String tag, String msg) {
        if (sConfig != null && !sConfig.isProd()) {
            Log.w(TAG, msg);
        }
    }

    public static void e(String msg) {
        e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        if (sConfig != null && !sConfig.isProd()) {
            Log.e(TAG, msg);
        }
    }
}
