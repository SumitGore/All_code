package com.demo.demo;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class test6 {

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

            // Get the title row from the original sheet
            Row titleRow = originalSheet.getRow(2);

            // Check if the title row is available
            if (titleRow != null) {
                // Check if column 5 is empty in the title row
                Cell titleCellInColumn5 = titleRow.getCell(4); // Assuming the specified column index is 4 (0-based)
                if (titleCellInColumn5 == null || titleCellInColumn5.getCellTypeEnum() == CellType.BLANK) {
                    System.out.println("Column 5 is empty in the title row. Skipping sheet creation.");
                } else {
                    // Iterate through unique values and create sheets
                    for (String sheetName : uniqueValues) {
                        try {
                            // Check if a sheet with the given name already exists
                            int sheetIndex = getSheetIndex(workbook, sheetName);
                            Sheet newSheet;

                            if (sheetIndex == -1) {
                                // Create a new sheet
                                newSheet = workbook.createSheet(sheetName);

                                // Copy title row from the original sheet and paste as header row in the new sheet
                                Row headerRow = newSheet.createRow(0);
                                copyRow(titleRow, headerRow);

                                // Add more cells if needed
                            } else {
                                System.out.println("Sheet with name '" + sheetName + "' already exists at index " + sheetIndex);
                                // Get the existing sheet if it already exists
                                newSheet = workbook.getSheetAt(sheetIndex);
                            }

                         // Add rows to the new sheet based on the unique value
                            for (Row row : originalSheet) {
                                // Check if column 5 is empty
                                Cell cellInColumn5 = row.getCell(4); // Assuming the specified column index is 4 (0-based)
                                if (cellInColumn5 == null || cellInColumn5.getCellTypeEnum() == CellType.BLANK) {
                                    // Skip the row if column 5 is empty
                                    continue;
                                }

//                             

                                Cell cell = row.getCell(2); // Assuming the specified column index is 2
                                if (cell != null && cell.getCellTypeEnum() == CellType.STRING
                                        && cell.getStringCellValue().equals(sheetName)) {
                                    // Create a new row in the new sheet and copy the values from the original row
                                    Row newRow = newSheet.createRow(newSheet.getLastRowNum() + 1);
                                    copyRow(row, newRow);

                                }
                            }


                        } catch (Exception e) {
                            System.out.println("Something went wrong.");
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                System.out.println("Title row not available. Skipping sheet creation.");
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
        for (int i = 0;  i < workbook.getNumberOfSheets(); i++) {
            if (workbook.getSheetName(i).equalsIgnoreCase(sheetName)) {
                return i;
            }
        }
        return -1; // Return -1 if sheet not found
    }

//    // Helper method to copy values from one row to another
//    private static void copyRow(Row sourceRow, Row targetRow) {
//        for (int i = 0; i < sourceRow.getPhysicalNumberOfCells(); i++) {
//            Cell sourceCell = sourceRow.getCell(i);
//            Cell targetCell = targetRow.createCell(i);
//           if (sourceCell != null) {
//        	   if (sourceCell.getCellTypeEnum() == CellType.STRING
//             		  // Iterate through cells in the row and replace "NA" with 0
//                     && "NA".equalsIgnoreCase(sourceCell.getStringCellValue())) {
//                 // Replace "NA" with 0
//                 targetCell.setCellValue(0.00);
//                 
//             } else {
//                 // Copy other cell values as is
//                switch (sourceCell.getCellTypeEnum()) {
//                    case STRING:
//                        targetCell.setCellValue(sourceCell.getStringCellValue());
//                        break;
//                    case NUMERIC:
//                        targetCell.setCellValue(sourceCell.getNumericCellValue());
//                        break;
//                    // Handle other cell types as needed
//                }
//            }
//            // Check if column 5 is empty
//            if (i == 4 && (sourceCell == null || sourceCell.getCellTypeEnum() == CellType.BLANK)) {
//                // Skip the row if column 5 is empty
//                return;
//            }
//        }
//    }
//}
    
 // Helper method to copy values from one row to another
    private static void copyRow(Row sourceRow, Row targetRow) {
        for (int i = 0; i < sourceRow.getPhysicalNumberOfCells(); i++) {
            Cell sourceCell = sourceRow.getCell(i);
            Cell targetCell = targetRow.createCell(i);

            if (sourceCell != null) {
                // Check if column 5 is empty
                if (i == 4 && (sourceCell.getCellTypeEnum() == CellType.BLANK)) {
                    // Skip the row if column 5 is empty
                    return;
                }

                if (sourceCell.getCellTypeEnum() == CellType.STRING
                        && "NA".equalsIgnoreCase(sourceCell.getStringCellValue())) {
                    // Replace "NA" with 0
                    targetCell.setCellValue(0.00);
                } else {
                    // Copy other cell values as is
                    switch (sourceCell.getCellTypeEnum()) {
                        case STRING:
                            targetCell.setCellValue(sourceCell.getStringCellValue());
                            break;
                        case NUMERIC:
                            targetCell.setCellValue(sourceCell.getNumericCellValue());
                            break;
                        // Handle other cell types as needed
                    }
                }
            }
        }
    }

}
