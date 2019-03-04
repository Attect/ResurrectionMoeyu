package jp.co.a_tm.moeyu.api.task;

import android.content.Context;
import jp.co.a_tm.moeyu.api.MoeyuAPIClient.GachaCoin;
import jp.co.a_tm.moeyu.api.MoeyuAPIException;
import jp.co.a_tm.moeyu.api.listener.MoeyuAPITaskListener;
import jp.co.a_tm.moeyu.api.model.GachaResult;

public class GachaTask extends BaseTask<Void, Void, GachaResult> {
    private GachaCoin mCoin;
    private String mUserId;

    public GachaTask(Context context, String userId, GachaCoin coin, MoeyuAPITaskListener<GachaResult> listener) {
        super(context, listener);
        this.mUserId = userId;
        this.mCoin = coin;
    }

    /* access modifiers changed from: protected|varargs */
    public GachaResult doInBackground(Void... params) {
        try {
            return this.mApiClient.userGatya(this.mUserId, this.mCoin);
        } catch (MoeyuAPIException e) {
            this.mException = e;
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(GachaResult result) {
        if (result != null) {
            storeUserData(result.getUserData());
        }
        super.onPostExecute(result);
    }
}
