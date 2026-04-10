package jp.co.a_tm.moeyu;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import jp.co.a_tm.moeyu.api.MoeyuAPIClient.GachaCoin;
import jp.co.a_tm.moeyu.api.MoeyuAPIException;
import jp.co.a_tm.moeyu.api.fragment.BillingFragment;
import jp.co.a_tm.moeyu.api.fragment.GachaFragment;
import jp.co.a_tm.moeyu.api.fragment.LoginFragment;
import jp.co.a_tm.moeyu.api.listener.GachaResultListener;
import jp.co.a_tm.moeyu.api.listener.UserDataListener;
import jp.co.a_tm.moeyu.api.model.GachaResult;
import jp.co.a_tm.moeyu.model.UserData;
import jp.co.a_tm.moeyu.util.Logger;
import jp.co.a_tm.moeyu.util.UserDataManager;

/**
 * 抽卡活动类
 * 负责实现抽卡功能，包括金币管理、抽卡动画、获取金币等
 */
public class GatyaActivity extends BaseActivity {
    /** 黄金金币 SKU */
    private static final String SKU_GOLD_COIN_10 = "gold_coin_10";
    /** 黄金金币 SKU */
    private static final String SKU_GOLD_COIN_3 = "gold_coin_3";
    /** 白金金币 SKU */
    private static final String SKU_PLATINUM_COIN_1 = "platinum_coin_1";
    /** 日志标签 */
    public static final String TAG = GatyaActivity.class.getSimpleName();
    /** 是否开始输入金币 */
    public boolean isInputCoinStart;
    /** 是否点击了转盘 */
    public boolean isTotteClick;
    /** 是否移动了转盘 */
    public boolean isTotteMove;
    /** 青铜金币数量 */
    private int mBronzeCoinCount;
    /** 青铜金币数量显示 */
    private TextView mBronzeQuantity;
    /** 购买对话框 */
    public Dialog mBuyDialog;
    /** 当前角度 */
    public float mCurrentDegrees;
    /** 抽卡转盘视图 */
    public ImageView mGatyaponView;
    /** 黄金金币数量 */
    public int mGoldCoinCount;
    /** 黄金金币数量显示 */
    private TextView mGoldQuantity;
    /** 处理器 */
    private Handler mHandler = new Handler();
    /** 指示器 */
    public View mIndicator;
    /** 输入金币视图 */
    public ImageView mInputCoinImageView;
    /** 白金金币数量 */
    private int mPlatinumCoinCount;
    /** 白金金币数量显示 */
    private TextView mPlatinumQuantity;
    /** 前用户数据 */
    public UserData mPreUserData;
    /** 上次触摸点 */
    public PointF mPreviousTouchF;
    /** 选择的金币 */
    public GachaCoin mSelectedCoin = GachaCoin.None;
    /** 转盘中心绝对坐标 */
    public PointF mTotteCenterAbsoluteF;
    /** 转盘中心相对坐标 */
    public PointF mTotteCenterRelativeF;
    /** 转盘视图 */
    public ImageView mTotteImageView;
    /** 转盘矩阵 */
    public Matrix mTotteMatrix;
    /** 用户ID */
    public String mUserId;

    /**
     * 创建时回调方法
     * 初始化抽卡界面
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("GatyaAcitivity");
        setContentView(R.layout.activity_gatya);
        this.mIndicator = findViewById(R.id.indicator);
        this.mBronzeQuantity = (TextView) findViewById(R.id.bronze_quantity);
        this.mGoldQuantity = (TextView) findViewById(R.id.gold_quantity);
        this.mPlatinumQuantity = (TextView) findViewById(R.id.platinum_quantity);
        this.mInputCoinImageView = (ImageView) findViewById(R.id.img_gatya_inputcoin);
        UserDataManager dataManager = new UserDataManager(this);
        if (dataManager.isSavedUserData()) {
            this.mUserId = dataManager.loadUserData().getUserId();
            executeLogin(this.mUserId);
            return;
        }
        finish();
    }

    /**
     * 恢复时回调方法
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 新意图回调方法
     *
     * @param intent 新意图
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        executeLogin(this.mUserId);
    }

    /**
     * 启动时回调方法
     */
    @Override
    protected void onStart() {
        super.onStart();
        this.isTotteClick = false;
        this.isTotteMove = false;
        this.isInputCoinStart = false;
        this.mCurrentDegrees = 0.0f;
        this.mGatyaponView = (ImageView) findViewById(R.id.img_gatya_gatyapon);
        this.mGatyaponView.setVisibility(View.INVISIBLE);
    }

    /**
     * 销毁时回调方法
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mInputCoinImageView.setImageDrawable(null);
        this.mTotteImageView.setImageDrawable(null);
        Logger.d(getClass().getSimpleName() + " onDestroy()");
    }

    /**
     * 窗口焦点改变回调方法
     *
     * @param hasFocus 是否获得焦点
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !this.isTotteMove) {
            setTotteImageView();
        }
    }

    /**
     * 绘制金币数量
     *
     * @param userData 用户数据
     */
    public void drawCoinQuantity(UserData userData) {
        this.mBronzeCoinCount = userData.getBronzeCoin();
        this.mGoldCoinCount = userData.getGoldCoin();
        this.mPlatinumCoinCount = userData.getPlatinumCoin();
        this.mBronzeQuantity.setText("" + userData.getBronzeCoin());
        this.mGoldQuantity.setText("" + this.mGoldCoinCount);
        this.mPlatinumQuantity.setText("" + this.mPlatinumCoinCount);
        if (this.mBronzeCoinCount > 0) {
            setCoin(GachaCoin.BRONZE);
        } else if (this.mGoldCoinCount > 0) {
            setCoin(GachaCoin.GOLD);
        } else if (this.mPlatinumCoinCount > 0) {
            setCoin(GachaCoin.PLATINUM);
        } else {
            setCoin(GachaCoin.None);
        }
    }

    /**
     * 获取角度
     *
     * @param current 当前坐标
     * @return 角度
     */
    public float getDegrees(PointF current) {
        PointF previousRelative = new PointF(this.mPreviousTouchF.x - this.mTotteCenterAbsoluteF.x, this.mPreviousTouchF.y - this.mTotteCenterAbsoluteF.y);
        PointF currentRelative = new PointF(current.x - this.mTotteCenterAbsoluteF.x, current.y - this.mTotteCenterAbsoluteF.y);
        return (float) ((Math.atan2((double) ((previousRelative.x * currentRelative.y) - (previousRelative.y * currentRelative.x)), (double) ((previousRelative.x * currentRelative.x) + (previousRelative.y * currentRelative.y))) * 180.0d) / 3.141592653589793d);
    }

    /**
     * 设置转盘视图
     */
    private void setTotteImageView() {
        this.mTotteImageView = (ImageView) findViewById(R.id.totte);
        this.mTotteImageView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Logger.d("totteDown");
                        if (GatyaActivity.this.mSelectedCoin != GachaCoin.None) {
                            GatyaActivity.this.mTotteCenterRelativeF = new PointF((float) (GatyaActivity.this.mTotteImageView.getWidth() / 2), (float) (GatyaActivity.this.mTotteImageView.getHeight() / 2));
                            Rect rect = new Rect();
                            GatyaActivity.this.mTotteImageView.getGlobalVisibleRect(rect);
                            GatyaActivity.this.mTotteCenterAbsoluteF = new PointF(((float) rect.left) + GatyaActivity.this.mTotteCenterRelativeF.x, ((float) rect.top) + GatyaActivity.this.mTotteCenterRelativeF.y);
                            GatyaActivity.this.mPreviousTouchF = new PointF(event.getRawX(), event.getRawY());
                            GatyaActivity.this.isTotteClick = true;
                            break;
                        }
                        return false;
                    case MotionEvent.ACTION_UP:
                        Logger.d("totteUp");
                        if (360.0f <= GatyaActivity.this.mCurrentDegrees && GatyaActivity.this.mGatyaponView.getVisibility() == View.INVISIBLE) {
                            GatyaActivity.this.mCurrentDegrees = 360.0f;
                            GatyaActivity.this.mTotteMatrix = new Matrix();
                            GatyaActivity.this.mTotteMatrix.setRotate(0.0f, GatyaActivity.this.mTotteCenterRelativeF.x, GatyaActivity.this.mTotteCenterRelativeF.y);
                            GatyaActivity.this.mTotteImageView.setImageMatrix(GatyaActivity.this.mTotteMatrix);
                            Animation anim = AnimationUtils.loadAnimation(GatyaActivity.this, R.anim.output_gachapon);
                            anim.setAnimationListener(new AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    GatyaActivity.this.executeGachaTask(GatyaActivity.this.mUserId, GatyaActivity.this.mSelectedCoin);
                                }
                            });
                            GatyaActivity.this.mGatyaponView.setVisibility(View.VISIBLE);
                            GatyaActivity.this.mGatyaponView.startAnimation(anim);
                        }
                        GatyaActivity.this.isTotteClick = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (GatyaActivity.this.isTotteClick) {
                            if (GatyaActivity.this.mInputCoinImageView.getVisibility() == View.VISIBLE) {
                                GatyaActivity.this.isInputCoinStart = true;
                                GatyaActivity.this.mInputCoinImageView.startAnimation(AnimationUtils.loadAnimation(GatyaActivity.this, R.anim.input_coin));
                                GatyaActivity.this.mInputCoinImageView.setVisibility(View.INVISIBLE);
                            }
                            if (!GatyaActivity.this.isTotteMove) {
                                GatyaActivity.this.isTotteMove = true;
                            }
                            if (GatyaActivity.this.mCurrentDegrees < 360.0f) {
                                PointF currentF = new PointF(event.getRawX(), event.getRawY());
                                float degrees = GatyaActivity.this.getDegrees(currentF);
                                if (0.0f < degrees) {
                                    GatyaActivity.this.mCurrentDegrees += degrees;
                                    GatyaActivity.this.mTotteMatrix = new Matrix();
                                    GatyaActivity.this.mTotteMatrix.setRotate(GatyaActivity.this.mCurrentDegrees, GatyaActivity.this.mTotteCenterRelativeF.x, GatyaActivity.this.mTotteCenterRelativeF.y);
                                    GatyaActivity.this.mTotteImageView.setImageMatrix(GatyaActivity.this.mTotteMatrix);
                                }
                                GatyaActivity.this.mPreviousTouchF = currentF;
                                break;
                            }
                        }
                        break;
                }
                return true;
            }
        });
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
     * 物品按钮点击事件
     *
     * @param view 点击的视图
     */
    public void toItemClick(View view) {
        toItemCollection();
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
     * 设置金币类型
     *
     * @param type 金币类型
     */
    private void setCoin(GachaCoin type) {
        if (!this.isInputCoinStart) {
            this.mSelectedCoin = type;
            String resName = "";
            switch (type) {
                case BRONZE:
                    resName = "coin_bronze";
                    break;
                case GOLD:
                    resName = "coin_gold";
                    break;
                case PLATINUM:
                    resName = "coin_platinum";
                    break;
                case None:
                    this.mInputCoinImageView.setVisibility(View.INVISIBLE);
                    return;
            }
            this.mInputCoinImageView.setImageResource(getResources().getIdentifier(resName, "drawable", getPackageName()));
            this.mInputCoinImageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 青铜金币按钮点击事件
     *
     * @param view 点击的视图
     */
    public void blonzButtonClick(View view) {
        if (this.mBronzeCoinCount > 0 && !this.isInputCoinStart) {
            setCoin(GachaCoin.BRONZE);
        }
    }

    /**
     * 黄金金币按钮点击事件
     *
     * @param view 点击的视图
     */
    public void goldButtonClick(View view) {
        if (this.mGoldCoinCount > 0 && !this.isInputCoinStart) {
            setCoin(GachaCoin.GOLD);
            this.mSelectedCoin = GachaCoin.GOLD;
        }
    }

    /**
     * 白金金币按钮点击事件
     *
     * @param view 点击的视图
     */
    public void platinumButtonClick(View view) {
        if (this.mPlatinumCoinCount > 0 && !this.isInputCoinStart) {
            setCoin(GachaCoin.PLATINUM);
        }
    }

    /**
     * 获取黄金金币按钮点击事件（本地化，直接赠送）
     *
     * @param view 点击的视图
     */
    public void buyGoldButtonClick(View view) {
        this.mBuyDialog = new Dialog(this, R.style.clear_dialog);
        this.mBuyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.mBuyDialog.setContentView(R.layout.dialog_gatya_gold);
        this.mBuyDialog.setCanceledOnTouchOutside(true);
        ImageButton buyButton = (ImageButton) this.mBuyDialog.findViewById(R.id.imgbutton_gatya_gold_panel_buy);
        ImageButton checkTopBoxButton = (ImageButton) this.mBuyDialog.findViewById(R.id.imgbutton_gatya_gold_panel_check_top_box);
        final ImageButton checkTopMarkButton = (ImageButton) this.mBuyDialog.findViewById(R.id.imgbutton_gatya_gold_panel_check_top_mark);
        ImageButton checkUnderBoxButton = (ImageButton) this.mBuyDialog.findViewById(R.id.imgbutton_gatya_gold_panel_check_under_box);
        final ImageButton checkUnderMarkButton = (ImageButton) this.mBuyDialog.findViewById(R.id.imgbutton_gatya_gold_panel_check_under_mark);
        ((ImageButton) this.mBuyDialog.findViewById(R.id.imgbutton_gatya_gold_panel_cancel)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                GatyaActivity.this.mBuyDialog.dismiss();
            }
        });
        buyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String productId;
                int c;
                if (checkTopMarkButton.getVisibility() == View.VISIBLE) {
                    productId = GatyaActivity.SKU_GOLD_COIN_10;
                    c = 10;
                } else {
                    productId = GatyaActivity.SKU_GOLD_COIN_3;
                    c = 3;
                }
                if (GatyaActivity.this.mGoldCoinCount + c > CoinController.MAX_COIN) {
                    GatyaActivity.this.showLimitCoinCountMessage();
                    return;
                }
                // 本地化赠送货币
                executeBilling(productId);
                GatyaActivity.this.mBuyDialog.dismiss();
            }
        });
        checkTopBoxButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                checkTopMarkButton.setVisibility(View.VISIBLE);
                checkUnderMarkButton.setVisibility(View.INVISIBLE);
            }
        });
        checkUnderBoxButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                checkTopMarkButton.setVisibility(View.INVISIBLE);
                checkUnderMarkButton.setVisibility(View.VISIBLE);
            }
        });
        this.mBuyDialog.show();
    }

    /**
     * 获取白金金币按钮点击事件（本地化，直接赠送）
     *
     * @param view 点击的视图
     */
    public void buyPlatinumButtonClick(View view) {
        if (this.mPlatinumCoinCount >= CoinController.MAX_COIN) {
            showLimitCoinCountMessage();
            return;
        }
        this.mBuyDialog = new Dialog(this, R.style.clear_dialog);
        this.mBuyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.mBuyDialog.setContentView(R.layout.dialog_gatya_platinum);
        this.mBuyDialog.setCanceledOnTouchOutside(true);
        ImageButton buyButton = (ImageButton) this.mBuyDialog.findViewById(R.id.imgbutton_gatya_platinum_buy);
        ((ImageButton) this.mBuyDialog.findViewById(R.id.imgbutton_gatya_platinum_cancel)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                GatyaActivity.this.mBuyDialog.dismiss();
            }
        });
        buyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 本地化赠送货币
                executeBilling(GatyaActivity.SKU_PLATINUM_COIN_1);
                GatyaActivity.this.mBuyDialog.dismiss();
            }
        });
        this.mBuyDialog.show();
    }

    /**
     * 显示金币数量超过限制消息
     */
    public void showLimitCoinCountMessage() {
        Toast.makeText(this, "これ以上コインを購入できません", Toast.LENGTH_SHORT).show();
    }

    /**
     * 执行本地化计费
     * 通过BillingFragment异步赠送货币，完成后刷新显示
     *
     * @param productId 产品ID
     */
    private void executeBilling(String productId) {
        this.mIndicator.setVisibility(View.VISIBLE);
        ((BillingFragment) getSupportFragmentManager().findFragmentById(R.id.billing_fragment))
                .billing(productId, new UserDataListener() {
                    @Override
                    public void onSuccess(UserData userData) {
                        GatyaActivity.this.mIndicator.setVisibility(View.INVISIBLE);
                        GatyaActivity.this.drawCoinQuantity(userData);
                        Toast.makeText(GatyaActivity.this, "コインを取得しました！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(MoeyuAPIException e) {
                        GatyaActivity.this.mIndicator.setVisibility(View.INVISIBLE);
                        Toast.makeText(GatyaActivity.this, "コインの取得に失敗しました", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {
                        GatyaActivity.this.mIndicator.setVisibility(View.INVISIBLE);
                    }
                });
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
                GatyaActivity.this.drawCoinQuantity(userData);
                GatyaActivity.this.mPreUserData = userData;
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
     * 执行抽卡任务
     *
     * @param userId 用户ID
     * @param coin 金币类型
     */
    private void executeGachaTask(String userId, GachaCoin coin) {
        ((GachaFragment) getSupportFragmentManager().findFragmentById(R.id.gacha_fragment)).gacha(userId, coin, new GachaResultListener() {
            @Override
            public void onSuccess(GachaResult result) {
                Intent data = new Intent();
                data.putExtra(BaseActivity.EXTRA_NEXT_ACTIVITY, BaseActivity.NEXT_ACTIVITY_GACHA_RESULT);
                data.putExtra(GatyaResultActivity.EXTRA_PRE_USER_DATA, GatyaActivity.this.mPreUserData);
                data.putExtra(GatyaResultActivity.EXTRA_GACHA_RESULT, result);
                // 使用 -1 作为 resultCode，与 Activity.RESULT_OK（值同为 -1）一致
                GatyaActivity.this.setResult(-1, data);
                GatyaActivity.this.finish();
            }

            @Override
            public void onError(MoeyuAPIException e) {
            }

            @Override
            public void onCancel() {
            }
        });
    }
}
