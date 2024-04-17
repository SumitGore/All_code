package com.demo.demo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.thingsboard.rest.client.RestClient;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class comb {

  //  private static String jwtToken = getJWtToken();

    public static void main(String[] args) {
        String filePath = "D:\\CRF_All_Customers_CalculatedTags.xlsx";

        try {
            // Read existing Excel file
            FileInputStream fileInputStream = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fileInputStream);

            // Get the first sheet
            Sheet originalSheet = workbook.getSheetAt(0);

            // Date timestamp for start and end (24 hours)
            LocalDateTime start = LocalDateTime.now();
            Timestamp sqlTimestamp = Timestamp.valueOf(start);

            LocalDateTime end = start.minusDays(1);
            Timestamp sqlTimestamp1 = Timestamp.valueOf(end);

            // Iterate through each row
            for (Row row : originalSheet) {
                Cell nameCell = row.getCell(2);

                if (nameCell != null && nameCell.getCellTypeEnum() == CellType.STRING) {
                    String customerName = nameCell.getStringCellValue();
                    Cell cellInColumn5 = row.getCell(3);

                    if ("Dashboard not available".equalsIgnoreCase(cellInColumn5.getStringCellValue()) ||
                            "Dashboard not available ".equalsIgnoreCase(cellInColumn5.getStringCellValue()) ||
                            "Dashboard not available".equalsIgnoreCase(cellInColumn5.getStringCellValue())) {
                        continue;
                    }

                    // Create a new sheet for each customerName
                    Sheet newSheet = workbook.createSheet(customerName);

                    // Copy title row from the original sheet and paste as header row in the new sheet
                    Row titleRow = originalSheet.getRow(2);
                    Row headerRow = newSheet.createRow(0);
                    copyRow(titleRow, headerRow);

                    // Create a new row in the new sheet and copy the values from the original row
                    Row newRow = newSheet.createRow(1);
                    copyRow(row, newRow);

                    // API calls and further processing
                    List<String> tags = new ArrayList<>();
                    for (int i = 3; i < row.getLastCellNum(); i++) {
                        Cell tagCell = row.getCell(i);
                        if (tagCell != null) {
                            switch (tagCell.getCellTypeEnum()) {
                                case STRING:
                                    if ("NA".equalsIgnoreCase(tagCell.getStringCellValue())) {
                                        // Handle NA case if needed
                                    } else {
                                        tags.add(tagCell.getStringCellValue());
                                    }
                                    break;
                                case NUMERIC:
                                    tags.add(String.valueOf(tagCell.getNumericCellValue()));
                                    break;
                            }
                        }
                    }

                    // API calls and further processing
                    // Call your API methods here using customerName and tags
                    // ...

                    // Save the changes back to the Excel file
                    FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                    workbook.write(fileOutputStream);
                    fileOutputStream.close();
                }
            }

            workbook.close();

            System.out.println("Sheets created and processed successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
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
    // Other utility methods...

}
