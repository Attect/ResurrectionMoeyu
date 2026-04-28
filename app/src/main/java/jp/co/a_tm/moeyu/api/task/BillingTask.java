
package jp.co.a_tm.moeyu.api.task;

import android.content.Context;
import jp.co.a_tm.moeyu.api.listener.MoeyuAPITaskListener;
import jp.co.a_tm.moeyu.model.UserData;

/**
 * 本地化计费任务
 * 通过产品ID直接赠送对应货币，无需远程验证
 */
public class BillingTask extends BaseTask<Void, Void, UserData> {
    private String mProductId;

    public BillingTask(Context context, String productId, MoeyuAPITaskListener<UserData> listener) {
        super(context, listener);
        this.mProductId = productId;
    }

    @Override
    public UserData doInBackground(Void... params) {
        return this.mApiClient.userBilling(this.mProductId);
    }

    @Override
    public void onPostExecute(UserData result) {
        if (result != null) {
            storeUserData(result);
        }
        super.onPostExecute(result);
    }
}
