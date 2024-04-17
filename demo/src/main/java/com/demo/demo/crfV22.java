package com.demo.demo;

import org.apache.commons.collections4.functors.EqualPredicate;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.thingsboard.rest.client.RestClient;
import org.thingsboard.server.common.data.id.AssetId;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.DoubleValue;

import org.apache.poi.ss.usermodel.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.annotation.processing.SupportedSourceVersion;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;

public class crfV22 {
	public static String jwtToken = getJWtToken();
    public static void main(String[] args) throws EncryptedDocumentException, InvalidFormatException {
        String filePath = "D:\\CRF(v2.2).xlsx";
        
        try {
            // Read existing Excel file
            FileInputStream fileInputStream = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fileInputStream);
           
            // Get the first sheet
            Sheet originalSheet = workbook.getSheetAt(0);
      
            // Map to store customer names as keys and their tags as values
            Map<String,Map<List<String>,List<String>>> customerTagMap = new HashMap<>();
            Map<String ,Map<List<String>,List<String>>> customerTagMap1 = new HashMap<>();
            // Fetch customer names and tags from the sheet
            int headingLen=0;
           // String AssetId= null;
            for (Row row : originalSheet) {
                Cell nameCell = row.getCell(2); // Assuming customer names are in the third column (index 2)
                Cell IdCell =row.getCell(1);
                if ((nameCell != null && nameCell.getCellTypeEnum() == CellType.STRING)&&(IdCell!= null && IdCell.getCellTypeEnum() == CellType.STRING)) {
                    String customerName = nameCell.getStringCellValue();
                  //   AssetId = IdCell.getStringCellValue();
                    Cell cellInColumn5 = row.getCell(3);
                    // Dashboard not available tag is removing
                    if ("Dashboard not available".equalsIgnoreCase(cellInColumn5.getStringCellValue()) || "Dashboard not available ".equalsIgnoreCase(cellInColumn5.getStringCellValue()) ||
                            "Dashboard not availabe".equalsIgnoreCase(cellInColumn5.getStringCellValue())) {
                        continue;
                    }

                    // All Row tag ( Get all remaining cells as tags)
                    ArrayList<String> tags = new ArrayList<>();
                    ArrayList<String> tags1 = new ArrayList<>();
                    ArrayList<String> headings = new ArrayList<>();
                    ArrayList<String> headingAddOn = new ArrayList<>(Arrays. asList("Steam Flow", "Condensate total", "Status"));
                   
                  // Copy title row from the original sheet and paste as header row in the new sheet
                    Row sourceRow = originalSheet.getRow(0);
                
                    
                     for (int i = 3; i <=5; i++) {
                    	
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
                     
                     for(int j=6;j< row.getLastCellNum();j++) {
                    	 Cell tagCell = row.getCell(j);
                         if (tagCell != null) {
                             switch (tagCell.getCellTypeEnum()) {
                                 case STRING:
                                     if ("NA".equalsIgnoreCase(tagCell.getStringCellValue())) {
                                         // Handle NA case if needed
                                     } else {
                                         tags1.add(tagCell.getStringCellValue());
                                         Cell sourceCell = sourceRow.getCell(j);
                                         headings.add(sourceCell.getStringCellValue());
                                     }
                                     break;
                                 case NUMERIC:
                                     tags1.add(String.valueOf(tagCell.getNumericCellValue()));
                                     Cell sourceCell = sourceRow.getCell(j);
                                     headings.add(sourceCell.getStringCellValue());
                                     break;
                             }
                          }
                     }
                   
                    headings.addAll(headingAddOn);
                    headingLen = headings.size();
             
                    
                    // Update the map with customer name and tags
                    Map<List<String>, List<String>> tagsHeadings = new HashMap<>();
                    tagsHeadings.put(tags, headings);
                    customerTagMap.put(customerName, tagsHeadings);
                 //   System.out.println("tags: "+tags);
                 
                    Map<List<String>,List<String>> tagsHeadings1= new HashMap<>();
                    tagsHeadings1.put(tags1, headings);
                    customerTagMap1.put(customerName, tagsHeadings1);
                   // System.out.println("tags1:"+tags1);
               
                }
            } 
            String filepath = "D:\\CRF(2.2)Values1.xlsx";
            Workbook newWorkbook = openOrCreateWorkbook(filepath);
          
            addToExcelInAssetDevice(newWorkbook,customerTagMap,customerTagMap1, headingLen);
//            addDataToExcelAsset(newWorkbook, customerTagMap, headingLen);
//            addDataToExcelDevice(newWorkbook,customerTagMap1,headingLen);

        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    private static void addToExcelInAssetDevice(Workbook newWorkbook, Map<String, Map<List<String>, List<String>>> customerTagMap, Map<String, Map<List<String>, List<String>>> customerTagMap1, int headingLen) throws IOException {
    	 LocalDate start = LocalDate.now();  // Date start time now 
    	 for (Map.Entry<String, Map<List<String>, List<String>>> entry : customerTagMap.entrySet()) {
            String customerName = entry.getKey();
            Map<List<String>, List<String>> tagsHeadings = entry.getValue();

            List<String> assetKeys = addTags(tagsHeadings);
            List<String> tags = assetKeys.subList(0, tagsHeadings.keySet().iterator().next().size());  // Extract tags
            List<String> headings = assetKeys.subList(tagsHeadings.keySet().iterator().next().size(), assetKeys.size());  // Extract headings

           // System.out.println("tags for : " + tags);
         // System.out.println("heading "+headings);
            List<String> tags1 = null;
            List<String> headings1=null;
            if (customerTagMap1.containsKey(customerName)) {
                Map<List<String>, List<String>> tagsHeadings1 = customerTagMap1.get(customerName);
                List<String> DeviceKeys1 = addTags(tagsHeadings1);
                tags1 = DeviceKeys1.subList(0, tagsHeadings1.keySet().iterator().next().size());  // Extract tags
                headings1 = DeviceKeys1.subList(tagsHeadings1.keySet().iterator().next().size(), DeviceKeys1.size());  // Extract headings
           }
            headingLen = headings.size();
			//System.out.println("tags1 for " +  ": " + tags1);
			//System.out.println("qqqq:"+headings1);
            String customerDetails=callApi(customerName);
            String keyToFind="id";
            try {
		        ObjectMapper objectMapper = new ObjectMapper();
		        JsonNode jsonNode = objectMapper.readTree(customerDetails);
		    
		        String custId = getCustomerIds(jsonNode, keyToFind);
		
		        if (!"na".equals(custId)) {
		            String assets = getCustomerAssets(custId);
		            String devices= getCustomerDevices(custId);
		          //  System.out.println("qqqq: "+devices);
		             JsonNode jsonNode2 = objectMapper.readTree(assets);
		             List<String> assetIds = filterIdsForCrfAssets(jsonNode2, "crf");
		              
		            // Specify the key to find in device names
		             String keyToFindInDevices = "crf";

		            // Filter and print the names containing the specified key
		            List<String> assetName = filterNamesWithCrf(jsonNode2, keyToFindInDevices);
		            
		            List<String> deviceIds= FindDeviceIds(objectMapper.readTree(devices), "id"); 
		            // System.out.println("device: "+deviceIds);
		           

		           //  System.out.println("tag: "+tags1);
		             
              // System.out.println(deviceValues+"^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		            // System.out.println("max value : "+maxTagValuesAPI1);
		            
		            for (int i = 0; i < assetIds.size(); i++) {
		                 String assetId = assetIds.get(i);
//		                 String deviceId= deviceIds.get(i);
		                 String AssetValues = getAssetValues(assetId, String.join(",", tags));
//		                 String deviceValues = getDeviceValues(deviceId, String.join(",", tags1));
//		                 JsonNode jsonDeviceValues = objectMapper.readTree(deviceValues);
		                 JsonNode jsonAssetValues = objectMapper.readTree(AssetValues);
		                // List <Double> tagValuesAPI = new ArrayList<Double>();
		                 List<Integer> tagValueList= new ArrayList<Integer>();
			             for (String key : tags) {
		                   JsonNode keyNode = jsonAssetValues.get(key);
		                   String value = keyNode.get(0).get("value").toString();
		                   if (value.equals("null")){
		                	  // tagValuesAPI.add(0.0);
		                	   tagValueList.add(0);
		                   }else {
		                	   value = value.replaceAll("\"", "");
		                	   Integer val = (int) Double.parseDouble(value);
		                	 //  tagValuesAPI.add(val);
		                	   tagValueList.add(val);
		                   }
			            }
			             //**************************device****888
			             System.out.println(tagValueList+"qqqq");

			             for (String tagToCompare : tags1) {
			            	 String maxTs = null;
			                 Integer maxValue = 0;
			                 
			                 for (String deviceId : deviceIds) {
			                     String deviceValues = getDeviceValues(deviceId, String.join(",", tags1));
			                   //  System.out.println("device Values: " + deviceValues);

			                     JsonNode jsonDeviceValues = objectMapper.readTree(deviceValues);
			                     String currentMaxTs = null;
			                     Integer currentMaxValue = 0;

			                     for (String key : tags1) {
			                    	    if (key.equals(tagToCompare)) {
			                    	        JsonNode keyNode = jsonDeviceValues.get(key);

			                    	        if (keyNode.isArray() && keyNode.size() > 0) {
			                    	            String ts = keyNode.get(0).get("ts").asText();
			                    	            String value = keyNode.get(0).get("value").asText();
			                    	            if (!"null".equals(value)) {
			                    	                Double doubleValue = Double.parseDouble(value);
			                    	              // Check if this is the first iteration or if ts is greater than maxTs
			                    	                if (currentMaxTs == null || ts.compareTo(currentMaxTs) > 0) {
			                    	                    currentMaxTs = ts;
			                    	                    currentMaxValue = doubleValue.intValue();
			                    	                }
			                    	                else if (ts.equals(currentMaxTs) && (currentMaxValue == null || doubleValue >currentMaxValue)) {
			                                            // Handle case where multiple devices have the same maxTs
			                                            currentMaxValue = doubleValue.intValue();
			                                        }
			                    	            } else { //  System.out.println("Value for " + key + " is null, ignoring...");
			                    	            }
			                    	        }
			                    	    }
			                    	} //  if this is the first iteration or if currentMaxTs is greater than maxTs
			                     if (maxTs == null || (currentMaxTs != null && currentMaxTs.compareTo(maxTs) > 0)) {
			                         maxTs = currentMaxTs;
			                         maxValue = currentMaxValue;
			                       //  System.out.println("maxcheck1: "+maxValue);
			                        
			                     } else if (currentMaxTs != null && currentMaxTs.equals(maxTs) && (maxValue == null ||currentMaxValue > maxValue)) {
			                         // Handle case where multiple devices have the same maxTs
			                         maxValue = currentMaxValue;
			                       //  tagValueList.add(maxValue);
			                       //  System.out.println("maxcheck2: "+maxValue);
			                     }
			                 }
			             
//			                 if(maxValue !=null) {
			                 tagValueList.add(maxValue);
//			                 }else {
//			                	// System.out.println("skipping null: "+maxValue);
//			                	 tagValueList.add("N/A");
//			                 }
//			                	 
			                 System.out.println("Maximum ts for " + tagToCompare + " across all devices: " + maxTs);
			                 System.out.println("Maximum value for " + tagToCompare + " across all devices: " + maxValue);
			                
			             } 
			             System.out.println("max list value: "+tagValueList);
			            
			             
			             
			             
			            Sheet customerSheet = null;
			            
			            Sheet sheet = newWorkbook.getSheet(customerName);
			            if (sheet==null) {
					    	int rowIndex = 0;
					    	try {
					    		customerSheet = newWorkbook.createSheet(customerName);
					    	    Row headerRow = customerSheet.createRow(rowIndex);   
			                    headerRow.createCell(0).setCellValue("Date");
			                    headerRow.createCell(1).setCellValue("Asset");
					            int idx = 2;
					            
			                    for (String heading : headings) {
			                    	
			                    	Cell cell = headerRow.createCell(idx);
			                    	cell.setCellValue(heading);
			                    	idx++;
			                    }
			                    int[] columnsToAdjust = {0,1,2,3,4,5,6,7,8,9};
			                    int columnWidth = 4000;  // Adjust this value based on your needs

			                    for (int columnIndex : columnsToAdjust) {
			                        customerSheet.setColumnWidth(columnIndex, columnWidth);
			                    }
					    	}catch(Exception e){
					    		continue;
					    	}	
			            }else {
			            	customerSheet = newWorkbook.getSheet(customerName);
			            }
		            	int nxtRow = customerSheet.getLastRowNum()+1 ;
			            Row header1 = customerSheet.createRow(nxtRow);
			            header1.createCell(0).setCellValue(start.toString()); 
	                    header1.createCell(1).setCellValue(assetName.get(i).replaceAll("\"", ""));
	                  
	                    addToExcel(2, header1, tagValueList, headingLen);
	                    //addToDevice(5, header1, tagValuesAPI);
	                    nxtRow++;
		              }
		             }
		          //  }
		            else {
		            System.out.println("Customer ID not available");
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
       
    	 }
    	   // Save the changes back to the Excel file
         FileOutputStream fileOutputStream = new FileOutputStream("D:\\CRF(2.2)Values1.xlsx");
         newWorkbook.write(fileOutputStream);
         fileOutputStream.close();
        // sendEmail(newWorkbook);
         newWorkbook.close();

         System.out.println("Sheet processed successfully.");

     
    }

    private static List<String> addTags(Map<List<String>, List<String>> tagsHeadings) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<List<String>, List<String>> tagValues : tagsHeadings.entrySet()) {
            List<String> tags = tagValues.getKey();
            List<String> headings = tagValues.getValue();
            result.addAll(tags);
            result.addAll(headings);
        }
        return result;
    }


	private static Workbook openOrCreateWorkbook(String filePath) throws IOException {
	        File file = new File(filePath);
	
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

	
    
    private static void addDataToExcelAsset(Workbook newWorkbook, Map<String,Map<List<String>,List<String>>> customerTagMap, Integer headingLen) throws IOException {
    LocalDate start = LocalDate.now();  // Date start time now 
    
    	 for (Map.Entry<String, Map<List<String>,List<String>>> entry : customerTagMap.entrySet()) {
    		 
             String customerName = entry.getKey();
          //  System.out.println("name: "+customerName);
            
             Map<List<String>,List<String>> tagsEntry = entry.getValue();
             for (Map.Entry<List<String>,List<String>> tagValues : tagsEntry.entrySet()) {
	            	List<String> tags = tagValues.getKey();
	                List<String> headings = tagValues.getValue();
				    headingLen = headings.size();  //set heading length again
				    String customerDetails = callApi(customerName);
				    String keyToFind = "id";
				    try {
				        ObjectMapper objectMapper = new ObjectMapper();
				        JsonNode jsonNode = objectMapper.readTree(customerDetails);
				    
				        String custId = getCustomerIds(jsonNode, keyToFind);
				
				        if (!"na".equals(custId)) {
				            String assets = getCustomerAssets(custId);
				         //   String devices= getCustomerDevices1(custId);
				             JsonNode jsonNode2 = objectMapper.readTree(assets);
				             List<String> assetIds = filterIdsForCrfAssets(jsonNode2, "crf");
				               
				            // Specify the key to find in device names
				             String keyToFindInDevices = "crf";

				            // Filter and print the names containing the specified key
				            List<String> assetName = filterNamesWithCrf(jsonNode2, keyToFindInDevices);
				          
				            for (int i = 0; i < assetIds.size(); i++) {
				                 String assetId = assetIds.get(i);
				                 
				                 String deviceValues = getAssetValues(assetId, String.join(",", tags));
				
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
					        
					            Sheet customerSheet = null;
					            
					            Sheet sheet = newWorkbook.getSheet(customerName);
					            if (sheet==null) {
							    	int rowIndex = 0;
							    	try {
							    		customerSheet = newWorkbook.createSheet(customerName);
							    		Row headerRow = customerSheet.createRow(rowIndex);   
					                    headerRow.createCell(0).setCellValue("Date");
					                    headerRow.createCell(1).setCellValue("Asset");
							            int idx = 2;
							            
					                    for (String heading : headings) {
					                    	
					                    	Cell cell = headerRow.createCell(idx);
					                    	cell.setCellValue(heading);
					                    	idx++;
					                    }
							    	}catch(Exception e){
							    		continue;
							    	}	
					            }else {
					            	customerSheet = newWorkbook.getSheet(customerName);
					            }
				            	int nxtRow = customerSheet.getLastRowNum()+1 ;
					            Row header1 = customerSheet.createRow(nxtRow);
					            header1.createCell(0).setCellValue(start.toString()); 
			                    header1.createCell(1).setCellValue(assetName.get(i).replaceAll("\"", ""));
			                  
			                  //  addToExcel(2, header1, tagValuesAPI, headingLen);
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
         FileOutputStream fileOutputStream = new FileOutputStream("D:\\CRF(2.2)Values1.xlsx");
          newWorkbook.write(fileOutputStream);
         fileOutputStream.close();
//        // sendEmail(newWorkbook);
//         newWorkbook.close();

         System.out.println("1 Sheet processed successfully.");
    }
    private static void addDataToExcelDevice(Workbook newWorkbook,Map<String, Map<List<String>, List<String>>> customerTagMap1, int headingLen) throws IOException {
    	
    	for (Map.Entry<String, Map<List<String>,List<String>>> entry : customerTagMap1.entrySet()) {
   		 
           String customerName = entry.getKey();
           System.out.println("name: "+customerName);
           
            Map<List<String>,List<String>> tagsEntry = entry.getValue();
            for (Map.Entry<List<String>,List<String>> tagValues : tagsEntry.entrySet()) {
	            	List<String> tags = tagValues.getKey();
	                List<String> headings = tagValues.getValue();
	                System.out.println("tags1: "+tags);
	                
	                headingLen = headings.size();  //set heading length again
				    String customerDetails = callApi(customerName);
				    String keyToFind = "id";
				    try {
				        ObjectMapper objectMapper = new ObjectMapper();
				        JsonNode jsonNode = objectMapper.readTree(customerDetails);
				        String custId = getCustomerIds(jsonNode, keyToFind);
				
				        if (!"na".equals(custId)) {
				          
				            String devices= getCustomerDevices(custId);
				            List<String> deviceIds= FindDeviceIds(objectMapper.readTree(devices), "id");
				        
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
					        
					            Sheet customerSheet = null;
					            
					            Sheet sheet = newWorkbook.getSheet(customerName);
					            if (sheet==null) {
							    	int rowIndex = 0;
							    	try {
							    		customerSheet = newWorkbook.createSheet(customerName);
							    		Row headerRow = customerSheet.createRow(rowIndex);   
					                  //  headerRow.createCell(0).setCellValue("Date");
					                   // headerRow.createCell(1).setCellValue("Asset");
							            int idx = 2;
							            
					                    for (String heading : headings) {
					                    	
					                    	Cell cell = headerRow.createCell(idx);
					                    	cell.setCellValue(heading);
					                    	idx++;
					                    }
							    	}catch(Exception e){
							    		continue;
							    	}	
					            }else {
					            	customerSheet = newWorkbook.getSheet(customerName);
					            }
				            	int nxtRow = customerSheet.getLastRowNum()+1 ;
					            Row header1 = customerSheet.createRow(nxtRow);
					           // header1.createCell(0).setCellValue(start.toString()); 
			                  //  header1.createCell(1).setCellValue(deviceName.get(i).replaceAll("\"", ""));
			                  
			                  //  addToDevice(5, header1, tagValuesAPI);
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
         FileOutputStream fileOutputStream = new FileOutputStream("D:\\CRF(2.2)Values1.xlsx");
         newWorkbook.write(fileOutputStream);
         fileOutputStream.close();
        // sendEmail(newWorkbook);
         newWorkbook.close();

         System.out.println("Sheet processed successfully.");
	}

    
    

    private static ArrayList<String> FindDeviceIds(JsonNode jsonNode, String keyToFindInDevices) {
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
    private static List<String> filterIdsForCrfAssets(JsonNode jsonNode, String keyToFindInDevices) {
        List<String> idList = new ArrayList<>();

        JsonNode dataArray = jsonNode.get("data");
        if (dataArray != null && dataArray.isArray() && dataArray.size() > 0) {
            for (JsonNode deviceNode : dataArray) {
                JsonNode idNode = deviceNode.get("id");
                JsonNode nameNode = deviceNode.get("name");

                if (nameNode != null && nameNode.isTextual() && idNode != null) {
                    String deviceName = nameNode.asText();
                    if (deviceName.toLowerCase().contains(keyToFindInDevices.toLowerCase())) {
                      //  System.out.println("Adding id for device: " + deviceName);
                        idList.add(idNode.get("id").asText());
                       // System.out.println(" id: " + idList);
                    }
                }
            }
        }

        return idList;
    }


    private static List<String> filterNamesWithCrf(JsonNode jsonNode, String keyToFindInDevices) {
        // Extract and filter names for the specified key
        JsonNode dataArray = jsonNode.get("data");
        List<String> nameList = new ArrayList<>();

        if (dataArray != null && dataArray.isArray() && dataArray.size() > 0) {
            for (JsonNode deviceNode : dataArray) {
                JsonNode nameNode = deviceNode.get("name");
                if (nameNode != null && nameNode.isTextual()) {
                    String name = nameNode.asText();
                    if (name.toLowerCase().contains(keyToFindInDevices.toLowerCase())) {
                        nameList.add(name);
                    }
                }
            }
        }
        return nameList;
    }

    private static void addToDevice(int idx, Row headerRow, List<Double> headings) {
		
        for (double heading : headings) {
        	
        	Cell cell = headerRow.createCell(idx);
        	//cell.setCellValue(heading);
        	cell.setCellValue((int) heading);
        	System.out.println(headings+"tags value");
        	idx++;
        }
	}

    private static void addToExcel(int idx, Row headerRow, List<Integer> tagValueList, Integer headingLen) {
    	 Workbook workbook = headerRow.getSheet().getWorkbook();
    	    Sheet conditionalFormattingSheet = workbook.createSheet("ConditionalFormattingSheet");

	    for (int heading : tagValueList) {
	    	Cell cell = headerRow.createCell(idx);
	    	
        	cell.setCellValue(heading);
        	 // Create a new cell style
            CellStyle cellStyle = workbook.createCellStyle();
        	if(idx==2) {
        		Cell newCell = headerRow.createCell(headingLen+idx-3);
        		if(heading>10) {
        		newCell.setCellValue("OK");
        		
        	    }else {
        		      newCell.setCellValue("Not ok");
        	          }
        	}
        	if(idx==3) {
        		Cell newCell = headerRow.createCell(headingLen+idx-3);
        		if(heading>10) {
        		newCell.setCellValue("OK");
        	    }else {
        		      newCell.setCellValue("Not ok");
        	          }
        	}
        	if(idx==4) {
        		Cell newCell = headerRow.createCell(headingLen+idx-3);
        		if(heading>5 && heading < 100) {
        		newCell.setCellValue("OK");
        		setCellColor(cellStyle, IndexedColors.GREEN.getIndex());
       		
                newCell.setCellStyle(cellStyle);

        	    }else {
        		      newCell.setCellValue("Not ok");
        		      setCellColor(cellStyle, IndexedColors.RED.getIndex());
             		 // Set the cell style to the created style
                     newCell.setCellStyle(cellStyle);

        	          }
        	}
        	             idx++;
        }
	    // Remove the conditionalFormattingSheet as it's not needed
	    workbook.removeSheetAt(workbook.getSheetIndex(conditionalFormattingSheet));
	}
    private static void setCellColor(CellStyle cellStyle, short colorIndex) {
        // Set the fill foreground color
        cellStyle.setFillForegroundColor(colorIndex);
        // Set the fill pattern
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }
	private static String getJWtToken() {
		String username = "testfm@forbesmarshall.com";
		String password = "testfm@123";
		String url = "https://eversense.forbesmarshall.com";
		 
		RestClient client = new RestClient(url);
		client.login(username, password);
		String token = client.getToken();
		// Perform logout of current user and close client
//				client.logout();
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
   private static String getCustomerIds(JsonNode jsonNode, String keyToFind) {
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

  
  
   private static String getCustomerAssets(String custId) {
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

   private static String getAssetValues(String deviceId, String tags) {
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
	   String response = "";
	   deviceId = deviceId.replaceAll("\"", "");
	   try {
		   String encodedDevice =  URLEncoder.encode(deviceId, StandardCharsets.UTF_8.toString());
		   String encodedTags =  URLEncoder.encode(tags, StandardCharsets.UTF_8.toString());
		
		   HttpClient client = HttpClient.newHttpClient();
		   String apiUrl = "https://eversense.forbesmarshall.com:443/api/plugins/telemetry/" + "DEVICE" + "/" + encodedDevice + "/values/timeseries?" + "keys=" + encodedTags;
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



