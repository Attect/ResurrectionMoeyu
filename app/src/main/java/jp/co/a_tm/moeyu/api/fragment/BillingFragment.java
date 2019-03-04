package jp.co.a_tm.moeyu.api.fragment;

import android.view.View;

import jp.co.a_tm.moeyu.api.MoeyuAPIException;
import jp.co.a_tm.moeyu.api.listener.MoeyuAPITaskListener;
import jp.co.a_tm.moeyu.api.listener.UserDataListener;
import jp.co.a_tm.moeyu.api.task.BillingTask;
import jp.co.a_tm.moeyu.model.UserData;

public class BillingFragment extends NetworkBaseFragment {
    /* access modifiers changed from: private */
    public UserDataListener mListener;
    private String mSignature;
    private String mSignedData;

    public void billing(String signedData, String signature, UserDataListener listener) {
        this.mSignedData = signedData;
        this.mSignature = signature;
        this.mListener = listener;
        execute();
    }

    /* access modifiers changed from: protected */
    public void execute() {
        this.mIndicator.setVisibility(View.VISIBLE);
        new BillingTask(getActivity(), this.mSignedData, this.mSignature, new MoeyuAPITaskListener<UserData>() {
            public void onPreCallback() {
                BillingFragment.this.mIndicator.setVisibility(View.INVISIBLE);
            }

            public void onSuccess(UserData userData) {
                BillingFragment.this.mListener.onSuccess(userData);
            }

            public void onError(MoeyuAPIException e) {
                BillingFragment.this.mNetworkError.setVisibility(View.VISIBLE);
                BillingFragment.this.mListener.onError(e);
            }

            public void onCancel() {
            }
        }).execute();
    }
}
