package com.netelsan.ipinterkompanel.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class APKListItem {

    public File file;

    boolean isSelected;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}