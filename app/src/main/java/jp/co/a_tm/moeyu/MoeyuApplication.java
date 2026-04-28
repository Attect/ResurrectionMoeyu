package jp.co.a_tm.moeyu;

import android.app.Application;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.co.a_tm.moeyu.util.Logger;

/**
 * Moeyu应用类
 * 负责管理应用的全局状态和生命周期
 */
public class MoeyuApplication extends Application {
    private static final String CRASH_LOG_FILE = "crash.log";
    /**
     * 是否为首次运行
     */
    private boolean mFirstRun = true;

    @Override
    public void onCreate() {
        super.onCreate();
        setupCrashHandler();
    }

    private void setupCrashHandler() {
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                try {
                    File crashFile = new File(getFilesDir(), CRASH_LOG_FILE);
                    FileWriter writer = new FileWriter(crashFile, true);
                    PrintWriter pw = new PrintWriter(writer);
                    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    pw.println("===== Crash at " + time + " =====");
                    pw.println("Thread: " + thread.getName());
                    ex.printStackTrace(pw);
                    pw.println();
                    pw.close();
                } catch (Exception e) {
                    Logger.e("MoeyuApplication", "Failed to write crash log", e);
                }
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(thread, ex);
                } else {
                    System.exit(1);
                }
            }
        });
    }

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
