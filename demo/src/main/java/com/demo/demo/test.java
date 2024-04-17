package com.demo.demo;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class test {

    public static void main(String[] args) {
        String filePath = "D:\\CRF_All_Customers_CalculatedTags.xlsx";
        try {
            // Read existing Excel file
            FileInputStream fileInputStream = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fileInputStream);

            // Get the first sheet
         
            
           
            Sheet sheet = workbook.getSheetAt(0);
            
            
            // code to check all columns and row
            
            
 
            
            
//            System.out.print("workbook"+ workbook + sheet);
            String[] n1 = new String[]{"name1", "name2", "name3"};

            for (String sheetName : n1) {
                try {
                    // Check if a sheet with the given name already exists
                    if (getSheetIndex(workbook, sheetName) == -1) {
                        // Create a new sheet
                        Sheet newSheet = workbook.createSheet(sheetName);
                    } else {
                        System.out.println("Sheet with name '" + sheetName + "' already exists.");
                        // Handle or skip this case based on your requirements
                    }

                } catch (Exception e) {
                    System.out.println("Something went wrong.");
                    e.printStackTrace();
                }
            }

            // Fetch customer names from the "Name" column in the first sheet
            Iterator<Row> iterator = sheet.iterator();
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

            // Save the changes back to the Excel file
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            workbook.close();

            System.out.println("Customer data written to a new sheet successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
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
