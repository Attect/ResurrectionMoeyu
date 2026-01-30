package jp.co.a_tm.moeyu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.VideoView;

import java.io.Serializable;

import jp.co.a_tm.moeyu.util.Config;
import jp.co.a_tm.moeyu.util.Logger;
//import net.adways.appdriver.sdk.AppDriverTracker;

public class MainActivity extends BaseActivity {
    private static final int REQUEST_CODE_NAV = 1;
    private static final int REQUEST_CODE_PREFERENCE = 2;
    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean isFirstRun = true;

    /**
     * 修正非16:9屏幕比例
     */
    public static int FIX_HEIGHT = 0;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display display = displayManager.getDisplays()[0];
        Rect displaySize = new Rect();
        display.getRectSize(displaySize);
        FIX_HEIGHT = displaySize.height() - (displaySize.width() / 9 * 16);

        setContentView(R.layout.activity_main);
//        AppDriverTracker.requestAppDriver(this, 2654, "a0d6bbbe12c9063db2575ed63bafe95b");
        Logger.setConfig(Config.getInstance(getApplicationContext()));
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        if (this.isFirstRun) {
            this.isFirstRun = false;
            playOpeningMovie();
            ((MoeyuApplication) getApplication()).setFirstRun(true);
        }
    }

    private void playOpeningMovie() {
        VideoView videoView = (VideoView) findViewById(R.id.video);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ateam_moive));
        videoView.start();
        videoView.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                MainActivity.this.startTitleActivity();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        if (requestCode == 2) {
            startBathActivity(null);
        } else if (resultCode != -1 || data == null) {
            startTitleActivity();
        } else {
            switch (data.getIntExtra("extra_next_activity", -1)) {
                case -1:
                    finish();
                    return;
                case 0:
                    startTitleActivity();
                    return;
                case 1:
                    startGachaActivity();
                    return;
                case 2:
                    startBathActivity(data);
                    return;
                case 3:
                    startPreferenceActivity();
                    return;
                case 4:
                    startPreferenceActivityFromBath();
                    return;
                case 5:
                    startGachaResultActivity(data);
                    return;
                case 6:
                    startCollectionActivity();
                    return;
                case 7:
                    startItemCollectionActivity();
                    return;
                case 8:
                    startVoiceCollectionActivity();
                    return;
                case 9:
                    startRoomActivity();
                    return;
                case 10:
                    startNoteCollectionActivity();
                    return;
                default:
                    finish();
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public void startTitleActivity() {
        startActivityForResult(new Intent(this, TitleActivity.class), 1);
    }

    private void startBathActivity(Intent data) {
        Intent intent = new Intent(this, BathActivity.class);
        String name = getString(R.string.intent_event);
        if (data != null && data.hasExtra(name)) {
            Log.d(TAG, "has Event data!");
            intent.putExtra(name, data.getSerializableExtra(name));
        }
        String scene = getString(R.string.intent_scene);
        String itemId = getString(R.string.intent_item);
        if (data != null && data.hasExtra(scene) && data.hasExtra(itemId)) {
            intent.putExtra(scene, data.getStringExtra(scene));
            intent.putExtra(itemId, data.getIntExtra(itemId, 0));
        }
        startActivityForResult(intent, 1);
    }

    private void startGachaActivity() {
        startActivityForResult(new Intent(this, GatyaActivity.class), 1);
    }

    private void startPreferenceActivity() {
        startActivityForResult(new Intent(this, PreferenceActivity.class), 1);
    }

    private void startPreferenceActivityFromBath() {
        startActivityForResult(new Intent(this, PreferenceActivity.class), 2);
    }

    private void startGachaResultActivity(Intent data) {
        Serializable pre = data.getSerializableExtra(GatyaResultActivity.EXTRA_PRE_USER_DATA);
        Serializable result = data.getSerializableExtra(GatyaResultActivity.EXTRA_GACHA_RESULT);
        Intent intent = new Intent(this, GatyaResultActivity.class);
        intent.putExtra(GatyaResultActivity.EXTRA_PRE_USER_DATA, pre);
        intent.putExtra(GatyaResultActivity.EXTRA_GACHA_RESULT, result);
        startActivityForResult(intent, 1);
    }

    private void startCollectionActivity() {
        startActivityForResult(new Intent(this, CollectionRoomActivity.class), 1);
    }

    private void startItemCollectionActivity() {
        startActivityForResult(new Intent(this, ItemCollectionActivity.class), 1);
    }

    private void startVoiceCollectionActivity() {
        startActivityForResult(new Intent(this, VoiceCollectionActivity.class), 1);
    }

    private void startRoomActivity() {
        startActivityForResult(new Intent(this, MomorisRoomActivity.class), 1);
    }

    private void startNoteCollectionActivity() {
        startActivityForResult(new Intent(this, NoteCollectionActivity.class), 1);
    }
}
