package jp.co.a_tm.moeyu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import jp.co.a_tm.moeyu.model.EventData;

/**
 * 基础Activity
 * 负责提供一些跳转方法
 *
 * @fixed Attect
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


    @Override
    protected void onPause() {
        super.onPause();
        release();
    }

    /**
     * Activity释放资源
     * 例如一些Live2D占用的资源
     */
    protected void release() {
    }

    /**
     * 退出
     */
    protected void exit() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_EXIT);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 前往标题
     */
    protected void toTitle() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_TITLE);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 前往扭蛋
     */
    protected void toGacha() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_GACHA);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 前往洗澡
     */
    protected void toBath() {
        toBath(null);
    }

    /**
     * 前往洗澡
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
     * 前往洗澡
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
     * 前往配置
     */
    protected void toPreference() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_PREFERENCE);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 从洗澡Activity前往配置
     */
    protected void toPreferenceFromBath() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_PREFERENCE_FROM_BATH);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 前往收藏
     */
    protected void toCollection() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_COLLECTION);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 前往道具收藏
     */
    protected void toItemCollection() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_ITEM_COLLECTION);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 前往语音收藏
     */
    protected void toVoiceCollection() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_VOICE_COLLECTION);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 前往桃璃的房间
     */
    protected void toRoom() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_ROOM);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 前往秘密的日记
     */
    protected void toDiary() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_DIARY);
        setResult(RESULT_OK, data);
        finish();
    }
}
