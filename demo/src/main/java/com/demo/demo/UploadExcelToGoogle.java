package com.demo.demo;

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
import com.google.api.services.drive.model.File;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class UploadExcelToGoogle {

    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "D:\\jsonKey\\tokens.json";

    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/drive");

    public static void main(String[] args) throws Exception {
        // Upload Excel file to Google Drive
        uploadExcelToGoogle("D:\\CRF_OneValues1.xlsx");
    }

    private static void uploadExcelToGoogle(String excelFilePath) {
        try {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

            
            Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME).build();
            
            // Create a file metadata
            File fileMetadata = new File();
            fileMetadata.setName(new java.io.File(excelFilePath).getName());
            fileMetadata.setMimeType("application/vnd.ms-excel");
            
          //  System.out.println(fileMetadata+"IS HERE ISSUE?");

//            Credential credential = getCredentials(HTTP_TRANSPORT);
//            System.out.println("Access Token: " + credential.getAccessToken());
//         // Refresh the access token if it has expired
//            if (credential.getExpiresInSeconds() <= 60) {
//                credential.refreshToken();
//            }
//            System.out.println("Token Expiration (seconds): " + credential.getExpiresInSeconds());
//            System.out.println("Refresh Token: " + credential.getRefreshToken());

            // Upload the Excel file to Google Drive
            java.io.File filePath = new java.io.File(excelFilePath);
            FileContent mediaContent = new FileContent("application/vnd.ms-excel", filePath);
          //  System.out.println(mediaContent+"MEDIACONTENT");
            
           // System.out.println(driveService.files()+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            File file = driveService.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
           // System.out.println("qqqqq "+file+"@@@@@@@@@@@@@@@@@@@");
            System.out.println("File ID: " + file.getId() +"!!!!!!!!!");

            // Open Google Drive file in browser
            Desktop desk = Desktop.getDesktop();
            desk.browse(new URI("https://drive.google.com/file/d/" + file.getId()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets
    	
        InputStream in = new FileInputStream(new java.io.File("D:\\jsonKey\\tokens.json"));
       // System.out.println("wwwww "+in);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

      //  System.out.println("clie secretes "+clientSecrets);
        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        
    }
}
