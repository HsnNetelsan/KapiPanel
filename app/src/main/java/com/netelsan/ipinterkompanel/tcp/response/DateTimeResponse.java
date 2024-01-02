package com.netelsan.ipinterkompanel.tcp.response;

import java.io.Serializable;

public class DateTimeResponse<DResponse> implements Serializable {

    long timeMillis;

    public long getTimeMillis() {
        return timeMillis;
    }

    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }
}
