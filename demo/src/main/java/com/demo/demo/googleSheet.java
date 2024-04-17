package com.demo.demo;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class googleSheet {

    public static void main(String[] args) throws Exception {
        // Set the path to the credentials JSON file you downloaded
        String credentialsPath = "D:\\jsonKey\\SheetJsonKey.json";

        // Set the ID of the Google Sheet you want to work with
        String spreadsheetId = "your-spreadsheet-id";

        // Set the range where you want to upload the data
        String range = "Sheet1!A1";

        // Set the path to the Excel file you want to upload
        String excelFilePath = "D:\\CRF(2.2)Values123456.xlsx";

        // Load the credentials JSON file
        try (InputStream credentialsStream = new FileInputStream(credentialsPath)) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/spreadsheets"));

            // Create a Sheets service
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            Sheets sheetsService = new Sheets.Builder(
                    httpTransport,
                    jsonFactory,
                    new HttpCredentialsAdapter(credentials)
            ).setApplicationName("Your Application Name").build();

            // Read the Excel file into a List<List<Object>>
            List<List<Object>> data = readExcelFile(excelFilePath);

            // Prepare the ValueRange object for updating the sheet
            ValueRange body = new ValueRange().setValues(data);

            // Update the sheet
            UpdateValuesResponse result = sheetsService.spreadsheets().values()
                    .update(spreadsheetId, range, body)
                    .setValueInputOption("RAW")
                    .execute();

            System.out.println("Data uploaded to Google Sheet successfully!");
        }
    }

    private static List<List<Object>> readExcelFile(String filePath) {
        // Implement the logic to read the Excel file and convert it into List<List<Object>>
        // You can use a library like Apache POI for reading Excel files in Java
        // Here's a simple example using Apache POI:
        // https://poi.apache.org/components/spreadsheet/quick-guide.html#ReadWriteWorkbook
        return null;
    }
}
