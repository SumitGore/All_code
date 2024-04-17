package com.demo.demo;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.Arrays;


public class uploadDrive {

    private static final String APPLICATION_NAME = "Your Application Name";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_PATH = "D:\\jsonKey\\tokens1.json";
    private static final java.io.File DATA_STORE_DIR = new java.io.File("token path");

    public static void main(String[] args) throws Exception {
        // Set up the Google Drive API
        Drive service = getDriveService();
        LocalDate start = LocalDate.now();  // Date start time now 
        String currentYearMonth = start.getMonth().name()+"_"+start.getYear();
   	
        // Specify the file to upload
        java.io.File fileToUpload = new java.io.File("D:\\PCB_Value_JANUARY_2024");

        // Create a File instance for the file to upload
        File fileMetadata = new File();
        fileMetadata.setName(fileToUpload.getName());

        // Create the media content
        InputStreamContent mediaContent = new InputStreamContent("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new FileInputStream(fileToUpload));

        // Upload the file
        File uploadedFile = service.files().create(fileMetadata, mediaContent).execute();

        // Print the file ID
        System.out.println("File ID: " + uploadedFile.getId());
    }

    private static Drive getDriveService() throws IOException, GeneralSecurityException {
        // Load client secrets
        InputStream in = new FileInputStream(CREDENTIALS_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Check that client secrets contain the necessary details
        if (clientSecrets.getDetails() == null || clientSecrets.getDetails().getClientId() == null || clientSecrets.getDetails().getClientSecret() == null) {
            throw new IllegalArgumentException("Client secrets are missing required details.");
        }

        // Set up authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets,
                Arrays.asList(DriveScopes.DRIVE))
                .setDataStoreFactory(new FileDataStoreFactory(DATA_STORE_DIR))
                .setAccessType("offline")
                .build();

        // Authorize
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");

        // Create and return the Drive service
        return new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


}
