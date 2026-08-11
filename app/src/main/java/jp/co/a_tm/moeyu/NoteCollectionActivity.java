package jp.co.a_tm.moeyu;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import jp.co.a_tm.moeyu.util.AspectRatioUtils;
import jp.co.a_tm.moeyu.util.Logger;

/**
 * 笔记收藏活动类
 * 负责显示和管理用户收集的笔记内容
 */
public class NoteCollectionActivity extends BaseActivity {
    /**
     * 打开的物品数量
     */
    private int mItemCountOpened;
    /** 笔记控制器 */
    private NoteTableController mNoteController;
    /** 打开的语音数量 */
    private int mVoiceCountOpened;

    /**
     * 笔记列表适配器类
     * 负责创建和管理笔记列表项视图
     */
    private class NoteListAdapter extends ArrayAdapter<NoteListItem> {
        /** 布局加载器 */
        private LayoutInflater mInflater;

        /**
         * 构造函数
         *
         * @param context 上下文
         * @param list 笔记列表
         */
        public NoteListAdapter(Context context, List<NoteListItem> list) {
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
            View view = this.mInflater.inflate(R.layout.list_note, null);
            final NoteListItem item = (NoteListItem) getItem(position);
            setLeftContents(view, item);
            setRightContents(view, item);

            // 设置右侧阅读图标点击事件
            ((ImageButton) view.findViewById(R.id.imgbutton_listview_right_bottom_readicon)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Logger.d("imgButton");
                    if (item.getCleared()) {
                        // 更新笔记状态并显示内容
                        new NoteTableController(NoteListAdapter.this.getContext()).update(String.format("%02d", new Object[]{Integer.valueOf(item.mNo)}));
                        ((ImageView) NoteCollectionActivity.this.findViewById(R.id.img_diarydialog_contents_words)).setImageResource(NoteListAdapter.this.getContext().getResources().getIdentifier("diary_" + item.mNo, "drawable", "jp.co.a_tm.moeyu"));
                        NoteCollectionActivity.this.findViewById(R.id.layout_diarydialog).setVisibility(View.VISIBLE);
                    }
                }
            });
            return view;
        }

        /**
         * 设置左侧内容
         *
         * @param view 视图
         * @param item 笔记项
         */
        private void setLeftContents(View view, NoteListItem item) {
            int noTenths = getTenths(item.mNo);
            int noOnes = item.mNo % 10;
            setImageResource(view, R.id.img_listview_left_tenth, "diary_number" + String.format("%02d", new Object[]{Integer.valueOf(noTenths)}));
            setImageResource(view, R.id.img_listview_left_one, "diary_number" + String.format("%02d", new Object[]{Integer.valueOf(noOnes)}));
            if (item.getCleared()) {
                setImageViewVisibility(view, R.id.img_listview_left_lock, 4);
            } else {
                setImageViewVisibility(view, R.id.img_listview_left_lock, 0);
            }
        }

        /**
         * 设置右侧内容
         *
         * @param view 视图
         * @param item 笔记项
         */
        private void setRightContents(View view, NoteListItem item) {
            int termHundreds = item.mTerm / 100;
            int termTenths = getTenths(item.mTerm);
            int termOnes = item.mTerm % 10;
            if (item.mNo % 2 == 1) {
                // 奇数笔记项显示语音信息
                setLinearLayoutVisibility(view, R.id.layout_listview_right_center_voice, 0);
                setLinearLayoutVisibility(view, R.id.layout_listview_right_center_item, 4);
                if (item.getPreCleared()) {
                    setImageResource(view, R.id.img_listview_right_center_voice_center_hundred, "number" + String.format("%02d", new Object[]{Integer.valueOf(termHundreds)}));
                    setImageResource(view, R.id.img_listview_right_center_voice_center_tenth, "number" + String.format("%02d", new Object[]{Integer.valueOf(termTenths)}));
                    setImageResource(view, R.id.img_listview_right_center_voice_center_one, "number" + String.format("%02d", new Object[]{Integer.valueOf(termOnes)}));
                    if (item.mTerm < 10) {
                        setImageViewVisibility(view, R.id.img_listview_right_center_voice_center_hundred, 4);
                        setImageViewVisibility(view, R.id.img_listview_right_center_voice_center_tenth, 4);
                    } else if (item.mTerm < 100) {
                        setImageViewVisibility(view, R.id.img_listview_right_center_voice_center_hundred, 4);
                        setImageViewVisibility(view, R.id.img_listview_right_center_voice_center_tenth, 0);
                    } else {
                        setImageViewVisibility(view, R.id.img_listview_right_center_voice_center_hundred, 0);
                        setImageViewVisibility(view, R.id.img_listview_right_center_voice_center_tenth, 0);
                    }
                } else {
                    setImageResource(view, R.id.img_listview_right_center_voice_center_hundred, "number_q");
                    setImageResource(view, R.id.img_listview_right_center_voice_center_tenth, "number_q");
                    setImageResource(view, R.id.img_listview_right_center_voice_center_one, "number_q");
                    setImageViewVisibility(view, R.id.img_listview_right_center_voice_center_hundred, 0);
                    setImageViewVisibility(view, R.id.img_listview_right_center_voice_center_tenth, 0);
                    setImageViewVisibility(view, R.id.img_listview_right_center_voice_center_one, 0);
                }
            } else {
                // 偶数笔记项显示物品信息
                setLinearLayoutVisibility(view, R.id.layout_listview_right_center_voice, 4);
                setLinearLayoutVisibility(view, R.id.layout_listview_right_center_item, 0);
                if (item.getPreCleared()) {
                    setImageResource(view, R.id.img_listview_right_center_item_center_tenth, "number" + String.format("%02d", new Object[]{Integer.valueOf(termTenths)}));
                    setImageResource(view, R.id.img_listview_right_center_item_center_one, "number" + String.format("%02d", new Object[]{Integer.valueOf(termOnes)}));
                    if (item.mTerm < 10) {
                        setImageViewVisibility(view, R.id.img_listview_right_center_item_center_tenth, 4);
                    } else {
                        setImageViewVisibility(view, R.id.img_listview_right_center_item_center_tenth, 0);
                    }
                } else {
                    setImageResource(view, R.id.img_listview_right_center_item_center_tenth, "number_q");
                    setImageResource(view, R.id.img_listview_right_center_item_center_one, "number_q");
                    setImageViewVisibility(view, R.id.img_listview_right_center_item_center_tenth, 0);
                    setImageViewVisibility(view, R.id.img_listview_right_center_item_center_one, 0);
                }
            }
            if (item.getCleared()) {
                setImageResource(view, R.id.imgbutton_listview_right_bottom_readicon, "readicon_a");
            } else {
                setImageResource(view, R.id.imgbutton_listview_right_bottom_readicon, "readicon_b");
            }
        }

        /**
         * 设置图片资源
         *
         * @param view 视图
         * @param resId 资源ID
         * @param file 文件名
         */
        private void setImageResource(View view, int resId, String file) {
            ((ImageView) view.findViewById(resId)).setImageResource(getContext().getResources().getIdentifier(file, "drawable", "jp.co.a_tm.moeyu"));
        }

        /**
         * 设置图片视图可见性
         *
         * @param view 视图
         * @param resId 资源ID
         * @param visibility 可见性
         */
        private void setImageViewVisibility(View view, int resId, int visibility) {
            ((ImageView) view.findViewById(resId)).setVisibility(visibility);
        }

        /**
         * 设置线性布局可见性
         *
         * @param view 视图
         * @param resId 资源ID
         * @param visibility 可见性
         */
        private void setLinearLayoutVisibility(View view, int resId, int visibility) {
            ((LinearLayout) view.findViewById(resId)).setVisibility(visibility);
        }

        /**
         * 获取十位数
         *
         * @param num 数字
         * @return 十位数
         */
        private int getTenths(int num) {
            return (num % 100) / 10;
        }
    }

    /**
     * 创建时回调方法
     * 初始化笔记收藏界面
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("NoteCollectionAcitivity");
        setContentView(R.layout.activity_notecollection);

        // 非16:9屏幕适配：内容限制为9:16居中显示，黑边填充
        AspectRatioUtils.applyToContent(this);
        findViewById(R.id.layout_note_collection).setBackgroundColor(Color.BLACK);

        // 初始化控制器和计数
        ItemTableController itemController = new ItemTableController(this);
        VoiceTableController voiceController = new VoiceTableController(this);
        this.mItemCountOpened = itemController.countOpened();
        this.mVoiceCountOpened = voiceController.countOpened();
        this.mNoteController = new NoteTableController(this);

        // 创建笔记列表
        List<NoteListItem> list = new ArrayList<>();
        for (int i = 1; i <= this.mNoteController.countRows(); i++) {
            list.add(new NoteListItem(i, this.mNoteController.getTerm(i), checkCleared(i), checkPreCleared(i)));
        }
        // 设置适配器
        ((ListView) findViewById(R.id.listview_notecollec)).setAdapter(new NoteListAdapter(this, list));
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
     * 检查笔记是否已经完成
     *
     * @param rowNumber 笔记行号
     * @return 是否完成
     */
    private boolean checkCleared(int rowNumber) {
        if (rowNumber % 2 == 1) {
            // 奇数位需要达到指定的语音数量才能解锁
            if (this.mVoiceCountOpened >= this.mNoteController.getTerm(rowNumber)) {
                return true;
            }
        } else {
            // 偶数位需要达到指定的物品数量才能解锁
            if (this.mItemCountOpened >= this.mNoteController.getTerm(rowNumber)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查前置笔记是否已经完成
     *
     * @param rowNumber 笔记行号
     * @return 是否前置完成
     */
    private boolean checkPreCleared(int rowNumber) {
        if (rowNumber > 2 && !checkCleared(rowNumber - 2)) {
            return false;
        }
        return true;
    }

    /**
     * 笔记点击事件
     *
     * @param view 点击的视图
     */
    public void onDiaryClick(View view) {
        view.setVisibility(View.INVISIBLE);
    }

    /**
     * 阅读按钮点击事件
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
     * 房间按钮点击事件
     *
     * @param view 点击的视图
     */
    public void toMomorisClick(View view) {
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
