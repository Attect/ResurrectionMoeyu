package jp.co.a_tm.moeyu.api.listener;

import jp.co.a_tm.moeyu.api.MoeyuAPIException;
import jp.co.a_tm.moeyu.model.UserData;

public interface UserDataListener {
    void onCancel();

    void onError(MoeyuAPIException moeyuAPIException);

    void onSuccess(UserData userData);
}
