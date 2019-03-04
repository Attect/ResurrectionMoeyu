package jp.co.a_tm.moeyu;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferencesHelper {
    private SharedPreferences sp;

    public PreferencesHelper(Context context) {
        this.sp = context.getSharedPreferences("data", 0);
    }

    public boolean isInitBoot() {
        return this.sp.getBoolean("InitBoot", true);
    }

    public void setInitBoot(boolean bool) {
        Editor editor = this.sp.edit();
        editor.putBoolean("InitBoot", bool);
        editor.commit();
    }

    public boolean isInitGatya() {
        return this.sp.getBoolean("InitGatya", true);
    }

    public boolean isInitBath() {
        return this.sp.getBoolean("InitBath", true);
    }

    public void setInitBath(boolean bool) {
        Editor editor = this.sp.edit();
        editor.putBoolean("InitBath", bool);
        editor.commit();
    }

    public void setInitGatya(boolean bool) {
        Editor editor = this.sp.edit();
        editor.putBoolean("InitGatya", bool);
        editor.commit();
    }

    public boolean isInitGatyaResult() {
        return this.sp.getBoolean("InitGatyaResult", true);
    }

    public void setInitGatyaResult(boolean bool) {
        Editor editor = this.sp.edit();
        editor.putBoolean("InitGatyaResult", bool);
        editor.commit();
    }

    public boolean isInitLevelUp() {
        return this.sp.getBoolean("InitLevelUp", true);
    }

    public void setInitLevelUp(boolean bool) {
        Editor editor = this.sp.edit();
        editor.putBoolean("InitLevelUp", bool);
        editor.commit();
    }

    public boolean isCameraSetting() {
        return this.sp.getBoolean("Camera", false);
    }

    public void setCameraSetting(boolean bool) {
        Editor editor = this.sp.edit();
        editor.putBoolean("Camera", bool);
        editor.commit();
    }

    public boolean isInitTweet() {
        return this.sp.getBoolean("InitTweet", true);
    }

    public void setInitTweet(boolean bool) {
        Editor editor = this.sp.edit();
        editor.putBoolean("InitTweet", bool);
        editor.commit();
    }

    public boolean isTwitterCheck() {
        return this.sp.getBoolean("Twitter", true);
    }

    public void setTwitterCheck(boolean bool) {
        Editor editor = this.sp.edit();
        editor.putBoolean("Twitter", bool);
        editor.commit();
    }

    public String getAppVersion() {
        return this.sp.getString("AppVersion", "1.0.0");
    }

    public void setAppVersion(String version) {
        Editor editor = this.sp.edit();
        editor.putString("AppVersion", version);
        editor.commit();
    }
}
