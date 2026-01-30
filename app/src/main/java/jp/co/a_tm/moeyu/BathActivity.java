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

public class BathActivity extends BaseActivity implements OnTouchListener {
    private static final String TAG = BathActivity.class.getSimpleName();
    /* access modifiers changed from: private */
    public MediaPlayer mBgmMp;
    /* access modifiers changed from: private|final */
    public final int[][] mBgms;
    private CameraPreview mCamera;
    private List<String> mChainEvent;
    /* access modifiers changed from: private */
    public Dialog mDialog;
    private LinearLayout mDialogLayout;
    /* access modifiers changed from: private */
    public boolean mFlag;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler();
    private boolean mIsFinishedLive2dSetup;
    private GridView mItemGrid;
    private List<Integer> mItemList;
    private LAppLive2DManager mLive2dManager;
    private View mMenu;
    private Map<Scene, LinkedHashMap<Region, PointF[]>> mRegions = new HashMap();
    /* access modifiers changed from: private */
    public LAppRenderer mRenderer;
    /* access modifiers changed from: private */
    public Scene mScene;
    private Timer mSceneChange;
    /* access modifiers changed from: private */
    public int mSelectedItem;
    /* access modifiers changed from: private */
    public UserData mUserData;
    private VoiceManager mVoiceManager;
    /* access modifiers changed from: private */
    public MediaPlayer mVoiceMp;
    /* access modifiers changed from: private */
    public LinkedList<String> mVoiceQueue = new LinkedList();

    public BathActivity() {
        LinkedHashMap<Region, PointF[]> common = createCommonRegionMap();
        for (Scene scene : Scene.values()) {
            this.mRegions.put(scene, common);
        }
        LinkedHashMap<Region, PointF[]> headMap = new LinkedHashMap();
        headMap.putAll(common);
        headMap.put(Region.arm, new PointF[]{new PointF(0.0f, 0.196f), new PointF(1.0f, 0.656f)});
        this.mRegions.put(Scene.head, headMap);
        this.mBgms = new int[Scene.values().length][];
        this.mBgms[Scene.bath_a.number] = new int[]{R.raw.se354, R.raw.se355, R.raw.se356, R.raw.se357};
        this.mBgms[Scene.bath_b.number] = new int[]{R.raw.se354, R.raw.se355, R.raw.se356, R.raw.se357};
        this.mBgms[Scene.body.number] = new int[]{R.raw.se361, R.raw.se363, R.raw.se364};
        this.mBgms[Scene.head.number] = new int[]{R.raw.se359, R.raw.se360, R.raw.se361};
        this.mVoiceMp = new MediaPlayer();
    }

    /* access modifiers changed from: private */
    public void changeSceneAuto() {
        final Handler handler = new Handler();
        if (this.mSceneChange != null) {
            this.mSceneChange.cancel();
        }
        this.mSceneChange = new Timer(true);
        this.mSceneChange.schedule(new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (BathActivity.this.mScene.next() != null) {
                            BathActivity.this.changeScene(BathActivity.this.mScene.next());
                        }
                    }
                });
            }
        }, (long) getResources().getInteger(R.integer.scene_change_interval_ms));
    }

    private LinkedHashMap<Region, PointF[]> createCommonRegionMap() {
        PointF[] face = new PointF[]{new PointF(0.27f, 0.394f), new PointF(0.666f, 0.564f)};
        PointF[] head = new PointF[]{new PointF(0.083f, 0.196f), new PointF(0.854f, 0.577f)};
        PointF[] brest = new PointF[]{new PointF(0.229f, 0.656f), new PointF(0.75f, 0.866f)};
        PointF[] belly = new PointF[]{new PointF(0.333f, 0.853f), new PointF(0.625f, 1.0f)};
        PointF[] arm = new PointF[]{new PointF(0.187f, 0.603f), new PointF(0.791f, 1.0f)};
        PointF[] none = new PointF[]{new PointF(0.0f, 0.0f), new PointF(1.0f, 1.0f)};
        LinkedHashMap<Region, PointF[]> common = new LinkedHashMap();
        common.put(Region.face, face);
        common.put(Region.head, head);
        common.put(Region.brest, brest);
        common.put(Region.belly, belly);
        common.put(Region.arm, arm);
        common.put(Region.none, none);
        return common;
    }

    private List<Integer> createItemList() {
        List<Integer> list = new ArrayList();
        list.add(Integer.valueOf(R.drawable.bath_item_null));
        for (int i = 1; i <= 25; i++) {
            list.add(Integer.valueOf(getResId(String.format(this.mUserData.isItemGet(i) ? "item%02d" : "gray_item%02d", new Object[]{Integer.valueOf(i)}))));
        }
        return list;
    }

    private int getResId(String resourceName) {
        return getResources().getIdentifier(resourceName, "drawable", getPackageName());
    }

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

    private boolean isInRange(PointF start, PointF end, PointF point) {
        return start.x <= point.x && point.x <= end.x && start.y <= point.y && point.y <= end.y;
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bath);
        this.mBgmMp = MediaPlayer.create(getApplicationContext(), R.raw.se353);
        this.mBgmMp.setOnCompletionListener(getBgmListener());
        try {
            this.mVoiceManager = new VoiceManager(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e2) {
            e2.printStackTrace();
        }
        this.mLive2dManager = new LAppLive2DManager(getApplicationContext());
        this.mIsFinishedLive2dSetup = false;
        this.mLive2dManager.setFinishListener(new FinishListener() {
            public void onFinishSetupModel() {
                BathActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        BathActivity.this.onFinishSetup();
                    }
                });
            }
        });
        LAppGLView lAppGLView = this.mLive2dManager.createView(this, new Rect(0, 0, 480, 480));
        this.mRenderer = lAppGLView.getRenderer();
        lAppGLView.setOnTouchListener(this);
        ((FrameLayout) findViewById(R.id.frame)).addView(lAppGLView, 0);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) lAppGLView.getLayoutParams();
        layoutParams.setMargins(0, MainActivity.FIX_HEIGHT / 2, 0, MainActivity.FIX_HEIGHT / 2);
        lAppGLView.setBackgroundColor(Color.BLACK);
        this.mLive2dManager.setupModel();
        this.mLive2dManager.startAnimation();
        this.mUserData = new UserDataManager(this).loadUserData();
        ((ImageView) findViewById(R.id.level_image)).setImageResource(getLevelResource(this.mUserData.getLevel()));
        this.mItemList = createItemList();
        final List<Integer> list = new ArrayList(this.mItemList);
        Intent intent = getIntent();
        String scene = getString(R.string.intent_scene);
        this.mScene = intent.hasExtra(scene) ? Scene.valueOf(intent.getStringExtra(scene)) : Scene.bath_a;
        this.mRenderer.setScene(this.mScene);
        this.mSelectedItem = ((Integer) list.get(intent.getIntExtra(getString(R.string.intent_item), 0))).intValue();
        ((ImageView) findViewById(R.id.item_image_view)).setImageResource(this.mSelectedItem);
        this.mItemGrid = (GridView) getLayoutInflater().inflate(R.layout.item_grid, null);
        this.mItemGrid.setOnItemClickListener(new OnItemClickListener() {
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
        this.mDialogLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_bath, null);
        this.mDialog = new Dialog(this, R.style.clear_dialog);
        this.mDialog.setContentView(this.mDialogLayout);
        startWhitein();
        this.mMenu = getLayoutInflater().inflate(R.layout.dialog_bath_menu, null);
        updateVoiceNum();
        updateVoiceNumDenominator();
        this.mChainEvent = new ArrayList();
    }

    /* access modifiers changed from: private */
    public void onFinishSetup() {
        this.mIsFinishedLive2dSetup = true;
        findViewById(R.id.indicator).setVisibility(View.GONE);
        if (PreferenceActivity.isEnableCamera(this)) {
            startCamera();
        }
        if (getIntent().hasExtra(getString(R.string.intent_event))) {
            EventData eventData = (EventData) getIntent().getSerializableExtra(getString(R.string.intent_event));
            ArrayList<String> voiceList = eventData.getVoiceList();
            if (voiceList != null && voiceList.size() > 0) {
                this.mVoiceQueue = new LinkedList(voiceList);
                String voiceName = (String) this.mVoiceQueue.poll();
                if (voiceName != null) {
                    try {
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
                        this.mVoiceMp.setOnCompletionListener(new OnCompletionListener() {
                            public void onCompletion(MediaPlayer mp) {
                                String voiceName = (String) BathActivity.this.mVoiceQueue.poll();
                                if (voiceName != null) {
                                    try {
                                        BathActivity.this.startVoiceAndAnimation(voiceName);
                                        return;
                                    } catch (IllegalArgumentException e) {
                                        e.printStackTrace();
                                        return;
                                    } catch (IllegalStateException e2) {
                                        e2.printStackTrace();
                                        return;
                                    } catch (FileNotFoundException e3) {
                                        e3.printStackTrace();
                                        return;
                                    } catch (IOException e4) {
                                        e4.printStackTrace();
                                        return;
                                    }
                                }
                                Animation animation = AnimationUtils.loadAnimation(BathActivity.this, R.anim.common_fadeout);
                                animation.setAnimationListener(new AnimationListener() {
                                    public void onAnimationStart(Animation animation) {
                                    }

                                    public void onAnimationRepeat(Animation animation) {
                                    }

                                    public void onAnimationEnd(Animation animation) {
                                        BathActivity.this.mHandler.post(new Runnable() {
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
                        e.printStackTrace();
                    } catch (IllegalStateException e2) {
                        e2.printStackTrace();
                    } catch (FileNotFoundException e3) {
                        e3.printStackTrace();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
            }
        }
        if (new PreferencesHelper(this).isInitBath()) {
            ImageView recommend = (ImageView) findViewById(R.id.recommend);
            recommend.setImageResource(R.drawable.recommend_plate01a);
            recommend.setTag("first");
            recommend.setVisibility(View.VISIBLE);
        }
    }

    /* access modifiers changed from: private */
    public OnCompletionListener getBgmListener() {
        return new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                try {
                    BathActivity.this.mBgmMp.release();
                    int[] bgms = BathActivity.this.mBgms[BathActivity.this.mScene.number];
                    Uri uri = Uri.parse("android.resource://" + BathActivity.this.getPackageName() + "/" + bgms[new Random().nextInt(bgms.length)]);
                    BathActivity.this.mBgmMp = new MediaPlayer();
                    BathActivity.this.mBgmMp.setDataSource(BathActivity.this.getApplicationContext(), uri);
                    BathActivity.this.mBgmMp.setOnCompletionListener(BathActivity.this.getBgmListener());
                    BathActivity.this.mBgmMp.prepare();
                    BathActivity.this.mBgmMp.start();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SecurityException e2) {
                    e2.printStackTrace();
                } catch (IllegalStateException e3) {
                    e3.printStackTrace();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
        };
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
//        this.mTracker.trackPageView("お風呂");
        this.mFlag = true;
        smoking();
        this.mBgmMp.start();
        if (PreferenceActivity.isEnableCamera(this) && this.mIsFinishedLive2dSetup) {
            startCamera();
        }
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        this.mFlag = false;
        this.mBgmMp.pause();
        if (this.mVoiceMp.isPlaying()) {
            this.mVoiceMp.stop();
        }
        if (this.mCamera != null) {
            ((FrameLayout) findViewById(R.id.frame)).removeView(this.mCamera);
            this.mCamera.stop();
            this.mCamera = null;
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        this.mBgmMp.release();
        this.mVoiceMp.release();
        Logger.d(getClass().getSimpleName() + " onDestroy()");
    }

    /* access modifiers changed from: protected */
    public void release() {
        if (this.mSceneChange != null) {
            this.mSceneChange.cancel();
            this.mSceneChange.purge();
            this.mSceneChange = null;
        }
    }

    private void startCamera() {
        this.mRenderer.isAr = true;
        this.mCamera = new CameraPreview(this);
        ((FrameLayout) findViewById(R.id.frame)).addView(this.mCamera, 0);
    }

    private void smoking() {
        final ImageView smoke = (ImageView) findViewById(R.id.smoke);
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            /* access modifiers changed from: private */
            public int mN;

            public void run() {
                while (BathActivity.this.mFlag) {
                    try {
                        Thread.sleep(100);
                        if (254 < this.mN) {
                            this.mN = -255;
                        } else {
                            this.mN += 5;
                        }
                        handler.post(new Runnable() {
                            public void run() {
                                smoke.setAlpha(255 - Math.abs(mN));
                                smoke.setPadding(0, mN * -1, 0, mN * -1);
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        this.mDialogLayout.removeAllViews();
        this.mDialogLayout.addView(this.mMenu);
        this.mDialog.show();
        return super.onPrepareOptionsMenu(menu);
    }

    public void onMenuTop(View view) {
        if (this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        finish();
    }

    public void onItemButton(View view) {
        this.mDialogLayout.removeAllViews();
        this.mDialogLayout.addView(this.mItemGrid);
        this.mDialog.show();
    }

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

    /* access modifiers changed from: private */
    public void startWhitein() {
        final View view = findViewById(R.id.white_screen);
        view.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.whitein);
        animation.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);
                BathActivity.this.changeSceneAuto();
            }
        });
        view.setAnimation(animation);
    }

    private void startWhiteOutAndIn(final Scene scene) {
        final View view = findViewById(R.id.white_screen);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.whiteout);
        animation.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                BathActivity.this.mRenderer.setScene(scene);
                Logger.d("onAnimationEnd: " + scene);
                BathActivity.this.startWhitein();
            }
        });
        view.setAnimation(animation);
        view.invalidate();
    }

    private ImageButton createSkipButton(final Scene scene) {
        ImageButton imageButton = (ImageButton) getLayoutInflater().inflate(R.layout.clear_image_button, null);
        imageButton.setImageResource(getSkipImageResourceIdFromScene(scene));
        imageButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BathActivity.this.mDialog.dismiss();
                BathActivity.this.changeScene(scene);
            }
        });
        return imageButton;
    }

    /* access modifiers changed from: private */
    public void changeScene(Scene next) {
        startWhiteOutAndIn(next);
        this.mBgmMp.release();
        this.mBgmMp = MediaPlayer.create(getApplicationContext(), getSceneChangeBgmResourceIdFromScene(next));
        this.mBgmMp.setOnCompletionListener(getBgmListener());
        this.mBgmMp.start();
        this.mScene = next;
    }

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

    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == 1 || !this.mItemList.contains(Integer.valueOf(this.mSelectedItem))) {
            Region region = getRegion(new PointF(event.getX() / ((float) v.getWidth()), event.getY() / ((float) v.getHeight())));
            if (!(this.mVoiceMp.isPlaying() || Region.none == region)) {
                if (this.mSelectedItem != R.drawable.bath_item_null) {
                    drawItem(event);
                }
                try {
                    int item = this.mItemList.indexOf(Integer.valueOf(this.mSelectedItem));
                    if (this.mChainEvent.size() == 0) {
                        this.mChainEvent = SpecialEvent.get(this.mUserData, item, this.mScene, region);
                    }
                    if (this.mChainEvent.size() != 0) {
                        startVoiceAndAnimation((String) this.mChainEvent.remove(0));
                    } else {
                        startVoiceAndAnimation(this.mVoiceManager.getVoiceName(this.mScene, region, item, this.mUserData.getLevel()));
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e2) {
                    e2.printStackTrace();
                } catch (IOException e3) {
                    e3.printStackTrace();
                } catch (JSONException e4) {
                    e4.printStackTrace();
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void startVoiceAndAnimation(String voiceName) throws IllegalArgumentException, IllegalStateException, FileNotFoundException, IOException {
        Log.d(TAG, "startVoiceAndAnimation: " + voiceName);
        this.mVoiceMp.reset();
        VoiceTableController voiceTable = new VoiceTableController(this);
        if (!(voiceTable.isOpened(voiceName) || "012".equals(voiceName) || "013".equals(voiceName))) {
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

    private void updateVoiceNum(int count) {
        updateVoiceNum(R.id.voice_hundred_place, R.id.voice_ten_palace, R.id.voice_one_place, count);
    }

    private void updateVoiceNum() {
        updateVoiceNum(new VoiceTableController(this).countOpened());
    }

    private void updateVoiceNum(int hundredRes, int tenRes, int oneRes, int count) {
        int hundred = count / 100;
        int ten = (count - (hundred * 100)) / 10;
        int one = (count - (hundred * 100)) - (ten * 10);
        ((ImageView) findViewById(hundredRes)).setImageResource(getVoiceNumResource(hundred));
        ((ImageView) findViewById(tenRes)).setImageResource(getVoiceNumResource(ten));
        ((ImageView) findViewById(oneRes)).setImageResource(getVoiceNumResource(one));
    }

    private void updateVoiceNumDenominator() {
        updateVoiceNum(R.id.voice_denominator_hundred_place, R.id.voice_denominator_ten_place, R.id.voice_denominator_one_place, new VoiceTableController(this).countRows());
    }

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

    private int getMotionSuffix(Scene scene) {
        return scene == Scene.bath_b ? Scene.bath_a.number : this.mScene.number;
    }

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

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        finish();
    }

    public void onGachaButton(View view) {
        if (this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        toGacha();
    }

    public void onPreferenceButton(View view) {
        if (this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        toPreferenceFromBath();
    }

    public void onRecommendClick(View view) {
        if ("first".equals(view.getTag())) {
            ((ImageView) view).setImageResource(R.drawable.recommend_plate01b);
            view.setTag("");
            return;
        }
        new PreferencesHelper(this).setInitBath(false);
        view.setVisibility(View.GONE);
    }

    private void popupNewVoice() {
        final View view = findViewById(R.id.voice_new);
        view.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                view.setVisibility(View.INVISIBLE);
            }
        }, 2000);
    }

    public void finish() {
        super.finish();
        this.mLive2dManager.releaseModel();
        this.mLive2dManager.releaseView();
    }
}
