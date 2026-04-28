package jp.co.a_tm.moeyu;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * 设置活动类
 * 负责显示和管理应用设置界面，包括相机设置等功能
 */
public class PreferenceActivity extends BaseActivity {
    /**
     * 首选项帮助器
     */
    public PreferencesHelper mPreferencesHelper;

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private ToggleButton mToggleCamera;

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
        mToggleCamera = findViewById(R.id.toggle_camera);
        mToggleCamera.setChecked(isEnableCamera(this));
        mToggleCamera.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ContextCompat.checkSelfPermission(PreferenceActivity.this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(PreferenceActivity.this,
                                new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    } else {
                        PreferenceActivity.this.mPreferencesHelper.setCameraSetting(true);
                    }
                } else {
                    PreferenceActivity.this.mPreferencesHelper.setCameraSetting(false);
                }
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
        if (keyCode != KeyEvent.KEYCODE_BACK) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mPreferencesHelper.setCameraSetting(true);
                Toast.makeText(this, "相机权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                mToggleCamera.setChecked(false);
                mPreferencesHelper.setCameraSetting(false);
                Toast.makeText(this, "需要相机权限才能使用AR模式", Toast.LENGTH_LONG).show();
            }
        }
    }
}
