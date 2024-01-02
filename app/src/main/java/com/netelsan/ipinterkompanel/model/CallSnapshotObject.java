package com.netelsan.ipinterkompanel.model;

import java.io.Serializable;

public class CallSnapshotObject implements Serializable {

    private static final long serialVersionUID = 4L;

    public String imageId;
    public String objectString;

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getObjectString() {
        return objectString;
    }

    public void setObjectString(String objectString) {
        this.objectString = objectString;
    }

}
