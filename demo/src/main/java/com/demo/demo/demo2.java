package com.demo.demo;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.thingsboard.rest.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

public class demo2 {
	public static String jwtToken = getJWtToken();
	public static void main(String[] args) {
	    String filePath = "D:\\CRF(v2.2)_Customers_CalculatedTags1.xlsx";
	    String filePath1 = "D:\\CRF(v2.2)_Customers_TagsValues1.xlsx";

	    try {
	        FileInputStream fileInputStream = new FileInputStream(filePath);
	        XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
	      
	      //  FileInputStream fileInputStream1 = new FileInputStream(filePath1);
	        XSSFWorkbook workbook1 = new XSSFWorkbook(filePath1);
	        
	        if (workbook.getNumberOfSheets() == 0) {
	            System.out.println("No sheets found in the workbook.");
	            return;
	        }
	        XSSFSheet originalSheet = workbook.getSheetAt(0);

	        LocalDate start = LocalDate.now();
	        Map<String, Map<List<String>, List<String>>> customerTagMap = new HashMap<>();

	        for (Row row : originalSheet) {
	            Cell nameCell = row.getCell(2);

	            if (nameCell != null && nameCell.getCellTypeEnum() == CellType.STRING) {
	                String customerName = nameCell.getStringCellValue();
	                Cell cellInColumn5 = row.getCell(3);

	                if ("Dashboard not available".equalsIgnoreCase(cellInColumn5.getStringCellValue()) ||
	                        "Dashboard not available ".equalsIgnoreCase(cellInColumn5.getStringCellValue()) ||
	                        "Dashboard not availabe".equalsIgnoreCase(cellInColumn5.getStringCellValue())) {
	                    continue;
	                }

	                ArrayList<String> tags = new ArrayList<>();
	                ArrayList<String> headings = new ArrayList<>();

	                Row sourceRow = originalSheet.getRow(2);

	                for (int i = 3; i < row.getLastCellNum(); i++) {
	                    Cell tagCell = row.getCell(i);
	                    if (tagCell != null) {
	                        switch (tagCell.getCellTypeEnum()) {
	                            case STRING:
	                                if ("NA".equalsIgnoreCase(tagCell.getStringCellValue())) {
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

	                Map<List<String>, List<String>> tagsHeadings = new HashMap<>();
	                tagsHeadings.put(tags, headings);
	                customerTagMap.put(customerName, tagsHeadings);
	            }
	        }

	        for (Map.Entry<String, Map<List<String>, List<String>>> entry : customerTagMap.entrySet()) {
	            String customerName = entry.getKey();
	            Map<List<String>, List<String>> tagsEntry = entry.getValue();

	            for (Map.Entry<List<String>, List<String>> tagValues : tagsEntry.entrySet()) {
	                List<String> tags = tagValues.getKey();
	                List<String> headings = tagValues.getValue();
	                String modifiedCustomerName = modifySheetName(customerName);

	                XSSFSheet customerSheet = findOrCreateSheet(workbook1, modifiedCustomerName);

	                String customerDetails = callApi(customerName);
	                String keyToFind = "id";

	                try {
	                    ObjectMapper objectMapper = new ObjectMapper();
	                    JsonNode jsonNode = objectMapper.readTree(customerDetails);

	                    String custId = printJsonNode(jsonNode, keyToFind);

		                System.out.println("name:"+ customerName);

	                    System.out.println("customer ID:"+custId);
	                    if (!"na".equals(custId)) {
	                        String devices = getCustomerDevices(custId);
	                        System.out.println("Asset:"+devices);
	                        List<String> deviceIds = printJsonNode2(objectMapper.readTree(devices), "id");
	                        List<String> deviceName = printJsonNode3(objectMapper.readTree(devices), "name");

	                        System.out.println("asset id:"+deviceIds);
	                        int rowIndex = 0;
	                        Row headerRow = customerSheet.createRow(rowIndex);
	                        headerRow.createCell(0).setCellValue("Date");
	                        headerRow.createCell(1).setCellValue("Asset");

	                        int idx = 2;
	                        for (String heading : headings) {
	                            Cell cell = headerRow.createCell(idx);
	                            cell.setCellValue(heading);
	                            idx++;
	                        }

	                        for (int i = 0; i < deviceIds.size(); i++) {
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

	                            int nextRowIndex = customerSheet.getLastRowNum();
	                            Row newRow = customerSheet.createRow(nextRowIndex + 1);

	                            newRow.createCell(0).setCellValue(start.toString());
	                            newRow.createCell(1).setCellValue(deviceName.get(i).replaceAll("\"", ""));
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

	        FileOutputStream fileOutputStream = new FileOutputStream("D:\\CRF(v2.2)_Customers_TagsValues1.xlsx");
	        workbook1.write(fileOutputStream);
	        fileOutputStream.close();
	        workbook1.close();

	        System.out.println("Sheet processed successfully.");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	private static XSSFSheet findOrCreateSheet(XSSFWorkbook workbook, String sheetName) {
	    XSSFSheet customerSheet = workbook.getSheet(sheetName);

	    if (customerSheet == null) {
	      //  System.out.println("Creating sheet: " + sheetName);
	        customerSheet = workbook.createSheet(sheetName);
	    }

	    return customerSheet;
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
	       // return originalName.replaceAll(" ", "_");
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
//		        System.out.println("Response Code: " + httpResponse.statusCode());
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
		       // String deviceId = "https://eversense.forbesmarshall.com:443/api/customer/" + custId + "/devices?pageSize=2100&page=0";
		        String assetId = "https://eversense.forbesmarshall.com:443/api/customer/"+custId+"/assets?pageSize=500&page=0";
		        HttpRequest request = HttpRequest.newBuilder()
		                .uri(URI.create(assetId))
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
			  // String apiUrl1="https://eversense.forbesmarshall.com:443/api/plugins/telemetry/" + "ASSET" + "/" + encodedDevice + "/values/timeseries?" + "keys=" + encodedTags + "&" + "startTs=" + str(startTS)  + "&" + "endTs=" + str(endTS)  + "&" + "interval=" + str(interval) + "&" + "agg=" + "NONE" + "&" + "limit=" + str(limit);
			 //  String apiUrl1= "https://eversense.forbesmarshall.com:443/api/plugins/telemetry/ASSET/029187c0-50a2-11ee-b3f1-c132cb361d73/values/timeseries?keys=parameter_daily";
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

