package jp.co.a_tm.moeyu.api.task;

import android.content.Context;
import android.os.AsyncTask;
import jp.co.a_tm.moeyu.ItemTableController;
import jp.co.a_tm.moeyu.api.MoeyuAPIClient;
import jp.co.a_tm.moeyu.api.MoeyuAPIException;
import jp.co.a_tm.moeyu.api.listener.MoeyuAPITaskListener;
import jp.co.a_tm.moeyu.model.UserData;
import jp.co.a_tm.moeyu.util.Config;
import jp.co.a_tm.moeyu.util.Logger;
import jp.co.a_tm.moeyu.util.UserDataManager;

public abstract class BaseTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    protected MoeyuAPIClient mApiClient;
    protected Context mContext;
    private UserDataManager mDataManager;
    protected MoeyuAPIException mException = null;
    protected MoeyuAPITaskListener<Result> mListener;

    public BaseTask(Context context, MoeyuAPITaskListener<Result> listener) {
        this.mContext = context;
        this.mListener = listener;
        this.mApiClient = new MoeyuAPIClient(this.mContext);
        this.mDataManager = new UserDataManager(this.mContext);
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Result result) {
        if (this.mListener != null) {
            this.mListener.onPreCallback();
            if (this.mException == null) {
                this.mListener.onSuccess(result);
                return;
            }
            if (!Config.getInstance(this.mContext).isProd()) {
                Logger.d("Moeyu API status code = " + this.mException.getStatusCode());
                this.mException.printStackTrace();
            }
            this.mListener.onError(this.mException);
        }
    }

    /* access modifiers changed from: protected */
    public void onCancelled() {
        if (this.mListener != null) {
            this.mListener.onPreCallback();
            this.mListener.onCancel();
        }
    }

    /* access modifiers changed from: protected */
    public void storeUserData(UserData userData) {
        if (userData != null) {
            this.mDataManager.saveUserData(userData);
            new ItemTableController(this.mContext).update(userData.getItems());
        }
    }
}
