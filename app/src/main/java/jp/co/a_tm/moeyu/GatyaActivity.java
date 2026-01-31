package jp.co.a_tm.moeyu;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import jp.co.a_tm.moeyu.api.MoeyuAPIClient.GachaCoin;
import jp.co.a_tm.moeyu.api.MoeyuAPIException;
import jp.co.a_tm.moeyu.api.fragment.GachaFragment;
import jp.co.a_tm.moeyu.api.fragment.LoginFragment;
import jp.co.a_tm.moeyu.api.listener.GachaResultListener;
import jp.co.a_tm.moeyu.api.listener.UserDataListener;
import jp.co.a_tm.moeyu.api.model.GachaResult;
import jp.co.a_tm.moeyu.billing.BillingService;
import jp.co.a_tm.moeyu.billing.BillingService.RequestPurchase;
import jp.co.a_tm.moeyu.billing.BillingService.RestoreTransactions;
import jp.co.a_tm.moeyu.billing.Consts.PurchaseState;
import jp.co.a_tm.moeyu.billing.Consts.ResponseCode;
import jp.co.a_tm.moeyu.billing.PurchaseObserver;
import jp.co.a_tm.moeyu.billing.ResponseHandler;
import jp.co.a_tm.moeyu.model.UserData;
import jp.co.a_tm.moeyu.util.Logger;
import jp.co.a_tm.moeyu.util.UserDataManager;

/**
 * 抽卡活动类
 * 负责实现抽卡功能，包括金币管理、抽卡动画、购买金币等
 */
public class GatyaActivity extends BaseActivity {
    /**
     * 最大金币数量
     */
    private static final int MAX_COIN_COUNT = 99;
    /** 黄金金币 SKU */
    private static final String SKU_GOLD_COIN_10 = "gold_coin_10";
    /** 黄金金币 SKU */
    private static final String SKU_GOLD_COIN_3 = "gold_coin_3";
    /** 白金金币 SKU */
    private static final String SKU_PLATINUM_COIN_1 = "platinum_coin_1";
    /** 日志标签 */
    /* access modifiers changed from: private|static|final */
    public static final String TAG = GatyaActivity.class.getSimpleName();
    /** 是否开始输入金币 */
    /* access modifiers changed from: private */
    public boolean isInputCoinStart;
    /** 是否点击了转盘 */
    /* access modifiers changed from: private */
    public boolean isTotteClick;
    /** 是否移动了转盘 */
    /* access modifiers changed from: private */
    public boolean isTotteMove;
    /** 金币服务 */
    private BillingService mBillingService;
    /** 青铜金币数量 */
    private int mBronzeCoinCount;
    /** 青铜金币数量显示 */
    private TextView mBronzeQuantity;
    /** 购买对话框 */
    /* access modifiers changed from: private */
    public Dialog mBuyDialog;
    /** 当前角度 */
    /* access modifiers changed from: private */
    public float mCurrentDegrees;
    /** 抽卡转盘视图 */
    /* access modifiers changed from: private */
    public ImageView mGatyaponView;
    /** 黄金金币数量 */
    /* access modifiers changed from: private */
    public int mGoldCoinCount;
    /** 黄金金币数量显示 */
    private TextView mGoldQuantity;
    /** 处理器 */
    private Handler mHandler = new Handler();
    /** 指示器 */
    /* access modifiers changed from: private */
    public View mIndicator;
    /** 输入金币视图 */
    /* access modifiers changed from: private */
    public ImageView mInputCoinImageView;
    /** 白金金币数量 */
    private int mPlatinumCoinCount;
    /** 白金金币数量显示 */
    private TextView mPlatinumQuantity;
    /** 前用户数据 */
    /* access modifiers changed from: private */
    public UserData mPreUserData;
    /** 上次触摸点 */
    /* access modifiers changed from: private */
    public PointF mPreviousTouchF;
    /** 购买观察者 */
    private MoeyuPurchaseObserver mPurchaseObserver;
    /** 选择的金币 */
    /* access modifiers changed from: private */
    public GachaCoin mSelectedCoin = GachaCoin.None;
    /** 转盘中心绝对坐标 */
    /* access modifiers changed from: private */
    public PointF mTotteCenterAbsoluteF;
    /** 转盘中心相对坐标 */
    /* access modifiers changed from: private */
    public PointF mTotteCenterRelativeF;
    /** 转盘视图 */
    /* access modifiers changed from: private */
    public ImageView mTotteImageView;
    /** 转盘矩阵 */
    /* access modifiers changed from: private */
    public Matrix mTotteMatrix;
    /** 用户ID */
    /* access modifiers changed from: private */
    public String mUserId;

    /**
     * Moeyu购买观察者类
     * 处理购买相关的回调
     */
    private class MoeyuPurchaseObserver extends PurchaseObserver {
        /**
         * 构造函数
         *
         * @param handler 处理器
         */
        public MoeyuPurchaseObserver(Handler handler) {
            super(GatyaActivity.this, handler);
        }

        /**
         * 交易支持回调
         *
         * @param supported 是否支持
         */
        @Override
        public void onBillingSupported(boolean supported) {
            Log.i(GatyaActivity.TAG, "supported: " + supported);
        }

        /**
         * 购买状态改变回调
         *
         * @param purchaseState    购买状态
         * @param itemId           商品ID
         * @param quantity         数量
         * @param purchaseTime     购买时间
         * @param developerPayload 开发者负载
         * @param signedData       签名数据
         * @param signature        签名
         * @param notificationId   通知ID
         */
        @Override
        public void onPurchaseStateChange(PurchaseState purchaseState, String itemId, int quantity, long purchaseTime, String developerPayload, String signedData, String signature, String notificationId) {
            Log.i(GatyaActivity.TAG, "onPurchaseStateChange() itemId: " + itemId + " " + purchaseState);
            if (purchaseState == PurchaseState.PURCHASED) {
                GatyaActivity.this.mIndicator.setVisibility(View.INVISIBLE);
                Toast.makeText(GatyaActivity.this, "購入完了！", 0).show();
                GatyaActivity.this.executeLogin(GatyaActivity.this.mUserId);
            }
        }

        /**
         * 请求购买响应回调
         *
         * @param request 请求
         * @param responseCode 响应码
         */
        @Override
        public void onRequestPurchaseResponse(RequestPurchase request, ResponseCode responseCode) {
            Log.d(GatyaActivity.TAG, request.mProductId + ": " + responseCode);
            if (responseCode == ResponseCode.RESULT_OK) {
                Log.i(GatyaActivity.TAG, "purchase was successfully sent to server");
                return;
            }
            GatyaActivity.this.mIndicator.setVisibility(View.INVISIBLE);
            if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
                Log.i(GatyaActivity.TAG, "user canceled purchase");
            }
        }

        /**
         * 恢复交易响应回调
         *
         * @param request 请求
         * @param responseCode 响应码
         */
        @Override
        public void onRestoreTransactionsResponse(RestoreTransactions request, ResponseCode responseCode) {
            if (responseCode == ResponseCode.RESULT_OK) {
                Log.d(GatyaActivity.TAG, "completed RestoreTransactions request");
            } else {
                Log.d(GatyaActivity.TAG, "RestoreTransactions error: " + responseCode);
            }
        }
    }

    /**
     * 访问器方法（供合成使用）
     *
     * @param x0 活动实例
     * @param x1 角度增量
     * @return 新的角度
     */
    static /* synthetic */ float access$916(GatyaActivity x0, float x1) {
        float f = x0.mCurrentDegrees + x1;
        x0.mCurrentDegrees = f;
        return f;
    }

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
        this.mPurchaseObserver = new MoeyuPurchaseObserver(this.mHandler);
        this.mBillingService = new BillingService();
        this.mBillingService.setContext(this);
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
        //        this.mTracker.trackPageView("ガチャ");
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
     *
     * @param savedInstanceState 保存的实例状态
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
        ResponseHandler.register(this.mPurchaseObserver);
    }

    /**
     * 停止时回调方法
     */
    @Override
    protected void onStop() {
        super.onStop();
        ResponseHandler.unregister(this.mPurchaseObserver);
    }

    /**
     * 销毁时回调方法
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mBillingService.unbind();
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
    /* access modifiers changed from: private */
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
    /* access modifiers changed from: private */
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
                    case 0:
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
                    case 1:
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
                    case 2:
                        if (GatyaActivity.this.isTotteClick) {
                            if (GatyaActivity.this.mInputCoinImageView.getVisibility() == 0) {
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
                                    GatyaActivity.access$916(GatyaActivity.this, degrees);
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
        toActivity(NEXT_ACTIVITY_BATH);
    }

    /**
     * 物品按钮点击事件
     *
     * @param view 点击的视图
     */
    public void toItemClick(View view) {
        toActivity(NEXT_ACTIVITY_ITEM_COLLECTION);
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
     * 购买黄金金币按钮点击事件
     *
     * @param view 点击的视图
     */
    public void buyGoldButtonClick(View view) {
        //        this.mTracker.trackEvent("ゴールドコイン購入選択", "", "", 1);
        this.mBuyDialog = new Dialog(this, R.style.clear_dialog);
        this.mBuyDialog.requestWindowFeature(1);
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
                //                GatyaActivity.this.mTracker.trackEvent("ゴールドコイン購入", "キャンセル", "", 1);
                GatyaActivity.this.mBuyDialog.dismiss();
            }
        });
        buyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String sku;
                int c;
                if (checkTopMarkButton.getVisibility() == 0) {
                    sku = GatyaActivity.SKU_GOLD_COIN_10;
                    c = 10;
                } else {
                    sku = GatyaActivity.SKU_GOLD_COIN_3;
                    c = 3;
                }
                if (GatyaActivity.this.mGoldCoinCount + c > 99) {
                    GatyaActivity.this.showLimitCoinCountMessage();
                    return;
                }
                switch (c) {
                    case 3:
                        //                        GatyaActivity.this.mTracker.trackEvent("ゴールドコイン購入", "3枚購入", "", 1);
                        break;
                    case 10:
                        //                        GatyaActivity.this.mTracker.trackEvent("ゴールドコイン購入", "10枚購入", "", 1);
                        break;
                }
                if (!GatyaActivity.this.requestPurchase(sku)) {
                    GatyaActivity.this.showCantPurchaseMessage();
                }
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
     * 购买白金金币按钮点击事件
     *
     * @param view 点击的视图
     */
    public void buyPlatinumButtonClick(View view) {
        if (this.mPlatinumCoinCount == 99) {
            showLimitCoinCountMessage();
            return;
        }
        //        this.mTracker.trackEvent("プラチナコイン購入選択", "", "", 1);
        this.mBuyDialog = new Dialog(this, R.style.clear_dialog);
        this.mBuyDialog.requestWindowFeature(1);
        this.mBuyDialog.setContentView(R.layout.dialog_gatya_platinum);
        this.mBuyDialog.setCanceledOnTouchOutside(true);
        ImageButton buyButton = (ImageButton) this.mBuyDialog.findViewById(R.id.imgbutton_gatya_platinum_buy);
        ((ImageButton) this.mBuyDialog.findViewById(R.id.imgbutton_gatya_platinum_cancel)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //                GatyaActivity.this.mTracker.trackEvent("プラチナコイン購入", "キャンセル", "", 1);
                GatyaActivity.this.mBuyDialog.dismiss();
            }
        });
        buyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //                GatyaActivity.this.mTracker.trackEvent("プラチナコイン購入", "購入", "", 1);
                if (!GatyaActivity.this.requestPurchase(GatyaActivity.SKU_PLATINUM_COIN_1)) {
                    GatyaActivity.this.showCantPurchaseMessage();
                }
                GatyaActivity.this.mBuyDialog.dismiss();
            }
        });
        this.mBuyDialog.show();
    }

    /**
     * 显示无法购买消息
     */
    /* access modifiers changed from: private */
    public void showCantPurchaseMessage() {
        Toast.makeText(this, "お客様の端末では購入ができません", 0).show();
    }

    /**
     * 显示金币数量超过限制消息
     */
    /* access modifiers changed from: private */
    public void showLimitCoinCountMessage() {
        Toast.makeText(this, "これ以上コインを購入できません", 0).show();
    }

    /**
     * 请求购买
     *
     * @param sku 商品SKU
     * @return 是否请求成功
     */
    /* access modifiers changed from: private */
    private boolean requestPurchase(String sku) {
        boolean isRequestSuccess = false;
        this.mIndicator.setVisibility(View.VISIBLE);
        if (this.mBillingService.checkBillingSupported()) {
            isRequestSuccess = this.mBillingService.requestPurchase(sku, this.mUserId);
        }
        if (!isRequestSuccess) {
            this.mIndicator.setVisibility(View.INVISIBLE);
        }
        return isRequestSuccess;
    }

    /**
     * 执行登录操作
     *
     * @param userId 用户ID
     */
    /* access modifiers changed from: private */
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
    /* access modifiers changed from: private */
    private void executeGachaTask(String userId, GachaCoin coin) {
        ((GachaFragment) getSupportFragmentManager().findFragmentById(R.id.gacha_fragment)).gacha(userId, coin, new GachaResultListener() {
            @Override
            public void onSuccess(GachaResult result) {
                Intent data = new Intent();
                data.putExtra("extra_next_activity", 5);
                data.putExtra(GatyaResultActivity.EXTRA_PRE_USER_DATA, GatyaActivity.this.mPreUserData);
                data.putExtra(GatyaResultActivity.EXTRA_GACHA_RESULT, result);
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
