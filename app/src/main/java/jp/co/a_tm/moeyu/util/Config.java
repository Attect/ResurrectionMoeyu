package jp.co.a_tm.moeyu.util;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

public class Config {
    private static Boolean isDebug = null;
    private static Boolean isProd = null;
    private static Boolean isStaging = null;
    private static Context mContext;
    private static Config mInstance;

    private Config() {
    }

    public static Config getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Config();
            mContext = context.getApplicationContext();
        }
        return mInstance;
    }

    public boolean isDebug() {
        if (isDebug != null) {
            return isDebug.booleanValue();
        }
        if (mContext == null) {
            throw new IllegalStateException("#init()でApplicationContextをセットしてください。");
        }
        try {
            Boolean valueOf;
            if ((mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), 0).flags & 2) != 0) {
                valueOf = Boolean.valueOf(true);
                isDebug = valueOf;
                return valueOf.booleanValue();
            }
            valueOf = Boolean.valueOf(false);
            isDebug = valueOf;
            return valueOf.booleanValue();
        } catch (NameNotFoundException e) {
            throw new IllegalStateException("ありえない");
        }
    }

    public boolean isStaging() {
        if (isStaging != null) {
            return isStaging.booleanValue();
        }
        boolean z = (isDebug() || isProd()) ? false : true;
        Boolean valueOf = Boolean.valueOf(z);
        isStaging = valueOf;
        return valueOf.booleanValue();
    }

    public boolean isProd() {
        if (isProd != null) {
            return isProd.booleanValue();
        }
        Boolean valueOf;
        if (mContext == null) {
            throw new IllegalStateException("#init()でApplicationContextをセットしてください。");
        } else if (mContext.getPackageManager().getInstallerPackageName(mContext.getPackageName()) == null) {
            valueOf = Boolean.valueOf(false);
            isProd = valueOf;
            return valueOf.booleanValue();
        } else {
            valueOf = Boolean.valueOf(true);
            isProd = valueOf;
            return valueOf.booleanValue();
        }
    }
}
