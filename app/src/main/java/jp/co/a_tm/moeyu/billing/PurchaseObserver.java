package jp.co.a_tm.moeyu.billing;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Handler;
import android.util.Log;
import java.lang.reflect.Method;
import jp.co.a_tm.moeyu.billing.BillingService.RequestPurchase;
import jp.co.a_tm.moeyu.billing.BillingService.RestoreTransactions;
import jp.co.a_tm.moeyu.billing.Consts.PurchaseState;
import jp.co.a_tm.moeyu.billing.Consts.ResponseCode;

public abstract class PurchaseObserver {
    private static final Class[] START_INTENT_SENDER_SIG = new Class[]{IntentSender.class, Intent.class, Integer.TYPE, Integer.TYPE, Integer.TYPE};
    private static final String TAG = "PurchaseObserver";
    private final Activity mActivity;
    private final Handler mHandler;
    private Method mStartIntentSender;
    private Object[] mStartIntentSenderArgs = new Object[5];

    public abstract void onBillingSupported(boolean z);

    public abstract void onPurchaseStateChange(PurchaseState purchaseState, String str, int i, long j, String str2, String str3, String str4, String str5);

    public abstract void onRequestPurchaseResponse(RequestPurchase requestPurchase, ResponseCode responseCode);

    public abstract void onRestoreTransactionsResponse(RestoreTransactions restoreTransactions, ResponseCode responseCode);

    public PurchaseObserver(Activity activity, Handler handler) {
        this.mActivity = activity;
        this.mHandler = handler;
        initCompatibilityLayer();
    }

    private void initCompatibilityLayer() {
        try {
            this.mStartIntentSender = this.mActivity.getClass().getMethod("startIntentSender", START_INTENT_SENDER_SIG);
        } catch (SecurityException e) {
            this.mStartIntentSender = null;
        } catch (NoSuchMethodException e2) {
            this.mStartIntentSender = null;
        }
    }

    /* access modifiers changed from: 0000 */
    public void startBuyPageActivity(PendingIntent pendingIntent, Intent intent) {
        if (this.mStartIntentSender != null) {
            try {
                this.mStartIntentSenderArgs[0] = pendingIntent.getIntentSender();
                this.mStartIntentSenderArgs[1] = intent;
                this.mStartIntentSenderArgs[2] = Integer.valueOf(0);
                this.mStartIntentSenderArgs[3] = Integer.valueOf(0);
                this.mStartIntentSenderArgs[4] = Integer.valueOf(0);
                this.mStartIntentSender.invoke(this.mActivity, this.mStartIntentSenderArgs);
                return;
            } catch (Exception e) {
                Log.e(TAG, "error starting activity", e);
                return;
            }
        }
        try {
            pendingIntent.send(this.mActivity, 0, intent);
        } catch (CanceledException e2) {
            Log.e(TAG, "error starting activity", e2);
        }
    }

    /* access modifiers changed from: 0000 */
    public void postPurchaseStateChange(PurchaseState purchaseState, String itemId, int quantity, long purchaseTime, String developerPayload, String signedData, String signature, String notificationId) {
        final PurchaseState purchaseState2 = purchaseState;
        final String str = itemId;
        final int i = quantity;
        final long j = purchaseTime;
        final String str2 = developerPayload;
        final String str3 = signedData;
        final String str4 = signature;
        final String str5 = notificationId;
        this.mHandler.post(new Runnable() {
            public void run() {
                PurchaseObserver.this.onPurchaseStateChange(purchaseState2, str, i, j, str2, str3, str4, str5);
            }
        });
    }
}
