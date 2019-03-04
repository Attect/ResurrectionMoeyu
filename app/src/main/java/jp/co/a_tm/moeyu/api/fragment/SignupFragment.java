package jp.co.a_tm.moeyu.api.fragment;

import android.view.View;

import jp.co.a_tm.moeyu.api.MoeyuAPIException;
import jp.co.a_tm.moeyu.api.listener.MoeyuAPITaskListener;
import jp.co.a_tm.moeyu.api.listener.UserDataListener;
import jp.co.a_tm.moeyu.api.task.SignupTask;
import jp.co.a_tm.moeyu.model.UserData;

public class SignupFragment extends NetworkBaseFragment {
    /* access modifiers changed from: private */
    public UserDataListener mListener;

    public void signup(UserDataListener listener) {
        this.mListener = listener;
        execute();
    }

    /* access modifiers changed from: protected */
    public void execute() {
        this.mIndicator.setVisibility(View.VISIBLE);
        new SignupTask(getActivity(), new MoeyuAPITaskListener<UserData>() {
            public void onPreCallback() {
                SignupFragment.this.mIndicator.setVisibility(View.INVISIBLE);
            }

            public void onSuccess(UserData userData) {
                SignupFragment.this.mListener.onSuccess(userData);
            }

            public void onError(MoeyuAPIException e) {
                SignupFragment.this.mNetworkError.setVisibility(View.VISIBLE);
                SignupFragment.this.mListener.onError(e);
            }

            public void onCancel() {
            }
        }).execute();
    }
}
