package com.demo.demo;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class test8 {

    public static void main(String[] args) {
        String filePath = "D:\\CRF_All_Customers_CalculatedTags.xlsx";
        try {
            // Read existing Excel file
            FileInputStream fileInputStream = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fileInputStream);

            // Get the first sheet
            Sheet originalSheet = workbook.getSheetAt(0);

            // Map to store customer names as keys and their tags as values
            Map<String, List<String>> customerTagMap = new HashMap<>();

            // Fetch customer names and tags from the sheet
            for (Row row : originalSheet) {
                Cell nameCell = row.getCell(2); // Assuming customer names are in the first column (index 0)
                Cell tagCell = row.getCell(3);  // Assuming tags are in the second column (index 1)

                if (nameCell != null && nameCell.getCellTypeEnum() == CellType.STRING &&
                        tagCell != null && tagCell.getCellTypeEnum() == CellType.STRING) {
                    String customerName = nameCell.getStringCellValue();
                    String tag = tagCell.getStringCellValue();

                    // Update the map with customer name and tag
                    customerTagMap.computeIfAbsent(customerName, k -> new ArrayList<>()).add(tag);
                }
            }

            // Print the customerTagMap for debugging
            for (Map.Entry<String, List<String>> entry : customerTagMap.entrySet()) {
                String customerName = entry.getKey();
                List<String> tags = entry.getValue();
                System.out.println("Customer: " + customerName + ", Tags: " + tags);
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
}
