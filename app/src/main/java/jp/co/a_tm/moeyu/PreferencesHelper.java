package jp.co.a_tm.moeyu;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferencesHelper {
    private SharedPreferences mPreferences;

    public PreferencesHelper(Context context) {
        this.mPreferences = context.getSharedPreferences("data", 0);
    }

    public boolean isInitBoot() {
        return this.mPreferences.getBoolean("InitBoot", true);
    }

    public void setInitBoot(boolean bool) {
        Editor editor = this.mPreferences.edit();
        editor.putBoolean("InitBoot", bool);
        editor.apply();
    }

    public boolean isInitGatya() {
        return this.mPreferences.getBoolean("InitGatya", true);
    }

    public boolean isInitBath() {
        return this.mPreferences.getBoolean("InitBath", true);
    }

    public void setInitBath(boolean bool) {
        Editor editor = this.mPreferences.edit();
        editor.putBoolean("InitBath", bool);
        editor.apply();
    }

    public void setInitGatya(boolean bool) {
        Editor editor = this.mPreferences.edit();
        editor.putBoolean("InitGatya", bool);
        editor.apply();
    }

    public boolean isInitGatyaResult() {
        return this.mPreferences.getBoolean("InitGatyaResult", true);
    }

    public void setInitGatyaResult(boolean bool) {
        Editor editor = this.mPreferences.edit();
        editor.putBoolean("InitGatyaResult", bool);
        editor.apply();
    }

    public boolean isInitLevelUp() {
        return this.mPreferences.getBoolean("InitLevelUp", true);
    }

    public void setInitLevelUp(boolean bool) {
        Editor editor = this.mPreferences.edit();
        editor.putBoolean("InitLevelUp", bool);
        editor.apply();
    }

    public boolean isCameraSetting() {
        return this.mPreferences.getBoolean("Camera", false);
    }

    public void setCameraSetting(boolean bool) {
        Editor editor = this.mPreferences.edit();
        editor.putBoolean("Camera", bool);
        editor.apply();
    }

    public boolean isInitTweet() {
        return this.mPreferences.getBoolean("InitTweet", true);
    }

    public void setInitTweet(boolean bool) {
        Editor editor = this.mPreferences.edit();
        editor.putBoolean("InitTweet", bool);
        editor.apply();
    }

    public boolean isTwitterCheck() {
        return this.mPreferences.getBoolean("Twitter", true);
    }

    public void setTwitterCheck(boolean bool) {
        Editor editor = this.mPreferences.edit();
        editor.putBoolean("Twitter", bool);
        editor.apply();
    }

    public String getAppVersion() {
        return this.mPreferences.getString("AppVersion", "1.0.0");
    }

    public void setAppVersion(String version) {
        Editor editor = this.mPreferences.edit();
        editor.putString("AppVersion", version);
        editor.apply();
    }
}
