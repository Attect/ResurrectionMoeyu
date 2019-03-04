package jp.co.a_tm.moeyu;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class PreferenceActivity extends BaseActivity {
    /* access modifiers changed from: private */
    public PreferencesHelper mPreferencesHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        this.mPreferencesHelper = new PreferencesHelper(getApplicationContext());
        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggle_camera);
        toggleButton.setChecked(isEnableCamera(this));
        toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceActivity.this.mPreferencesHelper.setCameraSetting(isChecked);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
//        this.mTracker.trackPageView("設定");
    }

    public static boolean isEnableCamera(Activity activity) {
        return !activity.getIntent().hasExtra(activity.getString(R.string.intent_event)) && new PreferencesHelper(activity.getApplicationContext()).isCameraSetting();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        onBackClick(null);
        return true;
    }

    public void onBackClick(View view) {
        setResult(-1);
        finish();
    }
}
