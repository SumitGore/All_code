package com.demo.demo;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class UploadToDrive {

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        // Load client secrets
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("D:/jsonKey/serviceAccount.json"))
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/drive.file"));

        // Build a new authorized API client for Drive
        Drive driveService = new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        )
                .setApplicationName("CRF package")
                .build();

        // Specify the file path of the file to be uploaded
        String filePath = "D:/CRF_OneValues1JANUARY_2024.xlsx";
        String fileName = "CRF_Package"; // The name you want the file to have in Google Drive

        // Upload the file to Google Drive
        uploadFile(driveService, filePath, fileName);
        
    }

    private static void uploadFile(Drive service, String filePath, String fileName) throws IOException {
        // Read the file content
        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));

        // Create the file metadata
        File fileMetadata = new File();
        fileMetadata.setName(fileName);

        // Set the parent folder ID (replace with your desired folder ID or omit if you want to upload to the root)
        fileMetadata.setParents(Collections.singletonList("10MK61Wz1bqgUwA2m3S1uwhcIgVqOOkET"));

        // Create the file content
        java.io.File fileContentIO = new java.io.File(filePath);
        FileContent mediaContent = new FileContent("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", fileContentIO);

        // Upload the file
        try {
            File uploadedFile = service.files().create(fileMetadata, mediaContent)
                    .setFields("id,webContentLink,webViewLink")
                    .execute();

            // Print the file ID
            System.out.println("File ID: " + uploadedFile.getId());
            // Print the link to the uploaded file
            System.out.println("File uploaded. Web Content Link: " + uploadedFile.getWebContentLink());
            System.out.println("File uploaded. Web View Link: " + uploadedFile.getWebViewLink());
         // Update file permissions to allow anyone with the link to view it
//            uploadedFile = service.files().update(uploadedFile.getId(), null).set("permissions", Collections.singletonList(
//                    new Permission()
//                            .setType("anyone")
//                            .setRole("reader")
//            )).execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
