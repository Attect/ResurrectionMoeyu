package jp.co.a_tm.moeyu.api.listener;

import jp.co.a_tm.moeyu.api.MoeyuAPIException;
import jp.co.a_tm.moeyu.api.model.GachaResult;

public interface GachaResultListener {
    void onCancel();

    void onError(MoeyuAPIException moeyuAPIException);

    void onSuccess(GachaResult gachaResult);
}
