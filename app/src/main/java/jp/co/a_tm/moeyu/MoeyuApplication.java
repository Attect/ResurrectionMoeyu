package jp.co.a_tm.moeyu;

import android.app.Application;

/**
 * Moeyu应用类
 * 负责管理应用的全局状态和生命周期
 */
public class MoeyuApplication extends Application {
    /**
     * 是否为首次运行
     */
    private boolean mFirstRun = true;

    /**
     * 设置首次运行标记
     *
     * @param firstRun 是否为首次运行
     */
    public void setFirstRun(boolean firstRun) {
        this.mFirstRun = firstRun;
    }

    /**
     * 判断是否为首次运行
     *
     * @return 是否为首次运行
     */
    public boolean isFirstRun() {
        return this.mFirstRun;
    }
}
