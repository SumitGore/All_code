package com.demo.demo;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.leshan.core.Startable;
import org.thingsboard.rest.client.RestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
//import org.json.JSONArray;
//import org.json.JSONObject;
import java.io.*;

public class CodelData {
	public static String jwtToken = getJWtToken();
    public static void main(String[] args) throws EncryptedDocumentException, InvalidFormatException {
        String filePath = CodelData.class.getClassLoader().getResource("test/PCB.xlsx").getFile();
        LocalDate start = LocalDate.now();  // Date start time now 
        String currentYearMonth = start.getMonth().name()+ "_" + start.getYear();
        String fileName = "PCB_Value_" + currentYearMonth + ".xlsx";
        
        
        try {
            // Read existing Excel file
            FileInputStream fileInputStream = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fileInputStream);
           
            // Get the first sheet
            Sheet originalSheet = workbook.getSheetAt(0);
      
            // Map to store customer names as keys and their tags as values
            Map<String,Map<List<String>,List<String>>> customerTagMap = new HashMap<>();
            // Fetch customer names and tags from the sheet
            int headingLen=0;
            for (Row row : originalSheet) {
                Cell nameCell = row.getCell(0); // Assuming customer names are in the third column (index 2)
                if (nameCell != null && nameCell.getCellTypeEnum() == CellType.STRING) {
                    String customerName = nameCell.getStringCellValue();
                    Cell cellInColumn5 = row.getCell(1);
                    // Dashboard not available tag is removing
                    if ("Dashboard not available".equalsIgnoreCase(cellInColumn5.getStringCellValue()) || "Dashboard not available ".equalsIgnoreCase(cellInColumn5.getStringCellValue()) ||
                            "Dashboard not availabe".equalsIgnoreCase(cellInColumn5.getStringCellValue())) {
                        continue;
                    }

                    // All Row tag ( Get all remaining cells as tags)
                    ArrayList<String> tags = new ArrayList<>();
                    ArrayList<String> headings = new ArrayList<>();
                 //   ArrayList<String> headingAddOn = new ArrayList<>(Arrays. asList("SO2 PPM Avg. Status", "NOX PPM Avg. Status", "Detector 1 Avg. Status", "Detector 2 Avg. Status", "DUST OPACITY Avg. Status", "DUST MG/NM3 Avg. Status", "DR1 Avg. Status", "DR2 Avg. Status", "DT1 Avg. Status", "DT2 Avg. Status"));
                   
                  // Copy title row from the original sheet and paste as header row in the new sheet
                    Row sourceRow = originalSheet.getRow(1);
                

                    for (int i = 3; i < row.getLastCellNum(); i++) {
                        Cell tagCell = row.getCell(i);
                        if (tagCell != null){
                            switch (tagCell.getCellTypeEnum()) {
                                case STRING:
//                                    if ("NA".equalsIgnoreCase(tagCell.getStringCellValue())) {
//                                        // Handle NA case if needed
//                                    } else {
                                        tags.add(tagCell.getStringCellValue());
                                        Cell sourceCell = sourceRow.getCell(i);
                                        if (sourceCell != null) {
                                            headings.add(sourceCell.getStringCellValue());
                                        } else {
                                           
                                        }
//                                    }
                                    break;
                                case NUMERIC:
                                    tags.add(String.valueOf(tagCell.getNumericCellValue()));
                                    Cell sourceCell1 = sourceRow.getCell(i);
                                    if (sourceCell1 != null) {
                                        headings.add(sourceCell1.getStringCellValue());
                                    } else {

                                    }
                                    break;
                            }
                        }
                    }
 
                  //  headings.addAll(headingAddOn);
                    headingLen = headings.size();
             
                    
                    // Update the map with customer name and tags
                    Map<List<String>, List<String>> tagsHeadings = new HashMap<>();
                    tagsHeadings.put(tags, headings);
                    customerTagMap.put(customerName, tagsHeadings);
                }
            } 
         
//            if (isNewMonth(currentYearMonth)) {
//                // Create a new workbook and reset start date and end date for the new month
//                String filepath = "D:\\"  + "PCB_Value_" + currentYearMonth + ".xlsx";
//                Workbook newWorkbook = openOrCreateWorkbook(filepath);
//                addDataToExcel(newWorkbook, customerTagMap,headingLen);
//            }else {
            	 String filepath = "src/main/resources/test/"  + fileName;
                 Workbook  newWorkbook = openOrCreateWorkbook(filepath);
                 addDataToExcel(newWorkbook, customerTagMap,headingLen,fileName);
         //   }
           
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
//    private static boolean isNewMonth(String currentYearMonth) {
//        File file = new File(  "D:\\"  + "PCB_Value_" + currentYearMonth + ".xlsx");
//        // Check if the file for the current month already exists
//        if (!file.exists()) {
//        	 return true; // New month, create a new workbook
//        }
//        return false; 
//    }


	private static Workbook openOrCreateWorkbook(String filePath) throws IOException {
	       // File file = new File(filePath);
	        java.io.File file = new java.io.File(filePath);
	        if (file.exists()) {
	            try (FileInputStream fileInputStream = new FileInputStream(file)) {
	                // Try to open the existing workbook
	                return WorkbookFactory.create(fileInputStream);
	            } catch (Exception e) {
	                e.printStackTrace();
	                return null;
	            }
	        } else {
	            // If the file does not exist, create a new workbook
	            return new XSSFWorkbook(); 
	        }
	    }

    
    private static void addDataToExcel(Workbook newWorkbook, Map<String,Map<List<String>,List<String>>> customerTagMap, Integer headingLen, String fileName) throws IOException {
    	
    	 LocalDate start = LocalDate.now();  // Date start time now 
    	 // Get the current time in milliseconds
    	 long endTs = System.currentTimeMillis();
    	 // Calculate the time for 1 day before
    	 long oneDayInMillis = 24 * 60 * 60 * 1000; // 24 hours * 60 minutes * 60 seconds * 1000 milliseconds
    	 long StartTs = endTs - oneDayInMillis;
    	
    	 for (Map.Entry<String, Map<List<String>,List<String>>> entry : customerTagMap.entrySet()) {
             String customerName = entry.getKey();
//             System.out.println("customer name: "+customerName);
             Map<List<String>,List<String>> tagsEntry = entry.getValue();
             for (Map.Entry<List<String>,List<String>> tagValues : tagsEntry.entrySet()) {
	            	List<String> tags = tagValues.getKey();
	                List<String> headings = tagValues.getValue();
	                headingLen=headings.size();
	                String customerDetails = callApi(customerName);
				    String keyToFind = "id";
				    try {
				        ObjectMapper objectMapper = new ObjectMapper();
				        JsonNode jsonNode = objectMapper.readTree(customerDetails);
				    
				        String custId = printJsonNode(jsonNode, keyToFind);
				
				        if (!"na".equals(custId)) {
				            String devices = getCustomerDevices(custId);
				           // System.out.println(devices);
				        	List<String> deviceIds= printJsonNode2(objectMapper.readTree(devices), "id");
//				        	System.out.println("id:"+deviceIds);
				            List<String> deviceName = printJsonNode3(objectMapper.readTree(devices), "name");
				            
				            for (int i = 0; i < deviceIds.size(); i++) {
				                String deviceId = deviceIds.get(i);
				                String deviceValues =  getDeviceValuesByTime(deviceId, String.join(",", tags),StartTs,endTs);
		          			    JsonNode jsonNodeValues = objectMapper.readTree(deviceValues);
					            List <Object> tagValuesAPI = new ArrayList<Object>();
				                for (String key : tags) {
					                 JsonNode keyNode = jsonNodeValues.get(key);
					                 String value = keyNode.get(0).get("value").asText();
					                 if (value.equals("null")){
					                	 tagValuesAPI.add(" ");
					                  }
					                  else {
					                	// Check the type of the value
					                	  
					                	  try {
					                	        // Try to parse the value as a double
					                	        double doubleValue = Double.parseDouble(value);
					                	    //    System.out.println("Double Value: " + doubleValue);
					                	        tagValuesAPI.add(doubleValue);
					                	    } catch (NumberFormatException e2) {
					                	        // It's not an integer or double, treat it as a String
					                	   //     System.out.println("String Value: " + value);
					                	        tagValuesAPI.add(value);
					                	    }
					                	 //  value = value.replaceAll("\"", "");
					                	  // double val = Double.parseDouble(value);
					                	 //  tagValuesAPI.add(value);
					                   }
					                }
				                Sheet customerSheet = null;
				                String sanitizedCustomerName = customerName.replaceAll("[\\\\/:*?\"<>|]", "_");
				                
					            if (sanitizedCustomerName.length()>31) {
					            	sanitizedCustomerName=sanitizedCustomerName.toUpperCase().substring(0, 31);	
					            }
					           
				             
				                Sheet sheet = newWorkbook.getSheet(sanitizedCustomerName);
					            
					         //   Sheet sheet = newWorkbook.getSheet(customerName);
					            if (sheet==null) {
							    	int rowIndex = 0;
							    	try {
							    		customerSheet = newWorkbook.createSheet(sanitizedCustomerName);
							    		Row headerRow = customerSheet.createRow(rowIndex);   
							    		headerRow.createCell(0).setCellValue("Date");
							    		headerRow.createCell(1).setCellValue("Device");
							    		int idx = 2;
							    		for (String heading : headings) {
							    			Cell cell = headerRow.createCell(idx);
							    			cell.setCellValue(heading);
							    			idx++;
							    		}
							    		int[] columnsToAdjust = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28};
							    		int columnWidth = 4900;  // Adjust this value based on your needs
							    		// Set the font style for the first row
							    		CellStyle boldStyle = customerSheet.getWorkbook().createCellStyle();
							    		Font boldFont = customerSheet.getWorkbook().createFont();
							    		boldFont.setBold(true);
							    		boldStyle.setFont(boldFont);
							    		
							    		for (int columnIndex : columnsToAdjust) {
							    			customerSheet.setColumnWidth(columnIndex, columnWidth);
							    			Cell cell = customerSheet.getRow(customerSheet.getFirstRowNum()).getCell(columnIndex);
							    			if (cell == null) {
							    				cell = customerSheet.getRow(customerSheet.getFirstRowNum()).createCell(columnIndex);
							    			}
							    			// Set bold style for cells in the first row
							    			cell.setCellStyle(boldStyle);
							    			// Set horizontal alignment for the entire row
							    			cell.getCellStyle().setAlignment(HorizontalAlignment.CENTER);
							    			}
							    		
							    	}catch(Exception e){
							    		e.printStackTrace();
							    		continue;
							    	}	
					            }else {
					            	//customerSheet = newWorkbook.getSheet(sanitizedCustomerName);
					            	customerSheet = sheet;
					            }
					            	
					            int nxtRow = customerSheet.getLastRowNum()+1 ;
					            Row header1 = customerSheet.createRow(nxtRow++);
					            header1.createCell(0).setCellValue(start.toString());    
					            header1.createCell(1).setCellValue(deviceName.get(i).replaceAll("\"", ""));
				                    
					            addToExcel(2, header1, tagValuesAPI,headingLen);
					            nxtRow++;
					        }
		              }
				        else {
				        	System.out.println("Customer ID not available");
				        }
				    } catch (Exception e) {
				        e.printStackTrace();
				    }
	           }
    	 }
    	 // Save the changes back to the Excel file
    	
         FileOutputStream fileOutputStream = new FileOutputStream(  "src/main/resources/test/" + fileName);
         newWorkbook.write(fileOutputStream);
         fileOutputStream.close();
      //    sendEmail(newWorkbook);
         try {
			uploadDrive(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         newWorkbook.close();

         System.out.println("Sheet processed successfully.");
         
         
        }  


	private static void uploadDrive(String fileName)throws IOException, GeneralSecurityException {
		  // Load client secrets
		String jsonFile= CodelData.class.getClassLoader().getResource("test/eversense-4-f5528e9f4323.json").getFile();
		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonFile))
        		.createScoped(Collections.singletonList("https://www.googleapis.com/auth/drive.file"));

        // Build a new authorized API client for Drive
        Drive driveService = new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        )
                .setApplicationName("PCB package")
                .build();

        // Specify the file path of the file to be uploaded
        String filePath = "src/main/resources/test/"+fileName;
        String upLoadedfileName = fileName; // The name you want the file to have in Google Drive

        // Upload the file to Google Drive
        uploadFile(driveService, filePath, upLoadedfileName);
        
    }
	private static void uploadFile(Drive service, String filePath, String upLoadedfileName) throws IOException {
        // Read the file content
		String folderId="1dhYjwNGM28XeMa8FDbIPW9z6PiVpBy3Q";
		 String existingFileId = getFileIdByName(service, upLoadedfileName, folderId);

		    if (existingFileId != null) {
		        // File with the same name already exists, delete it
		        deleteFile(service, existingFileId);
		    }
        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));

        // Create the file metadata
        File fileMetadata = new File();
        fileMetadata.setName(upLoadedfileName);

        // Set the parent folder ID (replace with your desired folder ID or omit if you want to upload to the root)
        fileMetadata.setParents(Collections.singletonList(folderId));
        
//        10MK61Wz1bqgUwA2m3S1uwhcIgVqOOkET  /  1dhYjwNGM28XeMa8FDbIPW9z6PiVpBy3Q

        // Create the file content
        java.io.File fileContentIO = new java.io.File(filePath);
        FileContent mediaContent = new FileContent("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", fileContentIO);

        // Upload the file
        try {
            File uploadedFile = service.files().create(fileMetadata, mediaContent)
                    .setFields("id,webContentLink,webViewLink")
                    .execute();

            // Print the file ID
            System.out.println("File ID: " + uploadedFile.getId());
            // Print the link to the uploaded file
            System.out.println("File uploaded. Web Content Link: " + uploadedFile.getWebContentLink());
            System.out.println("File uploaded. Web View Link: " + uploadedFile.getWebViewLink());
         // Update file permissions to allow anyone with the link to view it
//            uploadedFile = service.files().update(uploadedFile.getId(), null).set("permissions", Collections.singletonList(
//                    new Permission()
//                            .setType("anyone")
//                            .setRole("reader")
//            )).execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	private static String getFileIdByName(Drive service, String fileName, String folderId) throws IOException {
	    FileList result = service.files().list()
	            .setQ("name='" + fileName + "' and '" + folderId + "' in parents")
	            .setSpaces("drive")
	            .execute();

	    List<File> files = result.getFiles();
	    return files.isEmpty() ? null : files.get(0).getId();
	}
	private static void deleteFile(Drive service, String fileId) throws IOException {
	    try {
	        service.files().delete(fileId).execute();
	        System.out.println("File deleted: " + fileId);
	    } catch (IOException e) {
	        e.printStackTrace();
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
              

            }
            
    }
        return nameList;
  }
  
	private static void addToExcel(int idx, Row headerRow, List<Object> headings, Integer headingLen) {
		Workbook workbook = headerRow.getSheet().getWorkbook();
		Sheet conditionalFormatingSheet= workbook.createSheet("conditional sheet");
		// condition for index column and up to condition 
		HashMap<Integer, Integer> hm= new HashMap<Integer, Integer>();
		hm.put(3, 10);//column index, condition
		hm.put(4, 10);
		hm.put(5, 6000);
		hm.put(6, 6000);
		hm.put(10, 3);
		hm.put(11, 5);
		hm.put(12, 2000);
		hm.put(13, 2000);
		hm.put(14, 7000);
		hm.put(15, 7000);
		for (Object heading : headings) {
			Cell cell = headerRow.createCell(idx);
			//cell.getCellStyle().setAlignment(HorizontalAlignment.CENTER);;
        	if( heading instanceof Double) {
        		double doubleValue= (Double) heading;
        		long intValue = Math.round(doubleValue);
        		if(Double.isInfinite(doubleValue)) {
        			String str="Infinity";
        			cell.setCellValue(str);
        		}else if(Double.isNaN(doubleValue)) {
        			String nanValue= "NaN";
        			cell.setCellValue(nanValue);
        		}
        		else {
        			cell.setCellValue(intValue);
        			CellStyle cellstyle = workbook.createCellStyle(); 
        			cellstyle.setAlignment(HorizontalAlignment.CENTER);
        			cell.setCellStyle(cellstyle);
        			try {
        				
        				if (idx>=3 && idx<=6) {
        	
        					if(intValue>hm.get(idx)) {
            			
        						setCellColor(cellstyle, IndexedColors.BRIGHT_GREEN.getIndex());
            					
            					cell.setCellStyle(cellstyle);
            				}else {
            			//		newCell.setCellValue("NOT OK");
            					setCellColor(cellstyle, IndexedColors.RED.getIndex());
            				//	newCell.setCellStyle(cellstyle);
            					cell.setCellStyle(cellstyle);
            				}
        				}
        				else if(idx>=10 && idx<=15) {
        				//	newCell = headerRow.createCell(headingLen+idx-14);
        					if(intValue>hm.get(idx)) {
            				//	newCell.setCellValue("OK");
        						setCellColor(cellstyle, IndexedColors.BRIGHT_GREEN.getIndex());
            					//newCell.setCellStyle(cellstyle);
            					cell.setCellStyle(cellstyle);
            				}else {
            					//newCell.setCellValue("NOT OK");
            					setCellColor(cellstyle, IndexedColors.RED.getIndex());
            					//newCell.setCellStyle(cellstyle);
            					cell.setCellStyle(cellstyle);
            				}
        				}
        			}catch (Exception e) {
						// TODO: handle exception
        				
					}
//        			
        		}
        	}
        	else if(heading instanceof String) {
        		String strValue= (String) heading;
        		cell.setCellValue(strValue);
        		CellStyle cellStyle = workbook.createCellStyle();
        		cellStyle.setAlignment(HorizontalAlignment.CENTER);
    			cell.setCellStyle(cellStyle);
        		try {
        		if (idx>=3 && idx<=6) {
    				}
    				else if(idx>=10 && idx<=15) {
    				}
    				//newCell.setCellValue("Disconnect");
        			//setCellColor(cellStyle, IndexedColors.YELLOW.getIndex());
        			//newCell.setCellStyle(cellStyle);
        			cell.setCellStyle(cellStyle);
        			
        		}catch (Exception e) {
					
				}

        	}
        	
        	idx++;
		}
		workbook.removeSheetAt(workbook.getSheetIndex(conditionalFormatingSheet));
	}

	private static void setCellColor(CellStyle cellstyle, short colorIndex) {
		cellstyle.setFillForegroundColor(colorIndex);
		cellstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	}

	private static String getJWtToken() {
		String username = "testfm@forbesmarshall.com";
		String password = "testfm@123";
		String url = "https://eversense.forbesmarshall.com";
		 
		RestClient client = new RestClient(url);
		client.login(username, password);
		String token = client.getToken();
		// Perform logout of current user and close client
				//client.logout();
				client.close();
		return token;
		
    }

   private static String callApi(String customerName) {
    	
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

   private static String getDeviceValuesByTime(String deviceId, String tags, long StartTs, long endTs) {
	   String response = "";
	   deviceId = deviceId.replaceAll("\"", "");
	   try {
		   String encodedDevice =  URLEncoder.encode(deviceId, StandardCharsets.UTF_8.toString());
		   String encodedTags =  URLEncoder.encode(tags, StandardCharsets.UTF_8.toString());
		
		   HttpClient client = HttpClient.newHttpClient();
		 //  String apiUrl1="https://eversense.forbesmarshall.com:443/api/plugins/telemetry3&startTs=1706005201000&endTs=1706091566961";
		   String apiUrl = "https://eversense.forbesmarshall.com:443/api/plugins/telemetry/" + "DEVICE" + "/" + encodedDevice + "/values/timeseries?" + "keys=" + encodedTags +"&"+"startTs"+ StartTs + "&" +"endTs"+endTs;
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



