package com.demo.demo;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class test3 {

    public static void main(String[] args) {
        String filePath = "D:\\CRF_All_Customers_CalculatedTags.xlsx";
        try {
            // Read existing Excel file
            FileInputStream fileInputStream = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            
            

            // Get the first sheet
            Sheet originalSheet = workbook.getSheetAt(0);
        
         // Fetch customer names from the "Name" column in the first sheet
            Iterator<Row> iterator = originalSheet.iterator();
            while (iterator.hasNext()) {
                Row row = iterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();   //iterating over each column
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    
//                    System.out.println("cell form"+ cell);
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_STRING:    //field that represents string cell type
                          System.out.print(cell.getStringCellValue() + "\t\t\t");
                            break;
                        case Cell.CELL_TYPE_NUMERIC:    //field that represents number cell type
                            System.out.print("num"+cell.getNumericCellValue() + "\t\t\t");
                            break;
                        case Cell.CELL_TYPE_FORMULA:    //field that represents formula cell type
                            // Handle formula cell type if needed
                            System.out.print("Formula Cell: " + cell.getCellFormula() + "\t\t\t");
                            break;
                        default:
                            // Print the cell type for debugging
                            System.out.print("Unsupported Cell Type: " + cell.getCellType() + "\t\t\t");
                            // You may want to handle or skip this type of cell based on your requirements
                    }
                }
                System.out.println("");  // Move to the next line after printing each row
            }


            // Get unique values from the first column
            Set<String> uniqueValues = getUniqueValues(originalSheet, 2);
         // Iterate through unique values and create sheets
            for (String sheetName : uniqueValues) {
                try {
                    // Check if a sheet with the given name already exists
                    int sheetIndex = getSheetIndex(workbook, sheetName);
                    if (sheetIndex == -1) {
                        // Create a new sheet
                        Sheet newSheet = workbook.createSheet(sheetName);
                    } else {
                        System.out.println("Sheet with name '" + sheetName + "' already exists at index " + sheetIndex);
                        // Handle or skip this case based on your requirements
                    }

                } catch (Exception e) {
                    System.out.println("Something went wrong.");
                    e.printStackTrace();
                }
            }


//            // Iterate through unique values and create sheets
//            for (String sheetName : uniqueValues) {
//                try {
//                    // Check if a sheet with the given name already exists
//                    if (getSheetIndex(workbook, sheetName) == -1) {
//                        // Create a new sheet
//                        Sheet newSheet = workbook.createSheet(sheetName);
//                    } else {
//                        System.out.println("Sheet with name '" + sheetName + "' already exists.");
//                        // Handle or skip this case based on your requirements
//                    }
//
//                } catch (Exception e) {
//                    System.out.println("Something went wrong.");
//                    e.printStackTrace();
//                }
//            }

            // Save the changes back to the Excel file
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            workbook.close();

            System.out.println("Sheets created based on unique values in the first column successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to get unique values from a column
    private static Set<String> getUniqueValues(Sheet sheet, int columnIndex) {
        Set<String> uniqueValues = new HashSet<>();
        for (Row row : sheet) {
            Cell cell = row.getCell(columnIndex);
            if (cell != null && cell.getCellTypeEnum() == CellType.STRING) {
                uniqueValues.add(cell.getStringCellValue());
            }
        }
        return uniqueValues;
    }

    // Helper method to get the index of a sheet by name
    private static int getSheetIndex(Workbook workbook, String sheetName) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (workbook.getSheetName(i).equalsIgnoreCase(sheetName)) {
                return i;
            }
        }
        return -1; // Return -1 if sheet not found
    }
}
