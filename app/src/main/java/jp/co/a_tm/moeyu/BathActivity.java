package jp.co.a_tm.moeyu;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.a_tm.moeyu.live2d.LAppLive2DManager;
import jp.co.a_tm.moeyu.live2d.LAppLive2DManager.FinishListener;
import jp.co.a_tm.moeyu.live2d.motion.LAppAnimation;
import jp.co.a_tm.moeyu.live2d.view.LAppGLView;
import jp.co.a_tm.moeyu.live2d.view.LAppRenderer;
import jp.co.a_tm.moeyu.model.EventData;
import jp.co.a_tm.moeyu.model.UserData;
import jp.co.a_tm.moeyu.util.Logger;
import jp.co.a_tm.moeyu.util.UserDataManager;

/**
 * 浴室活动类
 * 负责显示和处理浴室内Live2D内容、触摸交互、物品使用等功能
 */
public class BathActivity extends BaseActivity implements OnTouchListener {
    /**
     * 日志标识符
     */
    private static final String TAG = BathActivity.class.getSimpleName();
    /** Live2D视图尺寸（正方形） */
    private static final int LIVE2D_VIEW_SIZE = 480;
    /** 无标记语音1（特殊语音，不计入收藏） */
    private static final String VOICE_NO_MARK_1 = "012";
    /** 无标记语音2（特殊语音，不计入收藏） */
    private static final String VOICE_NO_MARK_2 = "013";

    /** 背景音乐播放器 */
    public MediaPlayer mBgmMp;
    /** 各场景的背景音乐资源数组 */
    public final int[][] mBgms;
    /** 相机预览组件 */
    private CameraPreview mCamera;
    /** 链式事件列表 */
    private List<String> mChainEvent;
    /** 对话框 */
    public Dialog mDialog;
    /** 对话框布局 */
    private LinearLayout mDialogLayout;
    /** 运行标志 */
    public boolean mFlag;
    /** 处理器 */
    public Handler mHandler = new Handler();
    /** Live2D模型设置是否完成 */
    private boolean mIsFinishedLive2dSetup;
    /** 物品网格视图 */
    private GridView mItemGrid;
    /** 物品列表 */
    private List<Integer> mItemList;
    /** Live2D管理器 */
    private LAppLive2DManager mLive2dManager;
    /** 菜单视图 */
    private View mMenu;
    /** 区域映射 */
    private Map<Scene, LinkedHashMap<Region, PointF[]>> mRegions = new HashMap<>();
    /** 渲染器 */
    public LAppRenderer mRenderer;
    /** 当前场景 */
    public Scene mScene;
    /** 场景切换定时器 */
    private Timer mSceneChange;
    /** 当前选中的物品 */
    public int mSelectedItem;
    /** 用户数据 */
    public UserData mUserData;
    /** 语音管理器 */
    private VoiceManager mVoiceManager;
    /** 语音播放器 */
    public MediaPlayer mVoiceMp;
    /** 语音队列 */
    public LinkedList<String> mVoiceQueue = new LinkedList<>();

    /**
     * 构造函数，初始化区域和背景音乐配置
     */
    public BathActivity() {
        LinkedHashMap<Region, PointF[]> common = createCommonRegionMap();
        // 为所有场景设置通用区域配置
        for (Scene scene : Scene.values()) {
            this.mRegions.put(scene, common);
        }
        // 为头部场景额外配置手臂区域
        LinkedHashMap<Region, PointF[]> headMap = new LinkedHashMap<>();
        headMap.putAll(common);
        headMap.put(Region.arm, new PointF[]{new PointF(0.0f, 0.196f), new PointF(1.0f, 0.656f)});
        this.mRegions.put(Scene.head, headMap);
        // 设置各个场景的背景音乐
        this.mBgms = new int[Scene.values().length][];
        this.mBgms[Scene.bath_a.number] = new int[]{R.raw.se354, R.raw.se355, R.raw.se356, R.raw.se357};
        this.mBgms[Scene.bath_b.number] = new int[]{R.raw.se354, R.raw.se355, R.raw.se356, R.raw.se357};
        this.mBgms[Scene.body.number] = new int[]{R.raw.se361, R.raw.se363, R.raw.se364};
        this.mBgms[Scene.head.number] = new int[]{R.raw.se359, R.raw.se360, R.raw.se361};
        this.mVoiceMp = new MediaPlayer();
    }

    /**
     * 自动切换场景
     * 定时自动切换场景
     */
    private void changeSceneAuto() {
        final Handler handler = new Handler();
        if (this.mSceneChange != null) {
            this.mSceneChange.cancel();
        }
        this.mSceneChange = new Timer(true);
        this.mSceneChange.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (BathActivity.this.mScene.next() != null) {
                            BathActivity.this.changeScene(BathActivity.this.mScene.next());
                        }
                    }
                });
            }
        }, (long) getResources().getInteger(R.integer.scene_change_interval_ms));
    }

    /**
     * 创建通用区域映射
     * 定义各个区域的坐标范围
     *
     * @return 通用区域映射
     */
    private LinkedHashMap<Region, PointF[]> createCommonRegionMap() {
        // 定义各个区域的坐标范围
        PointF[] face = new PointF[]{new PointF(0.27f, 0.394f), new PointF(0.666f, 0.564f)};
        PointF[] head = new PointF[]{new PointF(0.083f, 0.196f), new PointF(0.854f, 0.577f)};
        PointF[] brest = new PointF[]{new PointF(0.229f, 0.656f), new PointF(0.75f, 0.866f)};
        PointF[] belly = new PointF[]{new PointF(0.333f, 0.853f), new PointF(0.625f, 1.0f)};
        PointF[] arm = new PointF[]{new PointF(0.187f, 0.603f), new PointF(0.791f, 1.0f)};
        PointF[] none = new PointF[]{new PointF(0.0f, 0.0f), new PointF(1.0f, 1.0f)};
        LinkedHashMap<Region, PointF[]> common = new LinkedHashMap<>();
        common.put(Region.face, face);
        common.put(Region.head, head);
        common.put(Region.brest, brest);
        common.put(Region.belly, belly);
        common.put(Region.arm, arm);
        common.put(Region.none, none);
        return common;
    }

    /**
     * 创建物品列表
     * 根据用户数据生成物品列表
     *
     * @return 物品列表
     */
    private List<Integer> createItemList() {
        List<Integer> list = new ArrayList<>();
        list.add(Integer.valueOf(R.drawable.bath_item_null));
        // 根据用户数据生成物品
        for (int i = 1; i <= UserData.MAX_ITEM_COUNT; i++) {
            list.add(Integer.valueOf(getResId(String.format(this.mUserData.isItemGet(i) ? "item%02d" : "gray_item%02d", new Object[]{Integer.valueOf(i)}))));
        }
        return list;
    }

    /**
     * 获取资源ID
     * 通过资源名称获取资源ID
     *
     * @param resourceName 资源名称
     * @return 资源ID
     */
    private int getResId(String resourceName) {
        return getResources().getIdentifier(resourceName, "drawable", getPackageName());
    }

    /**
     * 获取触摸区域
     * 根据触摸点确定触摸到的区域
     *
     * @param point 触摸点
     * @return 区域
     */
    private Region getRegion(PointF point) {
        Map<Region, PointF[]> map = (Map) this.mRegions.get(this.mScene);
        for (Region region : map.keySet()) {
            PointF[] points = (PointF[]) map.get(region);
            if (isInRange(points[0], points[1], point)) {
                return region;
            }
        }
        return Region.none;
    }

    /**
     * 判断点是否在范围内
     * 判断一个点是否在给定的矩形区域内
     *
     * @param start 起始点
     * @param end 结束点
     * @param point 待判断的点
     * @return 是否在范围内
     */
    private boolean isInRange(PointF start, PointF end, PointF point) {
        return start.x <= point.x && point.x <= end.x && start.y <= point.y && point.y <= end.y;
    }

    /**
     * 创建时回调方法
     * 初始化浴室内内容，设置监听器和组件
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bath);

        // 初始化背景音乐播放器
        this.mBgmMp = MediaPlayer.create(getApplicationContext(), R.raw.se353);
        this.mBgmMp.setOnCompletionListener(getBgmListener());

        try {
            // 初始化语音管理器
            this.mVoiceManager = new VoiceManager(getApplicationContext());
        } catch (IOException e) {
            Logger.e(TAG, "初始化语音管理器失败", e);
        } catch (JSONException e2) {
            Logger.e(TAG, "初始化语音管理器失败(JSON)", e2);
        }

        // 初始化Live2D管理器
        this.mLive2dManager = new LAppLive2DManager(getApplicationContext());
        this.mIsFinishedLive2dSetup = false;
        this.mLive2dManager.setFinishListener(new FinishListener() {
            @Override
            public void onFinishSetupModel() {
                BathActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BathActivity.this.onFinishSetup();
                    }
                });
            }
        });

        // 创建Live2D视图
        LAppGLView lAppGLView = this.mLive2dManager.createView(this, new Rect(0, 0, LIVE2D_VIEW_SIZE, LIVE2D_VIEW_SIZE));
        this.mRenderer = lAppGLView.getRenderer();
        lAppGLView.setOnTouchListener(this);
        ((FrameLayout) findViewById(R.id.frame)).addView(lAppGLView, 0);

        // 设置视图参数和背景色
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) lAppGLView.getLayoutParams();
        layoutParams.setMargins(0, MainActivity.FIX_HEIGHT / 2, 0, MainActivity.FIX_HEIGHT / 2);
        lAppGLView.setBackgroundColor(Color.BLACK);

        // 设置和启动Live2D模型
        this.mLive2dManager.setupModel();
        this.mLive2dManager.startAnimation();

        // 加载用户数据
        this.mUserData = new UserDataManager(this).loadUserData();
        ((ImageView) findViewById(R.id.level_image)).setImageResource(getLevelResource(this.mUserData.getLevel()));

        // 创建物品列表
        this.mItemList = createItemList();
        final List<Integer> list = new ArrayList<>(this.mItemList);

        // 获取传递的场景和物品信息
        Intent intent = getIntent();
        String scene = getString(R.string.intent_scene);
        this.mScene = intent.hasExtra(scene) ? Scene.valueOf(intent.getStringExtra(scene)) : Scene.bath_a;
        this.mRenderer.setScene(this.mScene);
        this.mSelectedItem = ((Integer) list.get(intent.getIntExtra(getString(R.string.intent_item), 0))).intValue();
        ((ImageView) findViewById(R.id.item_image_view)).setImageResource(this.mSelectedItem);

        // 设置物品选择网格
        this.mItemGrid = (GridView) getLayoutInflater().inflate(R.layout.item_grid, null);
        this.mItemGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ImageView item = (ImageView) BathActivity.this.findViewById(R.id.item_image_view);
                if (BathActivity.this.mUserData.isItemGet(position) || position == 0) {
                    BathActivity.this.mSelectedItem = ((Integer) list.get(position)).intValue();
                    item.setImageResource(BathActivity.this.mSelectedItem);
                    BathActivity.this.mDialog.dismiss();
                }
            }
        });
        this.mItemGrid.setAdapter(new ItemGridAdapter(getApplicationContext(), list));

        // 设置对话框和菜单
        this.mDialogLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_bath, null);
        this.mDialog = new Dialog(this, R.style.clear_dialog);
        this.mDialog.setContentView(this.mDialogLayout);

        // 开始动画
        startWhitein();
        this.mMenu = getLayoutInflater().inflate(R.layout.dialog_bath_menu, null);
        updateVoiceNum();
        updateVoiceNumDenominator();
        this.mChainEvent = new ArrayList<>();
    }

    /**
     * Live2D模型设置完成回调
     * 在Live2D模型设置完成后执行的操作
     */
    private void onFinishSetup() {
        this.mIsFinishedLive2dSetup = true;
        findViewById(R.id.indicator).setVisibility(View.GONE);
        if (PreferenceActivity.isEnableCamera(this)) {
            startCamera();
        }

        // 检查是否有事件数据
        if (getIntent().hasExtra(getString(R.string.intent_event))) {
            EventData eventData = (EventData) getIntent().getSerializableExtra(getString(R.string.intent_event));
            ArrayList<String> voiceList = eventData.getVoiceList();
            if (voiceList != null && voiceList.size() > 0) {
                this.mVoiceQueue = new LinkedList<>(voiceList);
                String voiceName = (String) this.mVoiceQueue.poll();
                if (voiceName != null) {
                    try {
                        // 显示事件标题
                        ImageView eventTitle = (ImageView) findViewById(R.id.event_title);
                        switch (eventData.getType()) {
                            case Level2:
                                eventTitle.setImageResource(R.drawable.spa_event_lv02);
                                break;
                            case Level3:
                                eventTitle.setImageResource(R.drawable.spa_event_lv03);
                                break;
                            case Level4:
                                eventTitle.setImageResource(R.drawable.spa_event_lv04);
                                break;
                            case Level5:
                                eventTitle.setImageResource(R.drawable.spa_event_lv05);
                                break;
                            case Level6:
                                eventTitle.setImageResource(R.drawable.spa_event_lv06);
                                break;
                            case Complete:
                                eventTitle.setImageResource(R.drawable.spa_event_item);
                                break;
                        }
                        findViewById(R.id.event_view).setVisibility(View.VISIBLE);

                        // 设置语音播放完成监听器
                        this.mVoiceMp.setOnCompletionListener(new OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                String voiceName = (String) BathActivity.this.mVoiceQueue.poll();
                                if (voiceName != null) {
                                    try {
                                        BathActivity.this.startVoiceAndAnimation(voiceName);
                                        return;
                                    } catch (IllegalArgumentException e) {
                                        Logger.e(TAG, "播放语音失败", e);
                                        return;
                                    } catch (IllegalStateException e2) {
                                        Logger.e(TAG, "播放语音失败", e2);
                                        return;
                                    } catch (FileNotFoundException e3) {
                                        Logger.e(TAG, "播放语音失败", e3);
                                        return;
                                    } catch (IOException e4) {
                                        Logger.e(TAG, "播放语音失败", e4);
                                        return;
                                    }
                                }

                                // 播放淡出动画
                                Animation animation = AnimationUtils.loadAnimation(BathActivity.this, R.anim.common_fadeout);
                                animation.setAnimationListener(new AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        BathActivity.this.mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((FrameLayout) BathActivity.this.findViewById(R.id.frame)).removeView(BathActivity.this.findViewById(R.id.event_view));
                                            }
                                        });
                                    }
                                });
                                BathActivity.this.findViewById(R.id.event_view).startAnimation(animation);
                                BathActivity.this.mVoiceMp.setOnCompletionListener(null);
                            }
                        });
                        startVoiceAndAnimation(voiceName);
                    } catch (IllegalArgumentException e) {
                        Logger.e(TAG, "播放事件语音失败", e);
                    } catch (IllegalStateException e2) {
                        Logger.e(TAG, "播放事件语音失败", e2);
                    } catch (FileNotFoundException e3) {
                        Logger.e(TAG, "播放事件语音失败", e3);
                    } catch (IOException e4) {
                        Logger.e(TAG, "播放事件语音失败", e4);
                    }
                }
            }
        }

        // 显示引导提示
        if (new PreferencesHelper(this).isInitBath()) {
            ImageView recommend = (ImageView) findViewById(R.id.recommend);
            recommend.setImageResource(R.drawable.recommend_plate01a);
            recommend.setTag("first");
            recommend.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 获取背景音乐播放监听器
     * 创建循环播放背景音乐的监听器
     *
     * @return 背景音乐监听器
     */
    private OnCompletionListener getBgmListener() {
        return new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    // 释放当前播放器
                    BathActivity.this.mBgmMp.release();
                    int[] bgms = BathActivity.this.mBgms[BathActivity.this.mScene.number];
                    // 选择随机音乐并播放
                    Uri uri = Uri.parse("android.resource://" + BathActivity.this.getPackageName() + "/" + bgms[new Random().nextInt(bgms.length)]);
                    BathActivity.this.mBgmMp = new MediaPlayer();
                    BathActivity.this.mBgmMp.setDataSource(BathActivity.this.getApplicationContext(), uri);
                    BathActivity.this.mBgmMp.setOnCompletionListener(BathActivity.this.getBgmListener());
                    BathActivity.this.mBgmMp.prepare();
                    BathActivity.this.mBgmMp.start();
                } catch (IllegalArgumentException e) {
                    Logger.e(TAG, "播放背景音乐失败", e);
                } catch (SecurityException e2) {
                    Logger.e(TAG, "播放背景音乐失败", e2);
                } catch (IllegalStateException e3) {
                    Logger.e(TAG, "播放背景音乐失败", e3);
                } catch (IOException e4) {
                    Logger.e(TAG, "播放背景音乐失败", e4);
                }
            }
        };
    }

    /**
     * 恢复时回调方法
     * 恢复活动时启动相关服务
     */
    @Override
    protected void onResume() {
        super.onResume();
        // 启动标志设置为true
        this.mFlag = true;
        smoking();
        this.mBgmMp.start();
        if (PreferenceActivity.isEnableCamera(this) && this.mIsFinishedLive2dSetup) {
            startCamera();
        }
    }

    /**
     * 暂停时回调方法
     * 暂停活动时停止相关服务
     */
    @Override
    protected void onPause() {
        super.onPause();
        this.mFlag = false;
        this.mBgmMp.pause();
        // 停止语音播放
        if (this.mVoiceMp.isPlaying()) {
            this.mVoiceMp.stop();
        }
        // 停止相机预览
        if (this.mCamera != null) {
            ((FrameLayout) findViewById(R.id.frame)).removeView(this.mCamera);
            this.mCamera.stop();
            this.mCamera = null;
        }
    }

    /**
     * 销毁时回调方法
     * 销毁活动时释放资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mBgmMp.release();
        this.mVoiceMp.release();
        Logger.d(getClass().getSimpleName() + " onDestroy()");
    }

    /**
     * 释放资源
     * 取消定时器任务
     */
    public void release() {
        if (this.mSceneChange != null) {
            this.mSceneChange.cancel();
            this.mSceneChange.purge();
            this.mSceneChange = null;
        }
    }

    /**
     * 启动相机预览
     * 显示相机预览画面
     */
    private void startCamera() {
        this.mRenderer.isAr = true;
        this.mCamera = new CameraPreview(this);
        ((FrameLayout) findViewById(R.id.frame)).addView(this.mCamera, 0);
    }

    /**
     * 启动烟雾效果
     * 显示烟雾动画效果
     */
    private void smoking() {
        final ImageView smoke = (ImageView) findViewById(R.id.smoke);
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            public int mN;

            @Override
            public void run() {
                while (BathActivity.this.mFlag) {
                    try {
                        Thread.sleep(100);
                        if (254 < this.mN) {
                            this.mN = -255;
                        } else {
                            this.mN += 5;
                        }
                        // 更新烟雾位置和透明度
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                smoke.setAlpha(255 - Math.abs(mN));
                                smoke.setPadding(0, -mN, 0, -mN);
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 准备选项菜单
     *
     * @param menu 菜单
     * @return 是否继续处理
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        this.mDialogLayout.removeAllViews();
        this.mDialogLayout.addView(this.mMenu);
        this.mDialog.show();
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * 菜单顶部按钮点击事件
     *
     * @param view 点击的视图
     */
    public void onMenuTop(View view) {
        if (this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        finish();
    }

    /**
     * 物品按钮点击事件
     *
     * @param view 点击的视图
     */
    public void onItemButton(View view) {
        this.mDialogLayout.removeAllViews();
        this.mDialogLayout.addView(this.mItemGrid);
        this.mDialog.show();
    }

    /**
     * 跳过按钮点击事件
     *
     * @param view 点击的视图
     */
    public void onSkipButton(View view) {
        this.mDialogLayout.removeAllViews();
        switch (this.mScene) {
            case bath_a:
                this.mDialogLayout.addView(createSkipButton(Scene.head));
                break;
            case bath_b:
                this.mDialogLayout.addView(createSkipButton(Scene.body));
                this.mDialogLayout.addView(createSkipButton(Scene.bath_a));
                break;
            case head:
                this.mDialogLayout.addView(createSkipButton(Scene.bath_a));
                this.mDialogLayout.addView(createSkipButton(Scene.body));
                break;
            case body:
                this.mDialogLayout.addView(createSkipButton(Scene.head));
                this.mDialogLayout.addView(createSkipButton(Scene.bath_b));
                break;
        }
        this.mDialog.show();
    }

    /**
     * 开始淡入动画
     * 显示白屏动画效果
     */
    private void startWhitein() {
        final View view = findViewById(R.id.white_screen);
        view.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.whitein);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);
                BathActivity.this.changeSceneAuto();
            }
        });
        view.setAnimation(animation);
    }

    /**
     * 启动淡出淡入动画
     * 切换场景时的过渡动画
     *
     * @param scene 目标场景
     */
    private void startWhiteOutAndIn(final Scene scene) {
        final View view = findViewById(R.id.white_screen);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.whiteout);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                BathActivity.this.mRenderer.setScene(scene);
                Logger.d("onAnimationEnd: " + scene);
                BathActivity.this.startWhitein();
            }
        });
        view.setAnimation(animation);
        view.invalidate();
    }

    /**
     * 创建跳过按钮
     * 根据场景创建跳过按钮
     *
     * @param scene 场景类型
     * @return 跳过按钮
     */
    private ImageButton createSkipButton(final Scene scene) {
        ImageButton imageButton = (ImageButton) getLayoutInflater().inflate(R.layout.clear_image_button, null);
        imageButton.setImageResource(getSkipImageResourceIdFromScene(scene));
        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BathActivity.this.mDialog.dismiss();
                BathActivity.this.changeScene(scene);
            }
        });
        return imageButton;
    }

    /**
     * 切换场景
     * 切换到指定场景并播放切换音乐
     *
     * @param next 目标场景
     */
    private void changeScene(Scene next) {
        startWhiteOutAndIn(next);
        this.mBgmMp.release();
        this.mBgmMp = MediaPlayer.create(getApplicationContext(), getSceneChangeBgmResourceIdFromScene(next));
        this.mBgmMp.setOnCompletionListener(getBgmListener());
        this.mBgmMp.start();
        this.mScene = next;
    }

    /**
     * 根据场景获取切换背景音乐资源ID
     *
     * @param scene 场景
     * @return 背景音乐资源ID
     */
    private int getSceneChangeBgmResourceIdFromScene(Scene scene) {
        switch (scene) {
            case bath_a:
            case bath_b:
            case body:
                return R.raw.se362;
            case head:
                return R.raw.se358;
            default:
                throw new IllegalArgumentException("Unknown scene: " + scene);
        }
    }

    /**
     * 根据场景获取跳过按钮图片资源ID
     *
     * @param scene 场景
     * @return 跳过按钮图片资源ID
     */
    private int getSkipImageResourceIdFromScene(Scene scene) {
        switch (scene) {
            case bath_a:
                return R.drawable.skip00;
            case bath_b:
                return R.drawable.skip03;
            case head:
                return R.drawable.skip01;
            case body:
                return R.drawable.skip02;
            default:
                throw new IllegalArgumentException("Unknown scene: " + scene);
        }
    }

    /**
     * 触摸事件处理
     *
     * @param v     触摸视图
     * @param event 触摸事件
     * @return 是否处理该事件
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 点击结束或未选择物品时才响应触摸
        if (event.getAction() == MotionEvent.ACTION_UP || !this.mItemList.contains(Integer.valueOf(this.mSelectedItem))) {
            Region region = getRegion(new PointF(event.getX() / ((float) v.getWidth()), event.getY() / ((float) v.getHeight())));
            if (!(this.mVoiceMp.isPlaying() || Region.none == region)) {
                // 使用物品时显示图标
                if (this.mSelectedItem != R.drawable.bath_item_null) {
                    drawItem(event);
                }
                try {
                    int item = this.mItemList.indexOf(Integer.valueOf(this.mSelectedItem));
                    // 获取链式事件或普通事件
                    if (this.mChainEvent.size() == 0) {
                        this.mChainEvent = SpecialEvent.get(this.mUserData, item, this.mScene, region);
                    }
                    if (this.mChainEvent.size() != 0) {
                        startVoiceAndAnimation((String) this.mChainEvent.remove(0));
                    } else {
                        startVoiceAndAnimation(this.mVoiceManager.getVoiceName(this.mScene, region, item, this.mUserData.getLevel()));
                    }
                } catch (IllegalArgumentException e) {
                    Logger.e(TAG, "播放触摸语音失败", e);
                } catch (IllegalStateException e2) {
                    Logger.e(TAG, "播放触摸语音失败", e2);
                } catch (IOException e3) {
                    Logger.e(TAG, "播放触摸语音失败", e3);
                } catch (JSONException e4) {
                    Logger.e(TAG, "播放触摸语音失败", e4);
                }
            }
        }
        return false;
    }

    /**
     * 启动语音和动画
     * 播放语音并触发动画
     *
     * @param voiceName 语音名称
     * @throws IllegalArgumentException 异常
     * @throws IllegalStateException 异常
     * @throws FileNotFoundException 异常
     * @throws IOException 异常
     */
    private void startVoiceAndAnimation(String voiceName) throws IllegalArgumentException, IllegalStateException, FileNotFoundException, IOException {
        Log.d(TAG, "startVoiceAndAnimation: " + voiceName);
        this.mVoiceMp.reset();
        VoiceTableController voiceTable = new VoiceTableController(this);
        // 检查语音是否已打开并更新打开状态及弹窗
        if (!(voiceTable.isOpened(voiceName) || VOICE_NO_MARK_1.equals(voiceName) || VOICE_NO_MARK_2.equals(voiceName))) {
            voiceTable.update(voiceName);
            updateVoiceNum(voiceTable.countOpened());
            popupNewVoice();
        }
        LAppAnimation mAnimation = this.mLive2dManager.getAnimation();
        if (mAnimation != null) {
            Logger.d("Animation");
            mAnimation.startTouchMotion(voiceName + getMotionSuffix(this.mScene));
        }
        this.mVoiceMp.setDataSource(this.mVoiceManager.getVoiceFileDescripter(voiceName));
        this.mVoiceMp.prepare();
        this.mVoiceMp.start();
    }

    /**
     * 更新语音数量显示
     *
     * @param count 语音数
     */
    private void updateVoiceNum(int count) {
        updateVoiceNum(R.id.voice_hundred_place, R.id.voice_ten_palace, R.id.voice_one_place, count);
    }

    /**
     * 更新语音数量显示（获取当前打开数）
     */
    private void updateVoiceNum() {
        updateVoiceNum(new VoiceTableController(this).countOpened());
    }

    /**
     * 更新语音数量显示的具体实现
     *
     * @param hundredRes 百位资源ID
     * @param tenRes 十位资源ID
     * @param oneRes 个位资源ID
     * @param count 语音数
     */
    private void updateVoiceNum(int hundredRes, int tenRes, int oneRes, int count) {
        int hundred = count / 100;
        int ten = (count - (hundred * 100)) / 10;
        int one = (count - (hundred * 100)) - (ten * 10);
        ((ImageView) findViewById(hundredRes)).setImageResource(getVoiceNumResource(hundred));
        ((ImageView) findViewById(tenRes)).setImageResource(getVoiceNumResource(ten));
        ((ImageView) findViewById(oneRes)).setImageResource(getVoiceNumResource(one));
    }

    /**
     * 更新语音数量分母显示
     */
    private void updateVoiceNumDenominator() {
        updateVoiceNum(R.id.voice_denominator_hundred_place, R.id.voice_denominator_ten_place, R.id.voice_denominator_one_place, new VoiceTableController(this).countRows());
    }

    /**
     * 获取语音数资源ID
     *
     * @param num 数字
     * @return 资源ID
     */
    private int getVoiceNumResource(int num) {
        switch (num) {
            case 0:
                return R.drawable.bath_voice_num00;
            case 1:
                return R.drawable.bath_voice_num01;
            case 2:
                return R.drawable.bath_voice_num02;
            case 3:
                return R.drawable.bath_voice_num03;
            case 4:
                return R.drawable.bath_voice_num04;
            case 5:
                return R.drawable.bath_voice_num05;
            case 6:
                return R.drawable.bath_voice_num06;
            case 7:
                return R.drawable.bath_voice_num07;
            case 8:
                return R.drawable.bath_voice_num08;
            case 9:
                return R.drawable.bath_voice_num09;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * 获取等级资源ID
     *
     * @param level 等级
     * @return 资源ID
     */
    private int getLevelResource(int level) {
        switch (level) {
            case 1:
                return R.drawable.bath_level_num01;
            case 2:
                return R.drawable.bath_level_num02;
            case 3:
                return R.drawable.bath_level_num03;
            case 4:
                return R.drawable.bath_level_num04;
            case 5:
                return R.drawable.bath_level_num05;
            case 6:
                return R.drawable.bath_level_num06;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * 获取动画后缀
     *
     * @param scene 场景
     * @return 后缀
     */
    private int getMotionSuffix(Scene scene) {
        return scene == Scene.bath_b ? Scene.bath_a.number : this.mScene.number;
    }

    /**
     * 绘制物品使用动画
     *
     * @param event 触摸事件
     */
    private void drawItem(MotionEvent event) {
        ImageView item = (ImageView) findViewById(R.id.use_item);
        View view = findViewById(R.id.item_image_view);
        item.setImageResource(this.mSelectedItem);
        LayoutParams layoutParams = new LayoutParams(item.getLayoutParams());
        layoutParams.setMargins(((int) event.getX()) - (view.getWidth() / 2), ((int) event.getY()) - (view.getHeight() / 2), 0, 0);
        item.setLayoutParams(layoutParams);
        item.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.item_fadeout));
        item.invalidate();
    }

    /**
     * 停止时回调方法
     * 活动停止时关闭活动
     */
    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    /**
     * 抽卡按钮点击事件
     *
     * @param view 点击的视图
     */
    public void onGachaButton(View view) {
        if (this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        toGacha();
    }

    /**
     * 设置按钮点击事件
     *
     * @param view 点击的视图
     */
    public void onPreferenceButton(View view) {
        if (this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        toPreferenceFromBath();
    }

    /**
     * 推荐点击事件
     *
     * @param view 点击的视图
     */
    public void onRecommendClick(View view) {
        if ("first".equals(view.getTag())) {
            ((ImageView) view).setImageResource(R.drawable.recommend_plate01b);
            view.setTag("");
            return;
        }
        new PreferencesHelper(this).setInitBath(false);
        view.setVisibility(View.GONE);
    }

    /**
     * 弹出新语音提示框
     */
    private void popupNewVoice() {
        final View view = findViewById(R.id.voice_new);
        view.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.INVISIBLE);
            }
        }, 2000);
    }

    /**
     * 结束活动时的清理工作
     */
    @Override
    public void finish() {
        super.finish();
        this.mLive2dManager.releaseModel();
        this.mLive2dManager.releaseView();
    }
}
