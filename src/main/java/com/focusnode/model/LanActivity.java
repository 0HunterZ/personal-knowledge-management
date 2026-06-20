package com.focusnode.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LanActivity {
    private String text;
    private long timestamp;
    private String colorHex;
    private String icon;

    public LanActivity(String text, String colorHex, String icon) {
        this.text = text;
        this.timestamp = System.currentTimeMillis();
        this.colorHex = colorHex;
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getIcon() {
        return icon;
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date(timestamp));
    }
}
