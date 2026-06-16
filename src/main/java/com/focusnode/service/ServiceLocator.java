package com.focusnode.service;

import com.focusnode.repository.DatabaseManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServiceLocator {
    private static AppDataService appDataService;
    private static LanSessionService lanSessionService;
    private static LanDiscoveryService lanDiscoveryService;
    private static LanFileTransferService lanFileTransferService;
    private static final ExecutorService asyncExecutor = Executors.newVirtualThreadPerTaskExecutor();

    static {
        // Initialize database and switch to SQL Server implementation
        DatabaseManager.initialize();
        appDataService = new SqlAppDataService();
        lanSessionService = new LanSessionService();
        lanDiscoveryService = new LanDiscoveryService(lanSessionService, asyncExecutor);
        lanFileTransferService = new LanFileTransferService(lanSessionService, asyncExecutor);
        
        // Auto-start listening for LAN rooms
        lanDiscoveryService.startListening();
    }

    public static AppDataService getAppDataService() {
        return appDataService;
    }

    public static void setAppDataService(AppDataService service) {
        appDataService = service;
    }

    public static LanSessionService getLanSessionService() {
        return lanSessionService;
    }

    public static LanDiscoveryService getLanDiscoveryService() {
        return lanDiscoveryService;
    }

    public static LanFileTransferService getLanFileTransferService() {
        return lanFileTransferService;
    }

    public static ExecutorService getAsyncExecutor() {
        return asyncExecutor;
    }
}
