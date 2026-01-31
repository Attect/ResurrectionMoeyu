package jp.co.a_tm.moeyu;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

/**
 * 设置活动类
 * 负责显示和管理应用设置界面，包括相机设置等功能
 */
public class PreferenceActivity extends BaseActivity {
    /**
     * 首选项帮助器
     */
    /* access modifiers changed from: private */
    public PreferencesHelper mPreferencesHelper;

    /**
     * 创建时回调方法
     * 初始化设置界面
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        this.mPreferencesHelper = new PreferencesHelper(getApplicationContext());
        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggle_camera);
        toggleButton.setChecked(isEnableCamera(this));
        toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceActivity.this.mPreferencesHelper.setCameraSetting(isChecked);
            }
        });
    }

    /**
     * 恢复时回调方法
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onResume() {
        super.onResume();
        //        this.mTracker.trackPageView("設定");
    }

    /**
     * 判断是否启用相机
     *
     * @param activity 活动实例
     * @return 是否启用相机
     */
    public static boolean isEnableCamera(Activity activity) {
        return !activity.getIntent().hasExtra(activity.getString(R.string.intent_event)) && new PreferencesHelper(activity.getApplicationContext()).isCameraSetting();
    }

    /**
     * 键盘按下事件
     *
     * @param keyCode 按键代码
     * @param event   按键事件
     * @return 是否处理
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        onBackClick(null);
        return true;
    }

    /**
     * 返回按钮点击事件
     *
     * @param view 点击的视图
     */
    public void onBackClick(View view) {
        setResult(-1);
        finish();
    }
}
