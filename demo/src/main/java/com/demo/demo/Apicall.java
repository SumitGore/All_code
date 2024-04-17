package com.demo.demo;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.thingsboard.rest.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class Apicall {
	public static String jwtToken = getJWtToken();
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
//            System.out.println("start in milliseconds: " + milliseconds);

            LocalDateTime end = start.minusDays(1);  // end start time now (-24 hours)
            Timestamp sqlTimestamp1 = Timestamp.valueOf(end);
            // Convert end Timestamp to milliseconds
            long milliseconds1 = sqlTimestamp1.getTime();
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
                String customerDetails = callApi(customerName);
                // Specify the key you want to find in the JSON response
                String keyToFind = "id"; // Replace with the actual key you want to find
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(customerDetails);

                    // Print value for the specified key
                    String custId = printJsonNode(jsonNode, keyToFind);
                   // System.out.println(custId+ "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                    String customerDevices = getCustomerDevices(custId);
                    JsonNode jsonNodeDevices = objectMapper.readTree(customerDevices);
//                    System.out.println("Customer ID: " + custId);
                    
                    

                    // Use the customer ID to get devices
                    if (!"na".equals(custId)) {
                        String devices = getCustomerDevices(custId);
                        System.out.println("Devices: " + devices);
                    } else {
                        System.out.println("Customer ID not available");
                    }
                    
                    //getting Device id ,tag values 
                    

		            // Specify the key you want to find in the devices JSON response
		            String keyToFindInDevices = "id";

		            // Print value for the specified key in the devices JSON response
		        
		            ArrayList<String> deviceId = printJsonNode2(jsonNodeDevices, keyToFindInDevices);
//		            System.out.println("Device ID111: " + deviceId);
		            // device values put in tag
		            for (int i=0;i<deviceId.size();i++) {
		            	String deviceValues =getDeviceValues(deviceId.get(i),  String.join(",", tags));
		            	System.out.println(deviceValues);
		            }
					

                } catch (Exception e) {
                    e.printStackTrace();
                }
            
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
//	        System.out.println("Response Code: " + httpResponse.statusCode());
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
      
      // JsonNode id = valueNode.get(keyToFind);
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
	    //  System.out.println(dataArray+"((((((((((((((((((((((((((((((((((");
	    //  System.out.println("aaaaaaaaa" + dataArray.size());

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

        // Print all collected ids
//        System.out.println("Device IDs: " + String.join(", ", idList));

        return idList;
    }
   
   
   
   private static String getCustomerDevices(String custId) {
	// Remove double quotes from custId
	    custId = custId.replaceAll("\"", "");

	    String response = "";

	    try {
	        HttpClient client = HttpClient.newHttpClient();

	        // Replace the placeholder URL with your actual API endpoint and customer ID
	        String deviceId = "https://eversense.forbesmarshall.com:443/api/customer/" + custId + "/devices?pageSize=2100&page=0";
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
		// TODO Auto-generated method stub
	   String response = "";
	   deviceId = deviceId.replaceAll("\"", "");
//	   tags = tags.replaceAll("\"", "");
	  // System.out.println(tags);
	   try {
		   String encodedDevice =  URLEncoder.encode(deviceId, StandardCharsets.UTF_8.toString());
		   String encodedTags =  URLEncoder.encode(tags, StandardCharsets.UTF_8.toString());
		  // System.out.println(encodedTags+"******************");
		   HttpClient client = HttpClient.newHttpClient();
		   
		   String apiUrl = "https://eversense.forbesmarshall.com:443/api/plugins/telemetry/" + "DEVICE" + "/" + encodedDevice + "/values/timeseries?" + "keys=" + encodedTags;
		  // System.out.println(apiUrl+"######################");
		   HttpRequest request = HttpRequest.newBuilder()
	               .uri(URI.create(apiUrl))
	               .header("accept", "application/json")
	               .header("X-Authorization", "Bearer " + jwtToken)
	               .GET()
	               .build();

	       HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
	       
	       response = httpResponse.body();
	       //System.out.println(response+":::::::::::::::::::::::::::");
	       return response;
	   }catch (Exception e) {
		return "Exception"+e;
	   }
	   }

}



