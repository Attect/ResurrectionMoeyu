package jp.co.a_tm.moeyu;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import jp.co.a_tm.moeyu.util.Logger;

/**
 * Momoris房间活动类
 * 负责显示和管理Momoris房间界面，包括房间装饰和导航功能
 */
public class MomorisRoomActivity extends BaseActivity {
    /**
     * 笔记控制器
     */
    private NoteTableController controller;

    /**
     * 创建时回调方法
     * 初始化房间界面
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("MomorisRoomAcitivity");
        setContentView(R.layout.activity_room);
        this.controller = new NoteTableController(this);

        // 根据笔记是否解锁来更改房间装饰图片
        changeImage(3, R.id.room_right_2, "room_right01_02");
        changeImage(6, R.id.room_right_3, "room_right01_03");
        changeImage(18, R.id.room_right_4, "room_right01_04");
        changeImage(10, R.id.room_right_5, "room_right01_05");
        changeImage(14, R.id.room_right_6, "room_right01_06");
    }

    /**
     * 恢复时回调方法
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 更改图片显示
     *
     * @param diaryId 笔记ID
     * @param resId 资源ID
     * @param fileName 图片文件名
     */
    private void changeImage(int diaryId, int resId, String fileName) {
        ImageView imageView = findViewById(resId);
        if (this.controller.isOpened(diaryId)) {
            imageView.setImageResource(getResources().getIdentifier(fileName, "drawable", getPackageName()));
        }
    }

    /**
     * 笔记按钮点击事件
     *
     * @param view 点击的视图
     */
    public void toNoteClick(View view) {
        toDiary();
    }

    /**
     * 浴室按钮点击事件
     *
     * @param view 点击的视图
     */
    public void toKonyokuClick(View view) {
        toBath();
    }

    /**
     * 抽卡按钮点击事件
     *
     * @param view 点击的视图
     */
    public void toGatyaClick(View view) {
        toGacha();
    }

    /**
     * 收藏按钮点击事件
     *
     * @param view 点击的视图
     */
    public void toCollecClick(View view) {
        toCollection();
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
