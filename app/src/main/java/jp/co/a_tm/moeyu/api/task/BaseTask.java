package jp.co.a_tm.moeyu.api.task;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jp.co.a_tm.moeyu.ItemTableController;
import jp.co.a_tm.moeyu.api.MoeyuAPIClient;
import jp.co.a_tm.moeyu.api.MoeyuAPIException;
import jp.co.a_tm.moeyu.api.listener.MoeyuAPITaskListener;
import jp.co.a_tm.moeyu.model.UserData;
import jp.co.a_tm.moeyu.util.Config;
import jp.co.a_tm.moeyu.util.Logger;
import jp.co.a_tm.moeyu.util.UserDataManager;

/**
 * API 异步任务基类（AsyncTask 替代实现）
 * 使用 ExecutorService + Handler 实现后台执行与主线程回调，避免 AsyncTask 废弃带来的兼容性风险
 */
public abstract class BaseTask<Params, Progress, Result> {
    protected MoeyuAPIClient mApiClient;
    protected Context mContext;
    private UserDataManager mDataManager;
    protected MoeyuAPIException mException = null;
    protected MoeyuAPITaskListener<Result> mListener;
    private volatile boolean mCancelled = false;
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    public BaseTask(Context context, MoeyuAPITaskListener<Result> listener) {
        this.mContext = context;
        this.mListener = listener;
        this.mApiClient = new MoeyuAPIClient(this.mContext);
        this.mDataManager = new UserDataManager(this.mContext);
    }

    @SafeVarargs
    public final void execute(Params... params) {
        EXECUTOR.execute(() -> {
            Result result = null;
            try {
                result = doInBackground(params);
            } catch (Exception e) {
                if (e instanceof MoeyuAPIException) {
                    this.mException = (MoeyuAPIException) e;
                } else {
                    this.mException = new MoeyuAPIException(e);
                    Logger.e("BaseTask", "后台任务异常", e);
                }
            }
            if (!mCancelled) {
                final Result finalResult = result;
                new Handler(Looper.getMainLooper()).post(() -> onPostExecute(finalResult));
            }
        });
    }

    public void cancel() {
        mCancelled = true;
        onCancelled();
    }

    protected abstract Result doInBackground(Params... params);

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

    public void onCancelled() {
        if (this.mListener != null) {
            this.mListener.onPreCallback();
            this.mListener.onCancel();
        }
    }

    public void storeUserData(UserData userData) {
        if (userData != null) {
            this.mDataManager.saveUserData(userData);
            new ItemTableController(this.mContext).update(userData.getItems());
        }
    }
}
