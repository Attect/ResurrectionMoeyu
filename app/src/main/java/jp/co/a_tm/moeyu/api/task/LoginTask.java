package jp.co.a_tm.moeyu.api.task;

import android.content.Context;
import jp.co.a_tm.moeyu.api.MoeyuAPIException;
import jp.co.a_tm.moeyu.api.listener.MoeyuAPITaskListener;
import jp.co.a_tm.moeyu.model.UserData;

public class LoginTask extends BaseTask<Void, Void, UserData> {
    private String mUserId;

    public LoginTask(Context context, String userId, MoeyuAPITaskListener<UserData> listener) {
        super(context, listener);
        this.mUserId = userId;
    }

    /* access modifiers changed from: protected|varargs */
    public UserData doInBackground(Void... params) {
        try {
            return this.mApiClient.userData(this.mUserId);
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
