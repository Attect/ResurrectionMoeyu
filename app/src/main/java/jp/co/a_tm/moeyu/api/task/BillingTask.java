package jp.co.a_tm.moeyu.api.task;

import android.content.Context;
import jp.co.a_tm.moeyu.api.MoeyuAPIException;
import jp.co.a_tm.moeyu.api.listener.MoeyuAPITaskListener;
import jp.co.a_tm.moeyu.model.UserData;

public class BillingTask extends BaseTask<Void, Void, UserData> {
    private String mSignature;
    private String mSignedData;

    public BillingTask(Context context, String signedData, String signature, MoeyuAPITaskListener<UserData> listener) {
        super(context, listener);
        this.mSignedData = signedData;
        this.mSignature = signature;
    }

    /* access modifiers changed from: protected|varargs */
    public UserData doInBackground(Void... params) {
        try {
            return this.mApiClient.userBilling(this.mSignedData, this.mSignature);
        } catch (MoeyuAPIException e) {
            this.mException = e;
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(UserData result) {
        if (result != null) {
            storeUserData(result);
        }
        super.onPostExecute(result);
    }
}
