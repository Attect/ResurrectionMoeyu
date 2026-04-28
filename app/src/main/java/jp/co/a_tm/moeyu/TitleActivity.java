package jp.co.a_tm.moeyu;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import jp.co.a_tm.moeyu.api.MoeyuAPIException;
import jp.co.a_tm.moeyu.api.fragment.LoginFragment;
import jp.co.a_tm.moeyu.api.fragment.SignupFragment;
import jp.co.a_tm.moeyu.api.listener.UserDataListener;
import jp.co.a_tm.moeyu.model.UserData;
import jp.co.a_tm.moeyu.util.Logger;
import jp.co.a_tm.moeyu.util.UserDataManager;

/**
 * 标题活动类
 * 负责显示应用标题界面，处理用户登录/注册和页面跳转
 */
public class TitleActivity extends BaseActivity {
    /**
     * 浴室呼叫音乐播放器
     */
    private MediaPlayer mBathCall = new MediaPlayer();
    /** 用户ID */
    public String mUserId;

    /**
     * 初始化数据任务类
     * 异步初始化应用数据
     */
    private class InitializeDataTask {
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final Handler handler = new Handler(Looper.getMainLooper());

        private InitializeDataTask() {
        }

        public void execute() {
            onPreExecute();
            executor.execute(() -> {
                try {
                    TitleActivity.this.initializeData();
                    new PreferencesHelper(TitleActivity.this.getApplicationContext()).setInitBoot(false);
                } catch (Exception e) {
                    Logger.e("TitleActivity", "初始化数据失败", e);
                } finally {
                    executor.shutdown();
                }
                handler.post(() -> onPostExecute(null));
            });
        }

        private void onPreExecute() {
            TitleActivity.this.findViewById(R.id.indicator).setVisibility(View.VISIBLE);
        }

        private void onPostExecute(Void result) {
            TitleActivity.this.findViewById(R.id.indicator).setVisibility(View.INVISIBLE);
            TitleActivity.this.login();
        }
    }

    /**
     * 创建时回调方法
     * 初始化标题界面
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);

        // 设置界面边距和背景色
        findViewById(R.id.layout_top).setPadding(0, MainActivity.FIX_HEIGHT / 2, 0, MainActivity.FIX_HEIGHT / 2);
        findViewById(R.id.layout_top).setBackgroundColor(Color.BLACK);
    }

    /**
     * 恢复时回调方法
     * 处理活动恢复时的登录逻辑
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onResume() {
        super.onResume();
        MoeyuApplication application = (MoeyuApplication) getApplication();
        if (application.isFirstRun()) {
            application.setFirstRun(false);
            if (new PreferencesHelper(this).isInitBoot()) {
                // 首次运行且需要初始化数据时执行异步任务
                new InitializeDataTask().execute();
                return;
            } else {
                // 不需要初始化可以直接登录
                login();
                return;
            }
        }
        // 加载保存的用户数据
        UserDataManager dataManager = new UserDataManager(this);
        if (dataManager.isSavedUserData()) {
            this.mUserId = dataManager.loadUserData().getUserId();
        }
    }

    /**
     * 初始化数据
     * 旧版会批量解密所有语音文件，现已改为按需解密。
     * 保留此方法以兼容首次启动流程。
     */
    private void initializeData() {
        // 语音文件改为按需解密（VoiceManager#getVoiceFileDescripter 中即时解密）
        // 不再首次启动时批量解密全部 352 个文件，大幅缩短首次启动时间
    }

    /**
     * 创建选项菜单
     *
     * @param menu 菜单
     * @return 是否继续处理
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * 选项菜单点击事件
     *
     * @param item 菜单项
     * @return 是否处理
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_terms) { /*2131624240*/
            showTerms();
        } else if (itemId == R.id.menu_inquiry) { /*2131624241*/
            sendInquiryMail();
        }
        return true;
    }

    /**
     * 键盘按下事件
     *
     * @param keyCode 按键代码
     * @param event   按键事件
     * @return 是否处理
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != KeyEvent.KEYCODE_BACK) {
            return super.onKeyDown(keyCode, event);
        }
        exit();
        return true;
    }

    /**
     * 显示服务条款
     */
    private void showTerms() {
        startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://www.moe-yu.com/kiyaku.html")));
    }

    /**
     * 发送咨询邮件
     */
    private void sendInquiryMail() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SENDTO");
        intent.setData(Uri.parse("mailto:moe-yu_support@a-tm.co.jp"));
        intent.putExtra("android.intent.extra.SUBJECT", "用户咨询 " + this.mUserId);
        startActivity(intent);
    }

    /**
     * 执行登录操作
     * 根据是否有保存用户数据决定是登录还是注册
     */
    public void login() {
        UserDataManager dataManager = new UserDataManager(this);
        if (dataManager.isSavedUserData()) {
            this.mUserId = dataManager.loadUserData().getUserId();
            executeLogin(this.mUserId);
            return;
        }
        executeSignup();
    }

    /**
     * 获取文件描述符
     * 配置媒体播放器的音频数据源
     *
     * @param player 播放器
     * @param str 文件名
     */
    private void getFileDescriptor(MediaPlayer player, String str) {
        try {
            player.setDataSource(openFileInput(str).getFD());
            player.prepare();
        } catch (IllegalArgumentException e1) {
            Logger.e("TitleActivity", "设置播放器数据源失败", e1);
        } catch (IllegalStateException e12) {
            Logger.e("TitleActivity", "设置播放器数据源失败", e12);
        } catch (FileNotFoundException e13) {
            Logger.e("TitleActivity", "设置播放器数据源失败", e13);
        } catch (IOException e14) {
            Logger.e("TitleActivity", "设置播放器数据源失败", e14);
        }
    }

    /**
     * 浴室按钮点击事件
     * 播放浴室音乐并跳转到浴室页面
     *
     * @param view 点击的视图
     */
    public void toKonyokuClick(View view) {
        getFileDescriptor(this.mBathCall, String.format("%03d", new Object[]{Integer.valueOf(new Random().nextInt(3) + 3)}) + ".ogg");
        this.mBathCall.start();
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
     * 收藏房间按钮点击事件
     *
     * @param view 点击的视图
     */
    public void toCollectionRoomClick(View view) {
        toCollection();
    }

    /**
     * 推特按钮点击事件
     * 显示推特分享对话框
     *
     * @param view 点击的视图
     */
    public void toTwitterClick(View view) {
        new TweetDialog(this).show(this);
    }

    /**
     * 设置按钮点击事件
     *
     * @param view 点击的视图
     */
    public void toSetteiClick(View view) {
        toPreference();
    }

    /**
     * 执行登录操作
     *
     * @param userId 用户ID
     */
    private void executeLogin(String userId) {
        ((LoginFragment) getSupportFragmentManager().findFragmentById(R.id.login_fragment)).login(userId, new UserDataListener() {
            @Override
            public void onSuccess(UserData userData) {
                TitleActivity.this.titleCall();
                if (userData.hasBonus()) {
                    TitleActivity.this.findViewById(R.id.login_bonus).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(MoeyuAPIException e) {
            }

            @Override
            public void onCancel() {
            }
        }, false, false);
    }

    /**
     * 执行注册操作
     */
    private void executeSignup() {
        ((SignupFragment) getSupportFragmentManager().findFragmentById(R.id.signup_fragment)).signup(new UserDataListener() {
            @Override
            public void onSuccess(UserData userData) {
                TitleActivity.this.mUserId = userData.getUserId();
                TitleActivity.this.titleCall();
            }

            @Override
            public void onError(MoeyuAPIException e) {
            }

            @Override
            public void onCancel() {
            }
        });
    }

    /**
     * 登录奖励点击事件
     *
     * @param view 点击的视图
     */
    public void onLoginBonusClick(View view) {
        view.setVisibility(View.INVISIBLE);
    }

    /**
     * 标题呼叫音乐播放
     * 播放标题界面的语音
     */
    public void titleCall() {
        MediaPlayer mp = new MediaPlayer();
        getFileDescriptor(mp, "002_2b.ogg");
        mp.start();
    }
}
