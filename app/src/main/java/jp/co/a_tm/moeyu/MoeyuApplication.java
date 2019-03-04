package jp.co.a_tm.moeyu;

import android.app.Application;

public class MoeyuApplication extends Application {
    private boolean mFirstRun = true;

    public void setFirstRun(boolean firstRun) {
        this.mFirstRun = firstRun;
    }

    public boolean isFirstRun() {
        return this.mFirstRun;
    }
}
