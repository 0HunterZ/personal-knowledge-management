package com.focusnode.model;

public class LocalProfile {
    private String name;
    private String avatarPath; // absolute path to local avatar image

    public LocalProfile() {
    }

    public LocalProfile(String name, String avatarPath) {
        this.name = name;
        this.avatarPath = avatarPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
}
