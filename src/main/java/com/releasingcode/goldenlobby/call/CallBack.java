package com.releasingcode.goldenlobby.call;

public class CallBack {
    public interface SingleCallBack {
        void onSuccess();

        default void onError() {

        }
    }

    public interface ReturnCallBack<T> {
        void onSuccess(T callback);

        default void onError(T callback) {

        }
    }
}
