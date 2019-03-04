package jp.co.a_tm.moeyu.api.task;

import android.content.Context;
import jp.co.a_tm.moeyu.api.MoeyuAPIException;
import jp.co.a_tm.moeyu.api.listener.MoeyuAPITaskListener;
import jp.co.a_tm.moeyu.model.UserData;

public class SignupTask extends BaseTask<Void, Void, UserData> {
    public SignupTask(Context context, MoeyuAPITaskListener<UserData> listener) {
        super(context, listener);
    }

    /* access modifiers changed from: protected|varargs */
    public UserData doInBackground(Void... params) {
        try {
            return this.mApiClient.userSignUp();
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
