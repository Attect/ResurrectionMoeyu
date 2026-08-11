package jp.co.a_tm.moeyu;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import jp.co.a_tm.moeyu.util.AspectRatioUtils;

/**
 * 收藏房间活动类
 * 负责显示收藏房间界面，提供导航到其他收藏页面的功能
 */
public class CollectionRoomActivity extends BaseActivity {
    /**
     * 创建时回调方法
     * 初始化收藏房间界面
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collectionroom);
        // 非16:9屏幕适配：内容限制为9:16居中显示，黑边填充
        AspectRatioUtils.applyToContent(this);
        findViewById(R.id.layout_collecroom).setBackgroundColor(Color.BLACK);
    }

    /**
     * 恢复时回调方法
     * 活动恢复时执行的操作
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 物品收藏按钮点击事件
     *
     * @param view 点击的视图
     */
    public void toItemCollectionClick(View view) {
        toItemCollection();
    }

    /**
     * 语音收藏按钮点击事件
     *
     * @param view 点击的视图
     */
    public void toVoiceCollectionClick(View view) {
        toVoiceCollection();
    }

    /**
     * 房间按钮点击事件
     *
     * @param view 点击的视图
     */
    public void toMomorisRoomClick(View view) {
        toRoom();
    }

    /**
     * 标题按钮点击事件
     *
     * @param view 点击的视图
     */
    public void toTitleClick(View view) {
        finish();
    }
}
