package com.demo.demo;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.thingsboard.rest.client.RestClient;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.query.BooleanFilterPredicate;
import org.thingsboard.server.common.data.query.EntityData;
import org.thingsboard.server.common.data.query.EntityDataPageLink;
import org.thingsboard.server.common.data.query.EntityDataQuery;
import org.thingsboard.server.common.data.query.EntityDataSortOrder;
import org.thingsboard.server.common.data.query.EntityKey;
import org.thingsboard.server.common.data.query.EntityKeyType;
import org.thingsboard.server.common.data.query.EntityKeyValueType;
import org.thingsboard.server.common.data.query.EntityTypeFilter;
import org.thingsboard.server.common.data.query.FilterPredicateValue;
import org.thingsboard.server.common.data.query.KeyFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
public class think1 {

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
                                    if ("NA".equalsIgnoreCase(tagCell.getStringCellValue())) {
                                        // Handle NA case if needed
                                    } else {
                                        tags.add(tagCell.getStringCellValue());
                                    }

                                    break;
                                case NUMERIC:
                                    tags.add(String.valueOf(tagCell.getNumericCellValue()));
                                    break;
                                //  Handle other cell types as needed
                            }
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
                // API call using customerName
                String customerDevice = callApi(customerName);
                System.out.println("Customer: " + customerName);
            }

            // Further processing... remaining code

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

   private static String callApi(String customerName) {
    	// ThingsBoard REST API URL
	   	// Replace the placeholder token with your actual JWT token
        String jwtToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJldmVyc2Vuc2V0cmFpbmVlQGZvcmJlc21hcnNoYWxsLmNvbSIsInVzZXJJZCI6Ijc3Nzc5ZjQxLTZmMjctMTFlZS05ZTQyLTVmMDA1NzFhMGE4ZiIsInNjb3BlcyI6WyJURU5BTlRfQURNSU4iXSwic2Vzc2lvbklkIjoiNGE5NDUyMzYtMmVlNS00YWEwLTk3OGUtNDg3NDM5Y2FkNzg5IiwiaXNzIjoidGhpbmdzYm9hcmQuaW8iLCJpYXQiOjE3MDExNjQ2MTIsImV4cCI6MTcwMTE3MzYxMiwiZW5hYmxlZCI6dHJ1ZSwiaXNQdWJsaWMiOmZhbHNlLCJ0ZW5hbnRJZCI6IjAxNGI3ZjMwLWY0YmUtMTFlYS05N2I1LWUxOGIzNTA4OTJiNSIsImN1c3RvbWVySWQiOiIxMzgxNDAwMC0xZGQyLTExYjItODA4MC04MDgwODA4MDgwODAifQ.aRayrzfJ7owjjhRNC0ogGraOyNeHDUK_OjbptCfLHWMNcfXERCNNdIQqtITcg_8bYi2xEGJBzZtO4ilShmbvfg";
        String res = "";
        try {
            HttpClient client = HttpClient.newHttpClient();

            // Encode the customerName before appending to the URL
            String encodedCustomerName = URLEncoder.encode(customerName, StandardCharsets.UTF_8.toString());
            // Replace the placeholder URL with your actual API endpoint and customerTitle
            String apiUrl = "https://eversense.forbesmarshall.com:443/api/tenant/customers?customerTitle="+encodedCustomerName;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("accept", "application/json")
                    .header("X-Authorization", "Bearer " + jwtToken)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Print the response
            System.out.println("Response Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
            res = response.body();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(res);

                // Now you can work with the JsonNode object
                printJsonNode(jsonNode);
              //  System.out.println("Parsed JSON object: " + jsonNode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        
//           System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
		return res;
    }


   private static void printJsonNode(JsonNode jsonNode) {
	    if (jsonNode.isObject()) {
	    	 // Iterate through each field and print key-value pairs
	        jsonNode.fields().forEachRemaining(entry -> {
	            //System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
	            // Recursive call for nested nodes
	            printJsonNode(entry.getValue());
	        });
	    } else if (jsonNode.isArray()) {
	        for (JsonNode arrayElement : jsonNode) {
	            // Recursive call for array elements
	            printJsonNode(arrayElement);
	        }
	    } else if (jsonNode.isValueNode()) {
	        System.out.println("Value: " + jsonNode.asText());
	    }
	}
 }



