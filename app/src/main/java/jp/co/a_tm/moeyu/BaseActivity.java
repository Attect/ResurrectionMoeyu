package jp.co.a_tm.moeyu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import jp.co.a_tm.moeyu.model.EventData;

/**
 * 基础Activity类
 * 提供一些通用的跳转方法和基础功能
 *
 * @author Attect
 * @date 2019-03-16
 */
public abstract class BaseActivity extends AppCompatActivity {
    /**
     * 结束自身Activity通知上一层Activity进行的操作的Key
     */
    protected static final String EXTRA_NEXT_ACTIVITY = "extra_next_activity";

    /**
     * 新活动：洗澡
     */
    protected static final int NEXT_ACTIVITY_BATH = 2;

    /**
     * 新活动：收藏
     */
    protected static final int NEXT_ACTIVITY_COLLECTION = 6;

    /**
     * 新活动：秘密的日记
     */
    protected static final int NEXT_ACTIVITY_DIARY = 10;

    /**
     * 新活动：扭蛋
     */
    protected static final int NEXT_ACTIVITY_GACHA = 1;

    /**
     * 新活动：扭蛋结果
     */
    protected static final int NEXT_ACTIVITY_GACHA_RESULT = 5;

    /**
     * 新活动：道具收藏
     */
    protected static final int NEXT_ACTIVITY_ITEM_COLLECTION = 7;

    /**
     * 新活动：偏好设置
     */
    protected static final int NEXT_ACTIVITY_PREFERENCE = 3;

    /**
     * 新活动：偏好设置
     * 仅从洗澡活动使用
     */
    protected static final int NEXT_ACTIVITY_PREFERENCE_FROM_BATH = 4;

    /**
     * 新活动：桃璃的房间
     * 搓头的视角
     */
    protected static final int NEXT_ACTIVITY_ROOM = 9;

    /**
     * 新活动：标题界面
     */
    protected static final int NEXT_ACTIVITY_TITLE = 0;

    /**
     * 新活动
     * 对话语音收藏
     */
    protected static final int NEXT_ACTIVITY_VOICE_COLLECTION = 8;

    /**
     * 行为
     * 退出
     */
    protected static final int NEXT_EXIT = -1;

    /**
     * 创建时回调方法
     * 初始化Activity并隐藏系统UI
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();
    }

    /**
     * 暂停时回调方法
     * 释放资源
     */
    @Override
    protected void onPause() {
        super.onPause();
        release();
    }

    /**
     * 隐藏系统UI并设置全屏
     * 隐藏状态栏但保留底部导航栏
     */
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 释放资源
     * 子类可重写以释放特定资源（如Live2D资源）
     */
    protected void release() {
    }

    /**
     * 退出应用
     * 设置结果并结束活动
     */
    protected void exit() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_EXIT);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到标题界面
     */
    protected void toTitle() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_TITLE);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到扭蛋界面
     */
    protected void toGacha() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_GACHA);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到洗澡界面（无参数版本）
     */
    protected void toBath() {
        toBath(null);
    }

    /**
     * 跳转到洗澡界面
     *
     * @param scene  指定场景
     * @param itemId 默认选中道具
     */
    public void toBath(String scene, int itemId) {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_BATH);
        data.putExtra(getString(R.string.intent_scene), scene);
        data.putExtra(getString(R.string.intent_item), itemId);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到洗澡界面
     *
     * @param eventData 发生的事件
     */
    protected void toBath(EventData eventData) {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_BATH);
        if (eventData != null) {
            data.putExtra(getString(R.string.intent_event), eventData);
        }
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到设置界面
     */
    protected void toPreference() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_PREFERENCE);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 从洗澡Activity跳转到设置界面
     */
    protected void toPreferenceFromBath() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_PREFERENCE_FROM_BATH);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到收藏界面
     */
    protected void toCollection() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_COLLECTION);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到道具收藏界面
     */
    protected void toItemCollection() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_ITEM_COLLECTION);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到语音收藏界面
     */
    protected void toVoiceCollection() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_VOICE_COLLECTION);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到桃璃房间界面
     */
    protected void toRoom() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_ROOM);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到日记界面
     */
    protected void toDiary() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_DIARY);
        setResult(RESULT_OK, data);
        finish();
    }
}
