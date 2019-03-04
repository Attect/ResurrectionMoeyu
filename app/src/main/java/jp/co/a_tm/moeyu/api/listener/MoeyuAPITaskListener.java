package jp.co.a_tm.moeyu.api.listener;

import jp.co.a_tm.moeyu.api.MoeyuAPIException;

public interface MoeyuAPITaskListener<Result> {
    void onCancel();

    void onError(MoeyuAPIException moeyuAPIException);

    void onPreCallback();

    void onSuccess(Result result);
}
