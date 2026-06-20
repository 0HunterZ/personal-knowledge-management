package com.focusnode.service;

import com.focusnode.model.LocalProfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class LocalProfileManager {
    private static final String FOLDER = ".focusnode";
    private static final String FILE_NAME = "profile.properties";

    private final Path profileFile;

    public LocalProfileManager() {
        String home = System.getProperty("user.home");
        Path dir = Paths.get(home, FOLDER);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                // ignore - will fail on save later
            }
        }
        profileFile = dir.resolve(FILE_NAME);
    }

    public boolean exists() {
        return Files.exists(profileFile);
    }

    public LocalProfile load() {
        Properties p = new Properties();
        if (!exists()) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(profileFile.toFile())) {
            p.load(fis);
            String name = p.getProperty("name", "");
            String avatar = p.getProperty("avatarPath", "");
            return new LocalProfile(name, avatar.isBlank() ? null : avatar);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void save(LocalProfile profile) {
        Properties p = new Properties();
        p.setProperty("name", profile.getName() == null ? "" : profile.getName());
        p.setProperty("avatarPath", profile.getAvatarPath() == null ? "" : profile.getAvatarPath());
        try (FileOutputStream fos = new FileOutputStream(profileFile.toFile())) {
            p.store(fos, "FocusNode Local Profile");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
