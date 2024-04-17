package com.demo.demo;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class test9 {

    public static void main(String[] args) {
        String filePath = "D:\\CRF_All_Customers_CalculatedTags.xlsx";
        try {
            // Read existing Excel file
            FileInputStream fileInputStream = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fileInputStream);

            // Get the first sheet
            Sheet originalSheet = workbook.getSheetAt(0);

//            Date time time tamp for start and end(24 hours)
//               LocalDate start = LocalDate.now(); // Or whatever you want
//               LocalDate end = start.minusDays(1);
      
         
            
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
                    if ("Dashboard not available".equalsIgnoreCase(cellInColumn5.getStringCellValue()) || "Dashboard not available ".equalsIgnoreCase(cellInColumn5.getStringCellValue()) ||
                    		"Dashboard not availabe".equalsIgnoreCase(cellInColumn5.getStringCellValue())) {
                    	continue;
                    }
                    
                    // All Row tag ( Get all remaining cells as tags)

                    List<String> tags = new ArrayList<>();
                    for (int i = 3; i < row.getLastCellNum(); i++) {
                        Cell tagCell = row.getCell(i);
                        if (tagCell != null) {
                            switch (tagCell.getCellTypeEnum()) {
                                case STRING:
                                	if("NA".equalsIgnoreCase(tagCell.getStringCellValue())) {
                                		
                                	}else {
                                		tags.add(tagCell.getStringCellValue());
                                	}
                                	
                              	
                                    break;
                                case NUMERIC:
                                    tags.add(String.valueOf(tagCell.getNumericCellValue()));
                                    break;
                               //  Handle other cell types as needed
                            }
//                            System.out.println(tags);
                        }
                    }

                    // Update the map with customer name and tags
                    customerTagMap.put(customerName, tags);
                }
            }

            // Print the customerTagMap for debugging
            for (Map.Entry<String, List<String>> entry : customerTagMap.entrySet()) {
                String customerName = entry.getKey();
                List<String> tags = entry.getValue();
               System.out.println("Customer: " + customerName + ", Tags: " + tags);
            }
            System.out.println(customerTagMap);

            // Further processing...  remaining code 

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
}
