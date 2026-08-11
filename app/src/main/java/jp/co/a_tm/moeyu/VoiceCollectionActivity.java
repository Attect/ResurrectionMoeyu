package jp.co.a_tm.moeyu;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.co.a_tm.moeyu.util.AspectRatioUtils;
import jp.co.a_tm.moeyu.util.Logger;

/**
 * 语音收藏活动类
 * 负责显示和管理用户收集的语音内容，支持三种类别：普通、物品、事件
 */
public class VoiceCollectionActivity extends BaseActivity {
    /**
     * 基础行数
     */
    private final int BASE_ROW = 15;
    /** 事件最大行数 */
    private final int EVENT_MAX_ROW = 288;
    /** 物品最大行数 */
    private final int ITEM_MAX_ROW = 244;
    /** 普通最大行数 */
    private final int NORMAL_MAX_ROW = 122;
    /** 完成状态文本视图 */
    private TextView mCompleteState;
    /** 语音控制器 */
    public VoiceTableController mController;
    /** 事件适配器 */
    private VoiceListAdapter mEventAdapter;
    /** 事件是否打开数组 */
    public boolean[] mEventOpened = new boolean[288];
    /** 物品适配器 */
    private VoiceListAdapter mItemAdapter;
    /** 物品是否打开数组 */
    public boolean[] mItemOpened = new boolean[244];
    /** 列表视图 */
    private ListView mListView;
    /** 普通适配器 */
    private VoiceListAdapter mNormalAdapter;
    /** 普通是否打开数组 */
    public boolean[] mNormalOpened = new boolean[122];
    /** 播放器 */
    public MediaPlayer mPlayer;
    /** 当前选择的标签页 */
    public Tab mSelectTab;

    /**
     * 标签页枚举
     * 定义语音收藏的三个分类标签页
     */
    enum Tab {
        NORMAL,
        ITEM,
        EVENT
    }

    /**
     * 创建时回调方法
     * 初始化语音收藏界面
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("VoiceCollectionAcitivity");
        setContentView(R.layout.activity_voicecollection);
        // 非16:9屏幕适配：内容限制为9:16居中显示，黑边填充
        AspectRatioUtils.applyToContent(this);
        this.mController = new VoiceTableController(this);
        this.mListView = (ListView) findViewById(R.id.listview_voicecollec);
        this.mCompleteState = (TextView) findViewById(R.id.textview_voicecollec_getstate);
        setTab(Tab.NORMAL);

        // 设置列表点击监听器
        this.mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                switch (VoiceCollectionActivity.this.mSelectTab.ordinal()) {
                    case 1: // 普通语音
                        if (VoiceCollectionActivity.this.mNormalOpened[position]) {
                            VoiceCollectionActivity.this.getFileDescriptor(VoiceCollectionActivity.this.mPlayer, VoiceCollectionActivity.this.mController.getName(position + 1) + ".ogg");
                            VoiceCollectionActivity.this.mPlayer.start();
                            return;
                        }
                        return;
                    case 2: // 物品语音
                        if (VoiceCollectionActivity.this.mItemOpened[position]) {
                            VoiceCollectionActivity.this.getFileDescriptor(VoiceCollectionActivity.this.mPlayer, VoiceCollectionActivity.this.mController.getName(((position + 122) - 15) + 2) + ".ogg");
                            VoiceCollectionActivity.this.mPlayer.start();
                            return;
                        }
                        return;
                    case 3: // 事件语音
                        if (VoiceCollectionActivity.this.mEventOpened[position]) {
                            VoiceCollectionActivity.this.getFileDescriptor(VoiceCollectionActivity.this.mPlayer, VoiceCollectionActivity.this.mController.getName(((position + 244) - 15) + 2) + ".ogg");
                            VoiceCollectionActivity.this.mPlayer.start();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        });
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
        this.mPlayer = new MediaPlayer();
    }

    /**
     * 暂停时回调方法
     * 活动暂停时释放播放器资源
     */
    @Override
    protected void onPause() {
        super.onPause();
        this.mPlayer.release();
    }

    /**
     * 设置标签页
     *
     * @param tab 标签页类型
     */
    private void setTab(Tab tab) {
        this.mSelectTab = tab;
        switch (this.mSelectTab) {
            case NORMAL: // 普通语音
                if (this.mNormalAdapter == null) {
                    this.mNormalOpened = this.mController.isOpened(1, 108);
                    this.mNormalAdapter = addList(this.mController, this.mNormalOpened, 0);
                }
                this.mListView.setAdapter(this.mNormalAdapter);
                this.mCompleteState.setText(this.mController.countOpened(1, 108) + "/" + 108);
                return;
            case ITEM: // 物品语音
                if (this.mItemAdapter == null) {
                    this.mItemOpened = this.mController.isOpened(109, 230);
                    this.mItemAdapter = addList(this.mController, this.mItemOpened, 108);
                }
                this.mListView.setAdapter(this.mItemAdapter);
                this.mCompleteState.setText(this.mController.countOpened(108, 229) + "/" + 122);
                return;
            case EVENT: // 事件语音
                if (this.mEventAdapter == null) {
                    this.mEventOpened = this.mController.isOpened(231, 274);
                    this.mEventAdapter = addList(this.mController, this.mEventOpened, 230);
                }
                this.mListView.setAdapter(this.mEventAdapter);
                this.mCompleteState.setText(this.mController.countOpened(230, 273) + "/" + 44);
                return;
            default:
                return;
        }
    }

    /**
     * 添加列表项
     *
     * @param controller 语音控制器
     * @param opened 是否打开数组
     * @param base 基础索引
     * @return 语音列表适配器
     */
    private VoiceListAdapter addList(VoiceTableController controller, boolean[] opened, int base) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < opened.length; i++) {
            if (opened[i]) {
                list.add(controller.getTitle((base + i) + 1));
            } else {
                list.add("？？？？？？");
            }
        }
        return new VoiceListAdapter(this, list);
    }

    /**
     * 获取文件描述符
     * 配置媒体播放器的音频数据源
     *
     * @param player 播放器
     * @param str 文件名
     */
    public void getFileDescriptor(MediaPlayer player, String str) {
        try {
            player.reset();
            player.setDataSource(openFileInput(str).getFD());
            player.prepare();
        } catch (IllegalArgumentException e1) {
            e1.printStackTrace();
        } catch (IllegalStateException e12) {
            e12.printStackTrace();
        } catch (FileNotFoundException e13) {
            e13.printStackTrace();
        } catch (IOException e14) {
            e14.printStackTrace();
        }
    }

    /**
     * 标签页点击事件
     *
     * @param view 点击的视图
     */
    public void tabClick(View view) {
        ImageButton normalButton = findViewById(R.id.imgbutton_voicecollec_normal);
        ImageButton itemButton = findViewById(R.id.imgbutton_voicecollec_item);
        ImageButton eventButton = findViewById(R.id.imgbutton_voicecollec_event);
        int id = view.getId();
        if (id == R.id.imgbutton_voicecollec_normal) { /*2131624131*/
            normalButton.setImageResource(R.drawable.nomalvoice);
            itemButton.setImageResource(R.drawable.itemvoice_glay);
            eventButton.setImageResource(R.drawable.iventvoice_glay);
            setTab(Tab.NORMAL);
            return;
        } else if (id == R.id.imgbutton_voicecollec_item) { /*2131624132*/
            normalButton.setImageResource(R.drawable.nomalvoice_glay);
            itemButton.setImageResource(R.drawable.itemvoice);
            eventButton.setImageResource(R.drawable.iventvoice_glay);
            setTab(Tab.ITEM);
            return;
        } else if (id == R.id.imgbutton_voicecollec_event) { /*2131624133*/
            normalButton.setImageResource(R.drawable.nomalvoice_glay);
            itemButton.setImageResource(R.drawable.itemvoice_glay);
            eventButton.setImageResource(R.drawable.iventvoice);
            setTab(Tab.EVENT);
            return;
        }
        return;
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
     * 标题按钮点击事件
     *
     * @param view 点击的视图
     */
    public void toTitleClick(View view) {
        finish();
    }
}
