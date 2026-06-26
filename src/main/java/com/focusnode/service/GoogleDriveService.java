package com.focusnode.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class GoogleDriveService {

    private static final String APPLICATION_NAME = "FocusNode";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private Drive driveService;

    public GoogleDriveService() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            this.driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            System.err.println("Could not initialize Google Drive Service: " + e.getMessage());
        }
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws Exception {
        InputStream in = GoogleDriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            // Check in the local directory if not found in resources
            java.io.File localCreds = new java.io.File("credentials.json");
            if (localCreds.exists()) {
                in = new java.io.FileInputStream(localCreds);
            } else {
                throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH + " or local credentials.json");
            }
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public boolean uploadFileToDrive(java.io.File file) {
        if (driveService == null || file == null || !file.exists()) {
            return false;
        }

        try {
            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
            fileMetadata.setName(file.getName());

            // Simple mime-type guessing based on extension
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            FileContent mediaContent = new FileContent(mimeType, file);
            com.google.api.services.drive.model.File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
            System.out.println("File uploaded to Google Drive. File ID: " + uploadedFile.getId());
            return true;
        } catch (Exception e) {
            System.err.println("Failed to upload to Google Drive: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
