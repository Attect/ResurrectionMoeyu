package jp.co.a_tm.moeyu.billing;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
//import com.android.vending.billing.IMarketBillingService;
//import com.android.vending.billing.IMarketBillingService.Stub;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import jp.co.a_tm.moeyu.api.MoeyuAPIClient;
import jp.co.a_tm.moeyu.api.MoeyuAPIException;
import jp.co.a_tm.moeyu.billing.Consts.PurchaseState;
import jp.co.a_tm.moeyu.billing.Consts.ResponseCode;
import jp.co.a_tm.moeyu.billing.Security.VerifiedPurchase;

public class BillingService extends Service implements ServiceConnection {
    private static final String TAG = "BillingService";
    /* access modifiers changed from: private|static */
    public static LinkedList<BillingRequest> mPendingRequests = new LinkedList();
    /* access modifiers changed from: private|static */
    public static HashMap<Long, BillingRequest> mSentRequests = new HashMap();
    /* access modifiers changed from: private|static */
//    public static IMarketBillingService mService;

    abstract class BillingRequest {
        protected long mRequestId;
        private final int mStartId;

        public abstract long run() throws RemoteException;

        public BillingRequest(int startId) {
            this.mStartId = startId;
        }

        public int getStartId() {
            return this.mStartId;
        }

        public boolean runRequest() {
            if (runIfConnected()) {
                return true;
            }
            if (!BillingService.this.bindToMarketBillingService()) {
                return false;
            }
            BillingService.mPendingRequests.add(this);
            return true;
        }

        public boolean runIfConnected() {
            Log.d(BillingService.TAG, getClass().getSimpleName());
//            if (BillingService.mService != null) {
//                try {
//                    this.mRequestId = run();
//                    Log.d(BillingService.TAG, "request id: " + this.mRequestId);
//                    if (this.mRequestId >= 0) {
//                        BillingService.mSentRequests.put(Long.valueOf(this.mRequestId), this);
//                    }
//                    return true;
//                } catch (RemoteException e) {
//                    onRemoteException(e);
//                }
//            }
            return false;
        }

        /* access modifiers changed from: protected */
        public void onRemoteException(RemoteException e) {
            Log.w(BillingService.TAG, "remote billing service crashed");
//            BillingService.mService = null;
        }

        /* access modifiers changed from: protected */
        public void responseCodeReceived(ResponseCode responseCode) {
        }

        /* access modifiers changed from: protected */
        public Bundle makeRequestBundle(String method) {
            Bundle request = new Bundle();
            request.putString(Consts.BILLING_REQUEST_METHOD, method);
            request.putInt(Consts.BILLING_REQUEST_API_VERSION, 1);
            request.putString(Consts.BILLING_REQUEST_PACKAGE_NAME, BillingService.this.getPackageName());
            return request;
        }

        /* access modifiers changed from: protected */
        public void logResponseCode(String method, Bundle response) {
            Log.e(BillingService.TAG, method + " received " + ResponseCode.valueOf(response.getInt(Consts.BILLING_RESPONSE_RESPONSE_CODE)).toString());
        }
    }

    public class CheckBillingSupported extends BillingRequest {
        public /* bridge */ /* synthetic */ int getStartId() {
            return super.getStartId();
        }

        public /* bridge */ /* synthetic */ boolean runIfConnected() {
            return super.runIfConnected();
        }

        public /* bridge */ /* synthetic */ boolean runRequest() {
            return super.runRequest();
        }

        public CheckBillingSupported() {
            super(-1);
        }

        /* access modifiers changed from: protected */
        public long run() throws RemoteException {
//            int responseCode = BillingService.mService.sendBillingRequest(makeRequestBundle("CHECK_BILLING_SUPPORTED")).getInt(Consts.BILLING_RESPONSE_RESPONSE_CODE);
//            Log.i(BillingService.TAG, "CheckBillingSupported response code: " + ResponseCode.valueOf(responseCode));
//            ResponseHandler.checkBillingSupportedResponse(responseCode == ResponseCode.RESULT_OK.ordinal());
            return Consts.BILLING_RESPONSE_INVALID_REQUEST_ID;
        }
    }

    public class ConfirmNotifications extends BillingRequest {
        final String[] mNotifyIds;

        public /* bridge */ /* synthetic */ int getStartId() {
            return super.getStartId();
        }

        public /* bridge */ /* synthetic */ boolean runIfConnected() {
            return super.runIfConnected();
        }

        public /* bridge */ /* synthetic */ boolean runRequest() {
            return super.runRequest();
        }

        public ConfirmNotifications(int startId, String[] notifyIds) {
            super(startId);
            this.mNotifyIds = notifyIds;
        }

        /* access modifiers changed from: protected */
        public long run() throws RemoteException {
//            Bundle request = makeRequestBundle("CONFIRM_NOTIFICATIONS");
//            request.putStringArray(Consts.BILLING_REQUEST_NOTIFY_IDS, this.mNotifyIds);
//            Bundle response = BillingService.mService.sendBillingRequest(request);
//            logResponseCode("confirmNotifications", response);
//            return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID, Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
            return 0;
        }
    }

    public class GetPurchaseInformation extends BillingRequest {
        long mNonce;
        final String[] mNotifyIds;

        public /* bridge */ /* synthetic */ int getStartId() {
            return super.getStartId();
        }

        public /* bridge */ /* synthetic */ boolean runIfConnected() {
            return super.runIfConnected();
        }

        public /* bridge */ /* synthetic */ boolean runRequest() {
            return super.runRequest();
        }

        public GetPurchaseInformation(int startId, String[] notifyIds) {
            super(startId);
            this.mNotifyIds = notifyIds;
        }

        /* access modifiers changed from: protected */
        public long run() throws RemoteException {
//            this.mNonce = Security.generateNonce();
//            Bundle request = makeRequestBundle("GET_PURCHASE_INFORMATION");
//            request.putLong(Consts.BILLING_REQUEST_NONCE, this.mNonce);
//            request.putStringArray(Consts.BILLING_REQUEST_NOTIFY_IDS, this.mNotifyIds);
//            Bundle response = BillingService.mService.sendBillingRequest(request);
//            logResponseCode("getPurchaseInformation", response);
//            return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID, Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
            return 0;
        }

        /* access modifiers changed from: protected */
        public void onRemoteException(RemoteException e) {
            super.onRemoteException(e);
            Security.removeNonce(this.mNonce);
        }
    }

    public class RequestPurchase extends BillingRequest {
        public final String mDeveloperPayload;
        public final String mProductId;

        public /* bridge */ /* synthetic */ int getStartId() {
            return super.getStartId();
        }

        public /* bridge */ /* synthetic */ boolean runIfConnected() {
            return super.runIfConnected();
        }

        public /* bridge */ /* synthetic */ boolean runRequest() {
            return super.runRequest();
        }

        public RequestPurchase(BillingService billingService, String itemId) {
            this(itemId, null);
        }

        public RequestPurchase(String itemId, String developerPayload) {
            super(-1);
            this.mProductId = itemId;
            this.mDeveloperPayload = developerPayload;
        }

        /* access modifiers changed from: protected */
        public long run() throws RemoteException {
//            Bundle request = makeRequestBundle("REQUEST_PURCHASE");
//            request.putString(Consts.BILLING_REQUEST_ITEM_ID, this.mProductId);
//            if (this.mDeveloperPayload != null) {
//                request.putString(Consts.BILLING_REQUEST_DEVELOPER_PAYLOAD, this.mDeveloperPayload);
//            }
//            Bundle response = BillingService.mService.sendBillingRequest(request);
//            PendingIntent pendingIntent = (PendingIntent) response.getParcelable(Consts.BILLING_RESPONSE_PURCHASE_INTENT);
//            if (pendingIntent == null) {
//                Log.e(BillingService.TAG, "Error with requestPurchase");
//                return Consts.BILLING_RESPONSE_INVALID_REQUEST_ID;
//            }
//            ResponseHandler.buyPageIntentResponse(pendingIntent, new Intent());
//            return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID, Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
            return 0;
        }

        /* access modifiers changed from: protected */
        public void responseCodeReceived(ResponseCode responseCode) {
            ResponseHandler.responseCodeReceived(BillingService.this, this, responseCode);
        }
    }

    public class RestoreTransactions extends BillingRequest {
        long mNonce;

        public /* bridge */ /* synthetic */ int getStartId() {
            return super.getStartId();
        }

        public /* bridge */ /* synthetic */ boolean runIfConnected() {
            return super.runIfConnected();
        }

        public /* bridge */ /* synthetic */ boolean runRequest() {
            return super.runRequest();
        }

        public RestoreTransactions() {
            super(-1);
        }

        /* access modifiers changed from: protected */
        public long run() throws RemoteException {
//            this.mNonce = Security.generateNonce();
//            Bundle request = makeRequestBundle("RESTORE_TRANSACTIONS");
//            request.putLong(Consts.BILLING_REQUEST_NONCE, this.mNonce);
//            Bundle response = BillingService.mService.sendBillingRequest(request);
//            logResponseCode("restoreTransactions", response);
//            return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID, Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
            return 0;
        }

        /* access modifiers changed from: protected */
        public void onRemoteException(RemoteException e) {
            super.onRemoteException(e);
            Security.removeNonce(this.mNonce);
        }

        /* access modifiers changed from: protected */
        public void responseCodeReceived(ResponseCode responseCode) {
            ResponseHandler.responseCodeReceived(BillingService.this, this, responseCode);
        }
    }

    public void setContext(Context context) {
        attachBaseContext(context);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onStart(Intent intent, int startId) {
        if (intent != null) {
            handleCommand(intent, startId);
        }
    }

    public void handleCommand(Intent intent, int startId) {
        String action = intent.getAction();
        Log.i(TAG, "handleCommand() action: " + action);
        if (Consts.ACTION_CONFIRM_NOTIFICATION.equals(action)) {
            confirmNotifications(startId, intent.getStringArrayExtra(Consts.NOTIFICATION_ID));
        } else if (Consts.ACTION_GET_PURCHASE_INFORMATION.equals(action)) {
            String notifyId = intent.getStringExtra(Consts.NOTIFICATION_ID);
            getPurchaseInformation(startId, new String[]{notifyId});
        } else if (Consts.ACTION_PURCHASE_STATE_CHANGED.equals(action)) {
            purchaseStateChanged(startId, intent.getStringExtra(Consts.INAPP_SIGNED_DATA), intent.getStringExtra(Consts.INAPP_SIGNATURE));
        } else if (Consts.ACTION_RESPONSE_CODE.equals(action)) {
            checkResponseCode(intent.getLongExtra(Consts.INAPP_REQUEST_ID, -1), ResponseCode.valueOf(intent.getIntExtra(Consts.INAPP_RESPONSE_CODE, ResponseCode.RESULT_ERROR.ordinal())));
        }
    }

    /* access modifiers changed from: private */
    public boolean bindToMarketBillingService() {
        try {
            Log.i(TAG, "binding to Market billing service");
            if (bindService(new Intent(Consts.MARKET_BILLING_SERVICE_ACTION), this, BIND_AUTO_CREATE)) {
                return true;
            }
            Log.e(TAG, "Could not bind to service.");
            return false;
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception: " + e);
        }
        return false;
    }

    public boolean checkBillingSupported() {
        return new CheckBillingSupported().runRequest();
    }

    public boolean requestPurchase(String productId, String developerPayload) {
        return new RequestPurchase(productId, developerPayload).runRequest();
    }

    public boolean restoreTransactions() {
        return new RestoreTransactions().runRequest();
    }

    private boolean confirmNotifications(int startId, String[] notifyIds) {
        return new ConfirmNotifications(startId, notifyIds).runRequest();
    }

    private boolean getPurchaseInformation(int startId, String[] notifyIds) {
        return new GetPurchaseInformation(startId, notifyIds).runRequest();
    }

    private void purchaseStateChanged(int startId, String signedData, String signature) {
        ArrayList<VerifiedPurchase> purchases = Security.verifyPurchase(signedData, signature);
        if (purchases != null) {
            ArrayList<String> notifyList = new ArrayList();
            Iterator i$ = purchases.iterator();
            while (i$.hasNext()) {
                VerifiedPurchase vp = (VerifiedPurchase) i$.next();
                if (vp.notificationId != null) {
                    if (vp.purchaseState == PurchaseState.PURCHASED) {
                        try {
                            new MoeyuAPIClient(getApplicationContext()).userBilling(signedData, signature);
                            notifyList.add(vp.notificationId);
                        } catch (MoeyuAPIException e) {
                            e.printStackTrace();
                        }
                    } else {
                        notifyList.add(vp.notificationId);
                    }
                }
                ResponseHandler.purchaseResponse(this, vp.purchaseState, vp.productId, vp.orderId, vp.purchaseTime, vp.developerPayload, signedData, signature, vp.notificationId);
            }
            if (!notifyList.isEmpty()) {
                int i = startId;
                confirmNotifications(i, (String[]) notifyList.toArray(new String[notifyList.size()]));
            }
        }
    }

    private void checkResponseCode(long requestId, ResponseCode responseCode) {
        BillingRequest request = (BillingRequest) mSentRequests.get(Long.valueOf(requestId));
        if (request != null) {
            Log.d(TAG, request.getClass().getSimpleName() + ": " + responseCode);
            request.responseCodeReceived(responseCode);
        }
        mSentRequests.remove(Long.valueOf(requestId));
    }

    private void runPendingRequests() {
        int maxStartId = -1;
        while (true) {
            BillingRequest request = (BillingRequest) mPendingRequests.peek();
            if (request != null) {
                if (request.runIfConnected()) {
                    mPendingRequests.remove();
                    if (maxStartId < request.getStartId()) {
                        maxStartId = request.getStartId();
                    }
                } else {
                    bindToMarketBillingService();
                    return;
                }
            } else if (maxStartId >= 0) {
                Log.i(TAG, "stopping service, startId: " + maxStartId);
                stopSelf(maxStartId);
                return;
            } else {
                return;
            }
        }
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "Billing service connected");
//        mService = Stub.asInterface(service);
//        runPendingRequests();
    }

    public void onServiceDisconnected(ComponentName name) {
        Log.w(TAG, "Billing service disconnected");
//        mService = null;
    }

    public void unbind() {
        try {
            unbindService(this);
        } catch (IllegalArgumentException e) {
        }
    }
}
