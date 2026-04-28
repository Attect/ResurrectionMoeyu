
package jp.co.a_tm.moeyu.api.fragment;

import android.view.View;

import jp.co.a_tm.moeyu.api.MoeyuAPIException;
import jp.co.a_tm.moeyu.api.listener.MoeyuAPITaskListener;
import jp.co.a_tm.moeyu.api.listener.UserDataListener;
import jp.co.a_tm.moeyu.api.task.BillingTask;
import jp.co.a_tm.moeyu.model.UserData;

/**
 * 本地化计费Fragment
 * 通过产品ID直接赠送对应货币
 */
public class BillingFragment extends NetworkBaseFragment {
    public UserDataListener mListener;
    private String mProductId;

    public void billing(String productId, UserDataListener listener) {
        this.mProductId = productId;
        this.mListener = listener;
        execute();
    }

    @Override
    public void execute() {
        this.mIndicator.setVisibility(View.VISIBLE);
        new BillingTask(getActivity(), this.mProductId, new MoeyuAPITaskListener<UserData>() {
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
