package jp.co.a_tm.moeyu.api.fragment;

import android.view.View;

import jp.co.a_tm.moeyu.api.MoeyuAPIClient.GachaCoin;
import jp.co.a_tm.moeyu.api.MoeyuAPIException;
import jp.co.a_tm.moeyu.api.listener.GachaResultListener;
import jp.co.a_tm.moeyu.api.listener.MoeyuAPITaskListener;
import jp.co.a_tm.moeyu.api.model.GachaResult;
import jp.co.a_tm.moeyu.api.task.GachaTask;

public class GachaFragment extends NetworkBaseFragment {
    private GachaCoin mCoin;
    /* access modifiers changed from: private */
    public GachaResultListener mListener;
    private String mUserId;

    public void gacha(String userId, GachaCoin coin, GachaResultListener listener) {
        this.mUserId = userId;
        this.mCoin = coin;
        this.mListener = listener;
        execute();
    }

    /* access modifiers changed from: protected */
    public void execute() {
        this.mIndicator.setVisibility(View.VISIBLE);
        new GachaTask(getActivity(), this.mUserId, this.mCoin, new MoeyuAPITaskListener<GachaResult>() {
            public void onPreCallback() {
                GachaFragment.this.mIndicator.setVisibility(View.INVISIBLE);
            }

            public void onSuccess(GachaResult gachaResult) {
                GachaFragment.this.mListener.onSuccess(gachaResult);
            }

            public void onError(MoeyuAPIException e) {
                GachaFragment.this.mNetworkError.setVisibility(View.VISIBLE);
                GachaFragment.this.mListener.onError(e);
            }

            public void onCancel() {
            }
        }).execute();
    }
}
