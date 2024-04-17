package com.demo.demo;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class test4 {

    public static void main(String[] args) {
        String filePath = "D:\\file_example_XLSX_10.xlsx";
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
                    

                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_STRING:    //field that represents string cell type
                          System.out.print(cell.getStringCellValue() + "\t\t\t");
                            break;
                        case Cell.CELL_TYPE_NUMERIC:    //field that represents number cell type
                            System.out.print(cell.getNumericCellValue() + "\t\t\t");
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

            // Get unique values from the specified column
            Set<String> uniqueValues = getUniqueValues(originalSheet, 2);

            // Iterate through unique values and create sheets
            for (String sheetName : uniqueValues) {
                try {
                    // Check if a sheet with the given name already exists
                    int sheetIndex = getSheetIndex(workbook, sheetName);
                    Sheet newSheet;
                    if (sheetIndex == -1) {
                        // Create a new sheet
                        newSheet = workbook.createSheet(sheetName);
                        
                        
                        
                    } else {
                        System.out.println("Sheet with name '" + sheetName + "' already exists at index " + sheetIndex);
                        // Get the existing sheet if it already exists
                        newSheet = workbook.getSheetAt(sheetIndex);
                    }

                    // Add rows to the new sheet based on the unique value
                    for (Row row : originalSheet) {
                        Cell cell = row.getCell(2); // Assuming the specified column index is 2
                        if (cell != null && cell.getCellTypeEnum() == CellType.STRING
                                && cell.getStringCellValue().equals(sheetName)) {
                            // Create a new row in the new sheet and copy the values from the original row
                            Row newRow = newSheet.createRow(newSheet.getLastRowNum() + 1);
                         // Helper method to copy values from one row to another
                            for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
                                Cell originalCell = row.getCell(i);
                                Cell newCell = newRow.createCell(i);
                                if (originalCell != null) {
                                    switch (originalCell.getCellTypeEnum()) {
                                        case STRING:
                                            newCell.setCellValue(originalCell.getStringCellValue());
                                            break;
                                        case NUMERIC:
                                            newCell.setCellValue(originalCell.getNumericCellValue());
                                            break;
                                        // Handle other cell types as needed
                                    }
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Something went wrong.");
                    e.printStackTrace();
                }
            }

            // Save the changes back to the Excel file
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            workbook.close();

            System.out.println("Sheets created and rows added successfully.");

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
