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

/**
 * 主活动类 - 应用程序的入口点
 * 负责启动应用、播放片头动画、处理页面跳转等
 */
public class MainActivity extends BaseActivity {
    /**
     * 页面跳转请求码 - 导航页面
     */
    private static final int REQUEST_CODE_NAV = 1;
    /** 页面跳转请求码 - 设置页面 */
    private static final int REQUEST_CODE_PREFERENCE = 2;
    /** 日志标签 */
    private static final String TAG = MainActivity.class.getSimpleName();
    /** 是否为首次运行标记 */
    private boolean isFirstRun = true;

    /**
     * 修正非16:9屏幕比例的值
     * 用于适配不同设备屏幕比例
     */
    public static int FIX_HEIGHT = 0;

    /**
     * 创建时的回调方法
     * 初始化屏幕尺寸计算和设置内容视图
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取屏幕尺寸并计算修正值
        calculateScreenFixValue();
        // 设置布局
        setContentView(R.layout.activity_main);
        // 设置日志配置
        Logger.setConfig(Config.getInstance(getApplicationContext()));
    }

    /**
     * 恢复时的回调方法
     * 首次运行时播放片头动画
     */
    @Override
    protected void onResume() {
        super.onResume();
        // 首次运行时播放片头动画
        if (this.isFirstRun) {
            this.isFirstRun = false;
            playOpeningMovie();
            // 设置首次运行标记
            ((MoeyuApplication) getApplication()).setFirstRun(true);
        }
    }

    /**
     * 计算屏幕修正值
     * 根据屏幕尺寸计算适配值，用于处理非16:9屏幕比例
     */
    private void calculateScreenFixValue() {
        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display display = displayManager.getDisplays()[0];
        Rect displaySize = new Rect();
        display.getRectSize(displaySize);
        FIX_HEIGHT = displaySize.height() - (displaySize.width() / 9 * 16);
    }

    /**
     * 播放片头动画
     * 播放指定的视频文件，播放完成后跳转到标题页
     */
    private void playOpeningMovie() {
        VideoView videoView = findViewById(R.id.video);
        // 设置视频路径
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ateam_moive));
        // 开始播放
        videoView.start();
        // 设置播放完成监听器
        videoView.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // 播放完成后跳转到标题页
                startTitleActivity();
            }
        });
    }

    /**
     * 处理页面返回结果
     * 根据返回的意图数据跳转到相应的页面
     *
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        返回的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");

        // 如果是设置页面返回的结果
        if (requestCode == REQUEST_CODE_PREFERENCE) {
            startBathActivity(null);
        } else if (resultCode != -1 || data == null) {
            // 页面返回结果不正确则跳转到标题页
            startTitleActivity();
        } else {
            // 获取跳转到的页面类型
            int nextPageType = data.getIntExtra(BaseActivity.EXTRA_NEXT_ACTIVITY, -1);
            // 根据类型跳转到对应的页面
            switch (nextPageType) {
                case -1: // 结束应用
                    finish();
                    break;
                case 0: // 标题页
                    startTitleActivity();
                    break;
                case 1: // 抽卡页
                    startGachaActivity();
                    break;
                case 2: // 浴室页
                    startBathActivity(data);
                    break;
                case 3: // 设置页
                    startPreferenceActivity();
                    break;
                case 4: // 从浴室页来的设置页
                    startPreferenceActivityFromBath();
                    break;
                case 5: // 抽卡结果页
                    startGachaResultActivity(data);
                    break;
                case 6: // 收藏房间页
                    startCollectionActivity();
                    break;
                case 7: // 物品收藏页
                    startItemCollectionActivity();
                    break;
                case 8: // 语音收藏页
                    startVoiceCollectionActivity();
                    break;
                case 9: // 房间页
                    startRoomActivity();
                    break;
                case 10: // 笔记收藏页
                    startNoteCollectionActivity();
                    break;
                default: // 默认返回
                    finish();
                    break;
            }
        }
    }

    /**
     * 启动标题页
     * 创建并启动标题活动
     */
    private void startTitleActivity() {
        startActivityForResult(new Intent(this, TitleActivity.class), REQUEST_CODE_NAV);
    }

    /**
     * 启动浴室页
     * 创建并启动浴室活动，传递数据
     *
     * @param data 传递的数据
     */
    private void startBathActivity(Intent data) {
        Intent intent = new Intent(this, BathActivity.class);
        String eventName = getString(R.string.intent_event);
        // 如果数据包含事件信息，传递该信息
        if (data != null && data.hasExtra(eventName)) {
            Log.d(TAG, "has Event data!");
            intent.putExtra(eventName, data.getSerializableExtra(eventName));
        }
        String scene = getString(R.string.intent_scene);
        String itemId = getString(R.string.intent_item);
        // 如果数据包含场景和物品ID，传递这些信息
        if (data != null && data.hasExtra(scene) && data.hasExtra(itemId)) {
            intent.putExtra(scene, data.getStringExtra(scene));
            intent.putExtra(itemId, data.getIntExtra(itemId, 0));
        }
        startActivityForResult(intent, REQUEST_CODE_NAV);
    }

    /**
     * 启动抽卡页
     * 创建并启动抽卡活动
     */
    private void startGachaActivity() {
        startActivityForResult(new Intent(this, GatyaActivity.class), REQUEST_CODE_NAV);
    }

    /**
     * 启动设置页
     * 创建并启动设置活动
     */
    private void startPreferenceActivity() {
        startActivityForResult(new Intent(this, PreferenceActivity.class), REQUEST_CODE_NAV);
    }

    /**
     * 从浴室页启动设置页
     * 创建并启动设置活动，使用特殊请求码
     */
    private void startPreferenceActivityFromBath() {
        startActivityForResult(new Intent(this, PreferenceActivity.class), REQUEST_CODE_PREFERENCE);
    }

    /**
     * 启动抽卡结果页
     * 创建并启动抽卡结果活动，传递前后数据
     *
     * @param data 抽卡结果数据
     */
    private void startGachaResultActivity(Intent data) {
        Serializable pre = data.getSerializableExtra(GatyaResultActivity.EXTRA_PRE_USER_DATA);
        Serializable result = data.getSerializableExtra(GatyaResultActivity.EXTRA_GACHA_RESULT);
        Intent intent = new Intent(this, GatyaResultActivity.class);
        intent.putExtra(GatyaResultActivity.EXTRA_PRE_USER_DATA, pre);
        intent.putExtra(GatyaResultActivity.EXTRA_GACHA_RESULT, result);
        startActivityForResult(intent, REQUEST_CODE_NAV);
    }

    /**
     * 启动收藏房间页
     * 创建并启动收藏房间活动
     */
    private void startCollectionActivity() {
        startActivityForResult(new Intent(this, CollectionRoomActivity.class), REQUEST_CODE_NAV);
    }

    /**
     * 启动物品收藏页
     * 创建并启动物品收藏活动
     */
    private void startItemCollectionActivity() {
        startActivityForResult(new Intent(this, ItemCollectionActivity.class), REQUEST_CODE_NAV);
    }

    /**
     * 启动语音收藏页
     * 创建并启动语音收藏活动
     */
    private void startVoiceCollectionActivity() {
        startActivityForResult(new Intent(this, VoiceCollectionActivity.class), REQUEST_CODE_NAV);
    }

    /**
     * 启动房间页
     * 创建并启动房间活动
     */
    private void startRoomActivity() {
        startActivityForResult(new Intent(this, MomorisRoomActivity.class), REQUEST_CODE_NAV);
    }

    /**
     * 启动笔记收藏页
     * 创建并启动笔记收藏活动
     */
    private void startNoteCollectionActivity() {
        startActivityForResult(new Intent(this, NoteCollectionActivity.class), REQUEST_CODE_NAV);
    }
}
