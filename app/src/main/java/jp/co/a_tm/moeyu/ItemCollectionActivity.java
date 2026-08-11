package jp.co.a_tm.moeyu;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jp.co.a_tm.moeyu.util.AspectRatioUtils;
import jp.co.a_tm.moeyu.util.Logger;

/**
 * 物品收藏活动类
 * 负责显示和管理用户收集的物品内容
 */
public class ItemCollectionActivity extends BaseActivity {
    /**
     * 场景映射数组
     * 数组索引对应物品ID(0=占位), 值为该物品使用的场景
     * 索引1~25分别对应物品01~25
     */
    private static Scene[] sScenes = new Scene[]{null, Scene.bath_a, Scene.body, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.body, Scene.head, Scene.bath_a, Scene.body, Scene.bath_a, Scene.body, Scene.bath_a, Scene.bath_a};
    /** 最大行数 */
    private final int MAX_ROW = 25;

    /**
     * 物品列表适配器类
     * 负责创建和管理物品列表项视图
     */
    private class ItemListAdapter extends ArrayAdapter<ItemListItem> {
        /** 布局加载器 */
        private LayoutInflater mInflater;

        /**
         * 构造函数
         *
         * @param context 上下文
         * @param list 物品列表
         */
        public ItemListAdapter(Context context, List<ItemListItem> list) {
            super(context, 0, list);
            this.mInflater = LayoutInflater.from(context);
        }

        /**
         * 获取列表项视图
         *
         * @param position    位置
         * @param convertView 可复用的视图
         * @param parent      父视图
         * @return 列表项视图
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = this.mInflater.inflate(R.layout.list_item, null);
            ItemListItem item = (ItemListItem) getItem(position);
            // 设置按钮（图像）显示
            for (int i = 1; i <= item.getButtonCount(); i++) {
                if (item.getItemNumber(i) == -1) {
                    hideImageButton(view, i);
                } else {
                    setImageButton(view, i, item);
                }
            }
            return view;
        }

        /**
         * 隐藏按钮
         *
         * @param view 视图
         * @param buttonNumber 按钮编号
         */
        private void hideImageButton(View view, int buttonNumber) {
            ((ImageButton) view.findViewById(getResID("imgbutton_listitem_item" + buttonNumber, "id"))).setVisibility(View.INVISIBLE);
        }

        /**
         * 设置按钮图像
         *
         * @param view 视图
         * @param buttonNumber 按钮编号
         * @param item 物品项
         */
        private void setImageButton(View view, final int buttonNumber, final ItemListItem item) {
            ImageButton imgButton = (ImageButton) view.findViewById(getResID("imgbutton_listitem_item" + buttonNumber, "id"));
            if (item.getOpened(buttonNumber)) {
                imgButton.setImageResource(getResID(String.format("item%02d", new Object[]{Integer.valueOf(item.getItemNumber(buttonNumber))}), "drawable"));
            } else {
                imgButton.setImageResource(getResID(String.format("gray_item%02d", new Object[]{Integer.valueOf(item.getItemNumber(buttonNumber))}), "drawable"));
            }
            imgButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Logger.d("Click item=" + item.getItemNumber(buttonNumber));
                    final Dialog dialog = new Dialog(ItemCollectionActivity.this, R.style.clear_dialog);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dialog_itemcollec);
                    dialog.setCanceledOnTouchOutside(true);
                    ItemListAdapter.this.setDialogImageView(dialog, item, buttonNumber);
                    // 取消按钮点击事件
                    ((ImageButton) dialog.findViewById(R.id.imgbutton_itemcollec_dialog_cancel)).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    // 浴室按钮点击事件
                    dialog.findViewById(R.id.imgbutton_itemcollec_dialog_konyoku).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            if (item.getOpened(buttonNumber)) {
                                ItemCollectionActivity.this.startBathActivity(item.getItemNumber(buttonNumber));
                            }
                        }
                    });
                    dialog.show();
                }
            });
        }

        /**
         * 设置对话框图片显示
         *
         * @param dialog 对话框
         * @param item 物品项
         * @param buttonNumber 按钮编号
         */
        public void setDialogImageView(Dialog dialog, ItemListItem item, int buttonNumber) {
            ImageView imgName = (ImageView) dialog.findViewById(R.id.img_itemcollec_dialog_itemname);
            ImageView imgPict = (ImageView) dialog.findViewById(R.id.img_itemcollec_dialog_itempicture);
            ImageView imgComment = (ImageView) dialog.findViewById(R.id.img_itemcollec_dialog_itemcomment);
            if (item.getOpened(buttonNumber)) {
                imgName.setImageResource(getResID("itemname_bar_" + String.format("%02d", new Object[]{Integer.valueOf(item.getItemNumber(buttonNumber))}), "drawable"));
                imgPict.setImageResource(getResID("item" + String.format("%02d", new Object[]{Integer.valueOf(item.getItemNumber(buttonNumber))}) + "_2x", "drawable"));
                imgComment.setImageResource(getResID("item_comment" + String.format("%02d", new Object[]{Integer.valueOf(item.getItemNumber(buttonNumber))}), "drawable"));
                return;
            }
            imgName.setImageResource(getResID("itemname_bar_" + String.format("%02d", new Object[]{Integer.valueOf(item.getItemNumber(buttonNumber))}) + "glay", "drawable"));
            imgPict.setImageResource(getResID("gray_item" + String.format("%02d", new Object[]{Integer.valueOf(item.getItemNumber(buttonNumber))}), "drawable"));
            imgComment.setImageResource(getResID("item_comment00", "drawable"));
        }

        /**
         * 获取资源ID
         *
         * @param strName 资源名称
         * @param strRes 资源类型
         * @return 资源ID
         */
        private int getResID(String strName, String strRes) {
            return getContext().getResources().getIdentifier(strName, strRes, ItemCollectionActivity.this.getPackageName());
        }
    }

    /**
     * 创建时回调方法
     * 初始化物品收藏界面
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemcollection);

        // 非16:9屏幕适配：内容限制为9:16居中显示，黑边填充
        AspectRatioUtils.applyToContent(this);
        findViewById(R.id.layout_itemcollec).setBackgroundColor(Color.BLACK);

        // 初始化物品控制器并创建物品列表
        ItemTableController itemController = new ItemTableController(getApplicationContext());
        List<ItemListItem> list = new ArrayList<>();
        list.add(new ItemListItem(new int[]{-1, 1, -1}, itemController));
        for (int i = 2; i <= itemController.countRows(); i += 3) {
            list.add(new ItemListItem(new int[]{i, i + 1, i + 2}, itemController));
        }

        // 设置适配器并更新统计信息
        ((ListView) findViewById(R.id.listview_itemcollec)).setAdapter(new ItemListAdapter(this, list));
        ((TextView) findViewById(R.id.textview_itemcollec_getstate)).setText(itemController.countOpened() + "/" + 25);
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
     * 抽卡按钮点击事件
     *
     * @param view 点击的视图
     */
    public void toGatyaClick(View view) {
        toGacha();
    }

    /**
     * 收藏房间按钮点击事件
     *
     * @param view 点击的视图
     */
    public void toCollectionRoomClick(View view) {
        toCollection();
    }

    /**
     * 推特按钮点击事件
     *
     * @param view 点击的视图
     */
    public void toTwitterClick(View view) {
        new TweetDialog(this).show(this);
    }

    /**
     * 标题按钮点击事件
     *
     * @param view 点击的视图
     */
    public void toTitleClick(View view) {
        finish();
    }

    /**
     * 启动浴室活动
     *
     * @param itemId 物品ID
     */
    public void startBathActivity(int itemId) {
        toBath(getScene(itemId), itemId);
    }

    /**
     * 获取场景名称
     *
     * @param itemId 物品ID
     * @return 场景名称
     */
    private String getScene(int itemId) {
        return sScenes[itemId].toString();
    }
}
