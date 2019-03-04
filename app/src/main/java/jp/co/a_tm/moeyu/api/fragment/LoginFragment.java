package jp.co.a_tm.moeyu.api.fragment;

import android.view.View;

import jp.co.a_tm.moeyu.api.MoeyuAPIException;
import jp.co.a_tm.moeyu.api.listener.MoeyuAPITaskListener;
import jp.co.a_tm.moeyu.api.listener.UserDataListener;
import jp.co.a_tm.moeyu.api.task.LoginTask;
import jp.co.a_tm.moeyu.model.UserData;

public class LoginFragment extends NetworkBaseFragment {
    /* access modifiers changed from: private */
    public UserDataListener mListener;
    /* access modifiers changed from: private */
    public boolean mShowNetoworkError;
    private String mUserId;

    public void login(String userId, UserDataListener listener) {
        login(userId, listener, true);
    }

    public void login(String userId, UserDataListener listener, boolean retry) {
        login(userId, listener, retry, true);
    }

    public void login(String userId, UserDataListener listener, boolean retry, boolean showNetworkError) {
        this.mUserId = userId;
        this.mListener = listener;
        this.mRetry = retry;
        this.mShowNetoworkError = showNetworkError;
        execute();
    }

    /* access modifiers changed from: protected */
    public void execute() {
        this.mIndicator.setVisibility(View.VISIBLE);
        new LoginTask(getActivity(), this.mUserId, new MoeyuAPITaskListener<UserData>() {
            public void onPreCallback() {
                LoginFragment.this.mIndicator.setVisibility(View.INVISIBLE);
            }

            public void onSuccess(UserData userData) {
                LoginFragment.this.mListener.onSuccess(userData);
            }

            public void onError(MoeyuAPIException e) {
                if (LoginFragment.this.mShowNetoworkError) {
                    LoginFragment.this.mNetworkError.setVisibility(View.VISIBLE);
                }
                LoginFragment.this.mListener.onError(e);
            }

            public void onCancel() {
            }
        }).execute();
    }
}
