package com.netelsan.ipinterkompanel.listener;

public interface TCPIPListener<T> {

    void onSuccess(int operationType, T data);

    void onFailure(Exception e);
}
