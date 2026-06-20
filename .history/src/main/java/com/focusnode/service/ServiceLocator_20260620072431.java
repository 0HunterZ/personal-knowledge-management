package com.focusnode.service;

import com.focusnode.repository.DatabaseManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServiceLocator {
    private static AppDataService appDataService;
    private static LanSessionService lanSessionService;
    private static LanDiscoveryService lanDiscoveryService;
    private static LanFileTransferService lanFileTransferService;
    private static LocalProfileManager localProfileManager;
    private static final ExecutorService asyncExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private static boolean initialized = false;

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        DatabaseManager.initialize();
        appDataService = appDataService == null ? new SqlAppDataService() : appDataService;
        lanSessionService = lanSessionService == null ? new LanSessionService() : lanSessionService;
        localProfileManager = localProfileManager == null ? new LocalProfileManager() : localProfileManager;
        lanDiscoveryService = lanDiscoveryService == null ? new LanDiscoveryService(lanSessionService, asyncExecutor) : lanDiscoveryService;
        lanFileTransferService = lanFileTransferService == null ? new LanFileTransferService(lanSessionService, asyncExecutor) : lanFileTransferService;

        lanDiscoveryService.startListening();
        initialized = true;
    }

    public static synchronized void shutdown() {
        if (!initialized) {
            return;
        }

        if (lanDiscoveryService != null) {
            lanDiscoveryService.stopListening();
        }

        if (lanSessionService != null && lanSessionService.getLocalMember() != null) {
            lanSessionService.leaveRoom(lanSessionService.getLocalMember());
        }

        initialized = false;
    }

    public static AppDataService getAppDataService() {
        ensureInitialized();
        return appDataService;
    }

    public static LocalProfileManager getLocalProfileManager() {
        ensureInitialized();
        return localProfileManager;
    }

    public static void setAppDataService(AppDataService service) {
        appDataService = service;
    }

    public static LanSessionService getLanSessionService() {
        ensureInitialized();
        return lanSessionService;
    }

    public static LanDiscoveryService getLanDiscoveryService() {
        ensureInitialized();
        return lanDiscoveryService;
    }

    public static LanFileTransferService getLanFileTransferService() {
        ensureInitialized();
        return lanFileTransferService;
    }

    public static ExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }

    private static void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("ServiceLocator must be initialized before use. Call ServiceLocator.initialize() in application startup.");
        }
    }
}
