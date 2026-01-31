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
 * 提供一些通用的跳转方法和基础功能，包括系统UI隐藏、资源释放及Activity跳转功能。
 * 所有Activity应继承此类以获得统一的基础行为和跳转机制。
 *
 * @author Attect
 * @date 2019-03-16
 */
public abstract class BaseActivity extends AppCompatActivity {
    /**
     * 结束自身Activity通知上一层Activity进行的操作的Key
     * 用于在Activity结束时传递下一步要跳转的Activity类型
     */
    protected static final String EXTRA_NEXT_ACTIVITY = "extra_next_activity";

    /**
     * 新活动：洗澡
     * 用于标识跳转到洗澡界面的Activity类型
     */
    protected static final int NEXT_ACTIVITY_BATH = 2;

    /**
     * 新活动：收藏
     * 用于标识跳转到收藏界面的Activity类型
     */
    protected static final int NEXT_ACTIVITY_COLLECTION = 6;

    /**
     * 新活动：秘密的日记
     * 用于标识跳转到日记界面的Activity类型
     */
    protected static final int NEXT_ACTIVITY_DIARY = 10;

    /**
     * 新活动：扭蛋
     * 用于标识跳转到扭蛋界面的Activity类型
     */
    protected static final int NEXT_ACTIVITY_GACHA = 1;

    /**
     * 新活动：扭蛋结果
     * 用于标识跳转到扭蛋结果界面的Activity类型
     */
    protected static final int NEXT_ACTIVITY_GACHA_RESULT = 5;

    /**
     * 新活动：道具收藏
     * 用于标识跳转到道具收藏界面的Activity类型
     */
    protected static final int NEXT_ACTIVITY_ITEM_COLLECTION = 7;

    /**
     * 新活动：偏好设置
     * 用于标识跳转到偏好设置界面的Activity类型
     */
    protected static final int NEXT_ACTIVITY_PREFERENCE = 3;

    /**
     * 新活动：偏好设置
     * 仅从洗澡活动使用
     * 用于标识从洗澡活动跳转到偏好设置界面的Activity类型
     */
    protected static final int NEXT_ACTIVITY_PREFERENCE_FROM_BATH = 4;

    /**
     * 新活动：桃璃的房间
     * 搓头的视角
     * 用于标识跳转到桃璃房间界面的Activity类型
     */
    protected static final int NEXT_ACTIVITY_ROOM = 9;

    /**
     * 新活动：标题界面
     * 用于标识跳转到标题界面的Activity类型
     */
    protected static final int NEXT_ACTIVITY_TITLE = 0;

    /**
     * 新活动
     * 对话语音收藏
     * 用于标识跳转到语音收藏界面的Activity类型
     */
    protected static final int NEXT_ACTIVITY_VOICE_COLLECTION = 8;

    /**
     * 行为
     * 退出
     * 用于标识退出应用的Activity类型
     */
    protected static final int NEXT_EXIT = -1;

    /**
     * 创建时回调方法
     * 初始化Activity并隐藏系统UI
     * 该方法在Activity创建时调用，用于初始化基础设置并隐藏系统UI元素
     *
     * @param savedInstanceState 保存的实例状态，可用于恢复Activity状态
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();
    }

    /**
     * 暂停时回调方法
     * 释放资源
     * 该方法在Activity暂停时调用，用于释放可能占用的资源
     */
    @Override
    protected void onPause() {
        super.onPause();
        release();
    }

    /**
     * 隐藏系统UI并设置全屏
     * 隐藏状态栏但保留底部导航栏
     * 该方法设置Activity为全屏模式，隐藏状态栏以提供更好的沉浸式体验
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
     * 该方法用于释放Activity可能占用的资源，在onPause方法中被调用
     */
    protected void release() {
    }

    /**
     * 退出应用
     * 设置结果并结束活动
     * 该方法用于结束当前Activity并将退出操作通知上一层Activity
     */
    protected void exit() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_EXIT);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到标题界面
     * 该方法用于跳转到标题界面Activity
     */
    protected void toTitle() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_TITLE);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到扭蛋界面
     * 该方法用于跳转到扭蛋界面Activity
     */
    protected void toGacha() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_GACHA);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到洗澡界面（无参数版本）
     * 该方法用于跳转到洗澡界面Activity，不带额外参数
     */
    protected void toBath() {
        toBath(null);
    }

    /**
     * 跳转到洗澡界面
     *
     * @param scene  指定场景
     * @param itemId 默认选中道具
     * 该方法用于跳转到洗澡界面Activity，并可指定场景和默认选中的道具
     */
    protected void toBath(String scene, int itemId) {
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
     * 该方法用于跳转到洗澡界面Activity，并可传入事件数据
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
     * 跳转到指定Activity类型
     *
     * @param activityType 要跳转到的Activity类型
     */
    protected void toActivity(int activityType) {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, activityType);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到设置界面
     * 该方法用于跳转到设置界面Activity
     */
    protected void toPreference() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_PREFERENCE);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 从洗澡Activity跳转到设置界面
     * 该方法用于从洗澡Activity跳转到设置界面Activity
     */
    protected void toPreferenceFromBath() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_PREFERENCE_FROM_BATH);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到收藏界面
     * 该方法用于跳转到收藏界面Activity
     */
    protected void toCollection() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_COLLECTION);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到道具收藏界面
     * 该方法用于跳转到道具收藏界面Activity
     */
    protected void toItemCollection() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_ITEM_COLLECTION);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到语音收藏界面
     * 该方法用于跳转到语音收藏界面Activity
     */
    protected void toVoiceCollection() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_VOICE_COLLECTION);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到桃璃房间界面
     * 该方法用于跳转到桃璃房间界面Activity
     */
    protected void toRoom() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_ROOM);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 跳转到日记界面
     * 该方法用于跳转到日记界面Activity
     */
    protected void toDiary() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_DIARY);
        setResult(RESULT_OK, data);
        finish();
    }
}
