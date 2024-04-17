package com.demo.demo;



import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class test10 {

    public static void main(String[] args) {
        String filePath = "D:\\CRF_All_Customers_CalculatedTags.xlsx";
        try {
            // Read existing Excel file
            FileInputStream fileInputStream = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fileInputStream);

            // Get the first sheet
            Sheet originalSheet = workbook.getSheetAt(0);

            // Date timestamp for start and end (24 hours)
            
            LocalDateTime start = LocalDateTime.now();  // Date start time now
            Timestamp sqlTimestamp = Timestamp.valueOf(start);
            // Convert star Timestamp to milliseconds
            long milliseconds = sqlTimestamp.getTime();
            System.out.println("start in milliseconds: " + milliseconds);

            LocalDateTime end = start.minusDays(1);  // end start time now (-24 hours)
            Timestamp sqlTimestamp1 = Timestamp.valueOf(end);
            // Convert end Timestamp to milliseconds
            long milliseconds1 = sqlTimestamp1.getTime();
            System.out.println("end in milliseconds: " + milliseconds1);

            // Map to store customer names as keys and their tags as values
            Map<String, List<String>> customerTagMap = new HashMap<>();

            // Fetch customer names and tags from the sheet
            for (Row row : originalSheet) {
                Cell nameCell = row.getCell(2); // Assuming customer names are in the third column (index 2)

                if (nameCell != null && nameCell.getCellTypeEnum() == CellType.STRING) {
                    String customerName = nameCell.getStringCellValue();
                    Cell cellInColumn5 = row.getCell(3);

                    // Dashboard not available tag is removing
                    if ("Dashboard not available".equalsIgnoreCase(cellInColumn5.getStringCellValue()) ||
                            "Dashboard not available ".equalsIgnoreCase(cellInColumn5.getStringCellValue()) ||
                            "Dashboard not available".equalsIgnoreCase(cellInColumn5.getStringCellValue())) {
                        continue;
                    }

                    // All Row tag ( Get all remaining cells as tags)
                    List<String> tags = new ArrayList<>();
                    for (int i = 3; i < row.getLastCellNum(); i++) {
                        Cell tagCell = row.getCell(i);
                        if (tagCell != null) {
                            switch (tagCell.getCellTypeEnum()) {
                                case STRING:
                                    if ("NA".equalsIgnoreCase(tagCell.getStringCellValue())) {
                                        // Skip or handle "NA" as needed
                                    } else {
                                        tags.add(tagCell.getStringCellValue());
                                    }
                                    break;
                                case NUMERIC:
                                    // Handle numeric values if needed
                                    tags.add(String.valueOf(tagCell.getNumericCellValue()));
                                    break;
                                // Handle other cell types as needed
                            }
                        }
                    }

                    // Update the map with customer name and tags
                    customerTagMap.put(customerName, tags);
                }
            }

            // Fetch API values and display results
            for (Map.Entry<String, List<String>> entry : customerTagMap.entrySet()) {
                String customerName = entry.getKey();
                List<String> tags = entry.getValue();

                // Fetch API values using customerName and tags
                Map<String, String> apiValues = fetchApiValues(customerName, tags);

                // Display results along with timestamp
                System.out.println("Timestamp: " + start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//                System.out.println("Customer: " + customerName + ", Tags: " + tags);
                System.out.println("API Values: " + apiValues);
            }

            // Further processing...

            // Save the changes back to the Excel file
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            workbook.close();

            System.out.println("Sheet processed successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Simulate fetching API values
    private static Map<String, String> fetchApiValues(String customerName, List<String> tags) {
        // Implement your logic to fetch API values based on customerName and tags
        // This is a placeholder, replace it with your actual API call
        Map<String, String> apiValues = new HashMap<>();
        apiValues.put("ApiTag1", "ApiValue1");
        apiValues.put("ApiTag2", "ApiValue2");
        // ...
        return apiValues;
    }
}
