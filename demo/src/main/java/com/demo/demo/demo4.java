package com.demo.demo;

	import java.io.FileInputStream;
	import java.io.FileOutputStream;
	import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
	import java.util.ArrayList;
	import java.util.HashMap;
	import java.util.List;
	import java.util.Map;

	import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
	import org.apache.poi.ss.usermodel.Sheet;
	import org.apache.poi.ss.usermodel.Workbook;
	import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookType;
import org.thingsboard.rest.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
	import com.fasterxml.jackson.databind.ObjectMapper;

public class demo4 {
	public static String jwtToken = getJWtToken();
	    public static void main(String[] args) {
	        String filePath = "D:\\CRF(v2.2)_Customers_CalculatedTags1.xlsx";
	      //  String filePath1="D:\\\\CRF(v2.2)_Customers_TagsValues1111.xlsx";
 
	        try {
	            // Read existing Excel file
	            FileInputStream fileInputStream = new FileInputStream(filePath);
	          //  FileOutputStream fileOutput = new FileOutputStream(filePath1);
	            Workbook workbook = new XSSFWorkbook(fileInputStream);
	            //Workbook newWorkbook = new XSSFWorkbook(fileOutput); // New workbook for appending data

	            // Get the first sheet
	            Sheet originalSheet = workbook.getSheetAt(0);

	            // Date timestamp for start and end (24 hours)
	            LocalDate start = LocalDate.now(); // Date start time now

	            // Map to store customer names as keys and their tags as values
	            Map<String, Map<List<String>, List<String>>> customerTagMap = new HashMap<>();

	            // Fetch customer names and tags from the sheet
	            for (Row row : originalSheet) {
	                Cell nameCell = row.getCell(2); // Assuming customer names are in the third column (index 2)

	                if (nameCell != null && nameCell.getCellTypeEnum() == CellType.STRING) {
	                    String customerName = nameCell.getStringCellValue();
	                    Cell cellInColumn5 = row.getCell(3);

	                    // Dashboard not available tag is removing
	                    if ("Dashboard not available".equalsIgnoreCase(cellInColumn5.getStringCellValue())
	                            || "Dashboard not available ".equalsIgnoreCase(cellInColumn5.getStringCellValue())
	                            || "Dashboard not availabe".equalsIgnoreCase(cellInColumn5.getStringCellValue())) {
	                        continue;
	                    }

	                    // All Row tag ( Get all remaining cells as tags)
	                    ArrayList<String> tags = new ArrayList<>();
	                    ArrayList<String> headings = new ArrayList<>();

	                    // Copy title row from the original sheet and paste as header row in the new sheet
	                    Row sourceRow = originalSheet.getRow(2);

	                    for (int i = 3; i < row.getLastCellNum(); i++) {
	                        Cell tagCell = row.getCell(i);
	                        if (tagCell != null) {
	                            switch (tagCell.getCellTypeEnum()) {
	                                case STRING:
	                                    if ("NA".equalsIgnoreCase(tagCell.getStringCellValue())) {
	                                        // Handle NA case if needed
	                                    } else {
	                                        tags.add(tagCell.getStringCellValue());
	                                        Cell sourceCell = sourceRow.getCell(i);
	                                        headings.add(sourceCell.getStringCellValue());
	                                    }
	                                    break;
	                                case NUMERIC:
	                                    tags.add(String.valueOf(tagCell.getNumericCellValue()));
	                                    Cell sourceCell = sourceRow.getCell(i);
	                                    headings.add(sourceCell.getStringCellValue());
	                                    break;
	                            }
	                        }
	                    }

	                    // Update the map with customer name and tags
	                    Map<List<String>, List<String>> tagsHeadings = new HashMap<>();
	                    tagsHeadings.put(tags, headings);
	                    customerTagMap.put(customerName, tagsHeadings);
	                }
	            }

	     

		        // FileOutputStream fileOutputStream1 = new FileOutputStream("D:\\CRF(v2.2)_Customers_TagsValues1111.xlsx");


	         // Create header row outside the loop
	         Row headerRow = null;

	         // Inside the loop where you iterate over customerTagMap entries
	         for (Map.Entry<String, Map<List<String>, List<String>>> entry : customerTagMap.entrySet()) {

	             String customerName = entry.getKey();
	             Map<List<String>, List<String>> tagsEntry = entry.getValue();

	             for (Map.Entry<List<String>, List<String>> tagValues : tagsEntry.entrySet()) {
	                 List<String> tags = tagValues.getKey();
	                 List<String> headings = tagValues.getValue();

	                 // Modify the sheet name to remove or replace characters that are not allowed
	                 String modifiedCustomerName = modifySheetName(customerName);

	                 
                 // Check if the sheet with the modified name already exists
	                 Sheet customerSheet = null;
//	                 for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
//	                     if (workbook.getSheetAt(sheetIndex).getSheetName().equalsIgnoreCase(modifiedCustomerName)) {
//	                         customerSheet = workbook.getSheetAt(sheetIndex);
//	                         
//	                     }
//	                 }
//
//	                 System.out.println("Customer: " + customerName + ", Modified Name : " + modifiedCustomerName + ", Sheet: " + customerSheet);
//                       System.out.println("workbook"+ workbook);
//                       
//	              // If the sheet already exists, you can append data to the next available row
//	                 if (customerSheet!=null) {
	                 
	                 boolean sheetFound = false;
	                 for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
	                     if (workbook.getSheetAt(sheetIndex).getSheetName().equalsIgnoreCase(modifiedCustomerName)) {
	                         customerSheet = workbook.getSheetAt(sheetIndex);
	                         sheetFound = true;
	                         break;
	                     }
	                 }

	                 System.out.println("Customer: " + customerName + ", Modified Name : " + modifiedCustomerName + ", Sheet: " + customerSheet);

	                 // If the sheet already exists, you can append data to the next available row
	                 if (sheetFound) {
	                     int nextRowIndex = customerSheet.getLastRowNum() + 1;
	                     Row newRow = customerSheet.createRow(nextRowIndex);
	                   
	                     System.out.println("qqqqq122 : "+newRow);
	                 
	                    // int nextRowIndex = customerSheet.getLastRowNum() + 1;

	                     // Fetch customer details and devices
	                     String customerDetails = callApi(customerName);
	                     String keyToFind = "id";

	                     try {
	                         ObjectMapper objectMapper = new ObjectMapper();
	                         JsonNode jsonNode = objectMapper.readTree(customerDetails);

	                         String custId = printJsonNode(jsonNode, keyToFind);

	                         if (!"na".equals(custId)) {
	                             String devices = getCustomerDevices(custId);
	                             List<String> deviceIds = printJsonNode2(objectMapper.readTree(devices), "id");
	                             List<String> deviceName = printJsonNode3(objectMapper.readTree(devices), "name");

	                             // Create cells for "Date" and "Asset" only once
	                             if (headerRow == null) {
	                                 headerRow = customerSheet.createRow(0);
	                                 headerRow.createCell(0).setCellValue("Date");
	                                 headerRow.createCell(1).setCellValue("Asset");
	                                 int idx = 2;
	                                 for (String heading : headings) {
	                                     Cell cell = headerRow.createCell(idx);
	                                     cell.setCellValue(heading);
	                                     idx++;
	                                 }
	                             }

	                             for (int i = 0; i < deviceIds.size(); i++) {
	                                 // Create a new row for each device
	                               //  Row newRow = customerSheet.createRow(nextRowIndex);

	                                 // Update the row cells for each device
	                                 newRow.createCell(0).setCellValue(start.toString());
	                                 newRow.createCell(1).setCellValue(deviceName.get(i).replaceAll("\"", ""));

	                                 String deviceId = deviceIds.get(i);
	                                 String deviceValues = getDeviceValues(deviceId, String.join(",", tags));

	                                 JsonNode jsonNodeValues = objectMapper.readTree(deviceValues);
	                                 List<Double> tagValuesAPI = new ArrayList<>();

	                                 for (String key : tags) {
	                                     JsonNode keyNode = jsonNodeValues.get(key);
	                                     String value = keyNode.get(0).get("value").toString();
	                                     if (value.equals("null")) {
	                                         tagValuesAPI.add(0.0);
	                                     } else {
	                                         value = value.replaceAll("\"", "");
	                                         Double val = Double.parseDouble(value);
	                                         tagValuesAPI.add(val);
	                                     }
	                                 }

	                                 // Append values to the existing sheet
	                                 addToExcel(2, newRow, tagValuesAPI);

	                                 // Increment nextRowIndex for the next iteration
	                                 nextRowIndex++;
	                             }
	                         } else {
	                             System.out.println("Customer ID not available");
	                         }
	                     } catch (Exception e) {
	                         e.printStackTrace();
	                     }
	                 }
	                 
	                 else {
	                     // If the sheet doesn't exist, create a new sheet and add data to the first row
	                     customerSheet = workbook.createSheet(modifiedCustomerName);

	                     System.out.println("Creating new sheet: " + customerSheet.getSheetName());

	                     // Create header row for the new sheet
	                     headerRow = customerSheet.createRow(0);
	                     headerRow.createCell(0).setCellValue("Date");
	                     headerRow.createCell(1).setCellValue("Asset");
	                     int idx = 2;
	                     for (String heading : headings) {
	                         Cell cell = headerRow.createCell(idx);
	                         cell.setCellValue(heading);
	                         idx++;
	                     }

	                     // Fetch customer details and devices
	                     String customerDetails = callApi(customerName);
	                     String keyToFind = "id";

	                     try {
	                         ObjectMapper objectMapper = new ObjectMapper();
	                         JsonNode jsonNode = objectMapper.readTree(customerDetails);

	                         String custId = printJsonNode(jsonNode, keyToFind);

	                         if (!"na".equals(custId)) {
	                             String devices = getCustomerDevices(custId);
	                             System.out.println("crf: "+devices);
	                             List<String> deviceIds = printJsonNode2(objectMapper.readTree(devices), "id");
	                             System.out.println("assetCRFid: "+deviceIds);
	                             List<String> deviceName = printJsonNode3(objectMapper.readTree(devices), "name");

	                             
	                             
	                             for (int i = 0; i < deviceIds.size(); i++) {
	                            	
	 					                String deviceId = deviceIds.get(i);
	 					                String deviceValues = getDeviceValues(deviceId, String.join(",", tags));
	 					
	 					                JsonNode jsonNodeValues = objectMapper.readTree(deviceValues);
	 					                List <Double> tagValuesAPI = new ArrayList<Double>();
	 				                    
	 						            for (String key : tags) {
	 					                   JsonNode keyNode = jsonNodeValues.get(key);
	 					                   String value = keyNode.get(0).get("value").toString();
	 					                   if (value.equals("null")){
	 					                	   tagValuesAPI.add(0.0);
	 					                   }else {
	 					                	   value = value.replaceAll("\"", "");
	 					                	   Double val = Double.parseDouble(value);
	 					                	   tagValuesAPI.add(val);
	 					                   }
	 						            }
	 						            
	                                 // Append values to the new sheet
	                                 Row newRow = customerSheet.createRow(customerSheet.getLastRowNum() + 1);
	                                 newRow.createCell(0).setCellValue(start.toString());
	                                 newRow.createCell(1).setCellValue(deviceName.get(i).replaceAll("\"", ""));
	                                // List<Double> tagValuesAPI = getTagValues(jsonNode, tags);
	                                 addToExcel(2, newRow, tagValuesAPI);
	                             }
	                         } else {
	                             System.out.println("Customer ID not available");
	                         }
	                     } catch (Exception e) {
	                         e.printStackTrace();
	                     }
	                 }
	             }
	         }

	         // Save the changes back to the Excel file
	         FileOutputStream fileOutputStream = new FileOutputStream("D:\\CRF(v2.2)_Customers_TagsValues111.xlsx");
	         workbook.write(fileOutputStream);
	         fileOutputStream.close();
	         workbook.close();

	         System.out.println("Sheet processed successfully.");

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	  
	    
 private static void addToExcel1(int startingColumnIndex, Row row, List<Double> values) {
	        for (int i = 0; i < values.size(); i++) {
	            Cell cell = row.createCell(i + startingColumnIndex);
	            cell.setCellValue(values.get(i));
	        }
	    }
	
private static List<String> printJsonNode3(JsonNode jsonNode, String keyToFindInDevices) {
    // Extract and print the value for the specified key
    JsonNode dataArray = jsonNode.get("data");
    ArrayList<String> nameList = new ArrayList<>();

    if (dataArray != null && dataArray.isArray() && dataArray.size() > 0) {
        for (JsonNode deviceNode : dataArray) {
            JsonNode nameNode = deviceNode.get("name");
            nameList.add(nameNode.toString());
           // System.out.println("aaaaaa"+ nameNode.asText());

        }
        
}
    return nameList;
}



private static void addToExcel(int idx, Row headerRow, List<Double> headings) {

    for (double heading : headings) {
    	
    	Cell cell = headerRow.createCell(idx);
    	cell.setCellValue(heading);
    	idx++;
    }
}

private static String modifySheetName(String originalName) {
    // Replace characters that are not allowed in Excel sheet names
    return originalName.replaceAll("[^a-zA-Z0-9]+", "_");
}

private static String getJWtToken() {
	String username = "testfm@forbesmarshall.com";
	String password = "testfm@123";
	String url = "https://eversense.forbesmarshall.com";
	 
	RestClient client = new RestClient(url);
	client.login(username, password);
	String token = client.getToken();
	// Perform logout of current user and close client
			client.logout();
			client.close();
	return token;
	
}

private static String callApi(String customerName) {
	// ThingsBoard REST API URL
   	// Replace the placeholder token with your actual JWT token
   	
    
    String response = "";

    try {
        HttpClient client = HttpClient.newHttpClient();

        // Encode the customerName before appending to the URL
        String encodedCustomerName = URLEncoder.encode(customerName, StandardCharsets.UTF_8.toString());

        // Replace the placeholder URL with your actual API endpoint and encoded customerTitle
        String apiUrl = "https://eversense.forbesmarshall.com:443/api/tenant/customers?customerTitle=" + encodedCustomerName;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("accept", "application/json")
                .header("X-Authorization", "Bearer " + jwtToken)
                .GET()
                .build();

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Print the response
//        System.out.println("Response Code: " + httpResponse.statusCode());
    //    System.out.println("customer Response Body: " + httpResponse.body());

        response = httpResponse.body();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return response;
}
   private static String printJsonNode(JsonNode jsonNode, String keyToFind) {
   // Extract and print the value for the specified key
   JsonNode valueNode = jsonNode.get(keyToFind);
  
   if (valueNode != null) {
       JsonNode id = valueNode.get(keyToFind);
      
       if (id != null) {
           return id.toString();
       } else {
           return "na";
       }
   } else {
       return "na";
   }
}

private static ArrayList<String> printJsonNode2(JsonNode jsonNode, String keyToFindInDevices) {
    // Extract and print the value for the specified key
     JsonNode dataArray = jsonNode.get("data");
     ArrayList<String> idList = new ArrayList<>();
    if (dataArray != null && dataArray.isArray() && dataArray.size() > 0) {
        for (JsonNode deviceNode : dataArray) {
            JsonNode idNode = deviceNode.get("id");
           
            if (idNode != null && idNode.has("id")) {
                String deviceId = idNode.get("id").asText();
             
                 idList.add(deviceId);
            }
        }
     
    }

return idList;
 
}

 private static String getCustomerDevices(String custId) {
// Remove double quotes from custId
    custId = custId.replaceAll("\"", "");

    String response = "";

    try {
        HttpClient client = HttpClient.newHttpClient();

        // Replace the placeholder URL with your actual API endpoint and customer ID
        String deviceId = "https://eversense.forbesmarshall.com:443/api/customer/"+custId+"/assets?pageSize=500&page=0";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(deviceId))
                .header("accept", "application/json")
                .header("X-Authorization", "Bearer " + jwtToken)
                .GET()
                .build();

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        response = httpResponse.body();

        // Use ObjectMapper to parse the JSON response
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNodeDevices = objectMapper.readTree(response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    } catch (Exception e) {
        System.err.println("rrrrrr");
    }
        return response;
}

private static String getDeviceValues(String deviceId, String tags) {
   String response = "";
   deviceId = deviceId.replaceAll("\"", "");
   try {
	   String encodedDevice =  URLEncoder.encode(deviceId, StandardCharsets.UTF_8.toString());
	   String encodedTags =  URLEncoder.encode(tags, StandardCharsets.UTF_8.toString());
	
	   HttpClient client = HttpClient.newHttpClient();
	   String apiUrl = "https://eversense.forbesmarshall.com:443/api/plugins/telemetry/" + "ASSET" + "/" + encodedDevice + "/values/timeseries?" + "keys=" + encodedTags;
	   HttpRequest request = HttpRequest.newBuilder()
               .uri(URI.create(apiUrl))
               .header("accept", "application/json")
               .header("X-Authorization", "Bearer " + jwtToken)
               .GET()
               .build();

       HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
       
       response = httpResponse.body();
       return response;
   }catch (Exception e) {
	return "Exception"+e;
   }
   }

}

