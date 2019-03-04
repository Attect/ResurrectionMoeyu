package jp.co.a_tm.moeyu.billing;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import jp.co.a_tm.moeyu.billing.BillingService.RequestPurchase;
import jp.co.a_tm.moeyu.billing.BillingService.RestoreTransactions;
import jp.co.a_tm.moeyu.billing.Consts.PurchaseState;
import jp.co.a_tm.moeyu.billing.Consts.ResponseCode;

public class ResponseHandler {
    private static final String TAG = "ResponseHandler";
    /* access modifiers changed from: private|static */
    public static PurchaseObserver sPurchaseObserver;

    public static synchronized void register(PurchaseObserver observer) {
        synchronized (ResponseHandler.class) {
            sPurchaseObserver = observer;
        }
    }

    public static synchronized void unregister(PurchaseObserver observer) {
        synchronized (ResponseHandler.class) {
            sPurchaseObserver = null;
        }
    }

    public static void checkBillingSupportedResponse(boolean supported) {
        if (sPurchaseObserver != null) {
            sPurchaseObserver.onBillingSupported(supported);
        }
    }

    public static void buyPageIntentResponse(PendingIntent pendingIntent, Intent intent) {
        if (sPurchaseObserver == null) {
            Log.d(TAG, "UI is not running");
        } else {
            sPurchaseObserver.startBuyPageActivity(pendingIntent, intent);
        }
    }

    public static void purchaseResponse(Context context, PurchaseState purchaseState, String productId, String orderId, long purchaseTime, String developerPayload, String signedData, String signature, String notificationId) {
        final PurchaseState purchaseState2 = purchaseState;
        final String str = productId;
        final long j = purchaseTime;
        final String str2 = developerPayload;
        final String str3 = signedData;
        final String str4 = signature;
        final String str5 = notificationId;
        new Thread(new Runnable() {
            public void run() {
                synchronized (ResponseHandler.class) {
                    if (ResponseHandler.sPurchaseObserver != null) {
                        ResponseHandler.sPurchaseObserver.postPurchaseStateChange(purchaseState2, str, 1, j, str2, str3, str4, str5);
                    }
                }
            }
        }).start();
    }

    public static void responseCodeReceived(Context context, RequestPurchase request, ResponseCode responseCode) {
        if (sPurchaseObserver != null) {
            sPurchaseObserver.onRequestPurchaseResponse(request, responseCode);
        }
    }

    public static void responseCodeReceived(Context context, RestoreTransactions request, ResponseCode responseCode) {
        if (sPurchaseObserver != null) {
            sPurchaseObserver.onRestoreTransactionsResponse(request, responseCode);
        }
    }
}
