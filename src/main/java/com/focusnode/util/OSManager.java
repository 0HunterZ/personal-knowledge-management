package com.focusnode.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class OSManager {

    private static final String HOSTS_FILE_PATH = "C:\\Windows\\System32\\drivers\\etc\\hosts";
    private static final List<String> BLOCKED_SITES = Arrays.asList(
            "facebook.com", "www.facebook.com",
            "youtube.com", "www.youtube.com",
            "twitter.com", "www.twitter.com",
            "instagram.com", "www.instagram.com",
            "tiktok.com", "www.tiktok.com"
    );

    public static void enableDoNotDisturb() {
        try {
            // Modifying registry for Focus Assist (Windows 10/11)
            String command = "powershell.exe -Command \"Set-ItemProperty -Path 'HKCU:\\Software\\Microsoft\\Windows\\CurrentVersion\\Notifications\\Settings' -Name 'NOC_GLOBAL_SETTING_TOASTS_ENABLED' -Value 0\"";
            Runtime.getRuntime().exec(command);
            System.out.println("[OSManager] Do Not Disturb Enabled.");
        } catch (IOException e) {
            System.err.println("[OSManager] Failed to enable DND: " + e.getMessage());
        }
    }

    public static void disableDoNotDisturb() {
        try {
            String command = "powershell.exe -Command \"Set-ItemProperty -Path 'HKCU:\\Software\\Microsoft\\Windows\\CurrentVersion\\Notifications\\Settings' -Name 'NOC_GLOBAL_SETTING_TOASTS_ENABLED' -Value 1\"";
            Runtime.getRuntime().exec(command);
            System.out.println("[OSManager] Do Not Disturb Disabled.");
        } catch (IOException e) {
            System.err.println("[OSManager] Failed to disable DND: " + e.getMessage());
        }
    }

    public static void blockDistractingWebsites() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HOSTS_FILE_PATH, true))) {
            writer.newLine();
            writer.write("# FOCUS NODE ZEN MODE BLOCK");
            writer.newLine();
            for (String site : BLOCKED_SITES) {
                writer.write("127.0.0.1 " + site);
                writer.newLine();
            }
            System.out.println("[OSManager] Distracting websites blocked.");
        } catch (IOException e) {
            System.err.println("[OSManager] Failed to block websites. Ensure application is running as Administrator. " + e.getMessage());
        }
    }

    public static void unblockDistractingWebsites() {
        try {
            String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(HOSTS_FILE_PATH)));
            StringBuilder newContent = new StringBuilder();
            String[] lines = content.split("\n");
            
            boolean inBlock = false;
            for (String line : lines) {
                if (line.contains("# FOCUS NODE ZEN MODE BLOCK")) {
                    inBlock = true;
                    continue;
                }
                
                if (inBlock) {
                    boolean isBlockedSite = false;
                    for (String site : BLOCKED_SITES) {
                        if (line.contains(site)) {
                            isBlockedSite = true;
                            break;
                        }
                    }
                    if (isBlockedSite) {
                        continue; // Skip the blocked site line
                    } else {
                        inBlock = false; // Exited the block
                    }
                }
                
                if (!inBlock) {
                    newContent.append(line).append("\n");
                }
            }
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(HOSTS_FILE_PATH))) {
                writer.write(newContent.toString().trim());
                System.out.println("[OSManager] Distracting websites unblocked.");
            }
        } catch (IOException e) {
            System.err.println("[OSManager] Failed to unblock websites. Ensure application is running as Administrator. " + e.getMessage());
        }
    }
}
