package com.demo.demo;


import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.thingsboard.rest.client.RestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import javax.mail.*;  
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import javax.activation.*;  

public class crfSitesAll {
	public static String jwtToken = getJWtToken();
    public static void main(String[] args) throws EncryptedDocumentException, InvalidFormatException {
        String filePath = "D:\\CRF.xlsx";
        
        
        
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
                Cell nameCell = row.getCell(1); // Assuming customer names are in the third column (index 2)
                if (nameCell != null && nameCell.getCellTypeEnum() == CellType.STRING) {
                    String customerName = nameCell.getStringCellValue();
                    Cell cellInColumn5 = row.getCell(2);
                    // Dashboard not available tag is removing
                    if ("Dashboard not available".equalsIgnoreCase(cellInColumn5.getStringCellValue()) || "Dashboard not available ".equalsIgnoreCase(cellInColumn5.getStringCellValue()) ||
                            "Dashboard not availabe".equalsIgnoreCase(cellInColumn5.getStringCellValue())) {
                        continue;
                    }

                    // All Row tag ( Get all remaining cells as tags)
                    ArrayList<String> tags = new ArrayList<>();
                    ArrayList<String> headings = new ArrayList<>();
                    ArrayList<String> headingAddOn = new ArrayList<>(Arrays. asList("Steam Flow", "Condensate total", "Status"));
                   
                  // Copy title row from the original sheet and paste as header row in the new sheet
                    Row sourceRow = originalSheet.getRow(0);
                

                    for (int i = 2; i < row.getLastCellNum(); i++) {
                        Cell tagCell = row.getCell(i);
                        if (tagCell != null) {
                            switch (tagCell.getCellTypeEnum()) {
                                case STRING:
                                    if ("NA".equalsIgnoreCase(tagCell.getStringCellValue())) {
                                        // Handle NA case if needed
                                    } else {
                                        tags.add(tagCell.getStringCellValue());
                                        Cell sourceCell = sourceRow.getCell(i);
                                        if (sourceCell != null) {
                                            headings.add(sourceCell.getStringCellValue());
                                        } else {
                                           
                                        }
                                    }
                                    break;
                                case NUMERIC:
                                    tags.add(String.valueOf(tagCell.getNumericCellValue()));
                                    Cell sourceCell = sourceRow.getCell(i);
                                    if (sourceCell != null) {
                                        headings.add(sourceCell.getStringCellValue());
                                    } else {
                                        
                                    }
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
                }
            } 
            String filepath = "D:\\CRF_AllValues1.xlsx";
            Workbook newWorkbook = openOrCreateWorkbook(filepath);
          
            
            addDataToExcel(newWorkbook, customerTagMap, headingLen);

          //  System.out.println(customerTagMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    
    private static void addDataToExcel(Workbook newWorkbook, Map<String,Map<List<String>,List<String>>> customerTagMap, Integer headingLen) throws IOException {
    	
    	 LocalDate start = LocalDate.now();  // Date start time now 
    	 for (Map.Entry<String, Map<List<String>,List<String>>> entry : customerTagMap.entrySet()) {
             String customerName = entry.getKey();
            // System.out.println("customer name1: "+customerName);
             Map<List<String>,List<String>> tagsEntry = entry.getValue();
             for (Map.Entry<List<String>,List<String>> tagValues : tagsEntry.entrySet()) {
	            	List<String> tags = tagValues.getKey();
	                List<String> headings = tagValues.getValue();
				   
				   headingLen = headings.size();  //set heading length again
				 //  System.out.println("heading:"+headingLen);
				//   System.out.println("customer name2: "+customerName);
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
				        	//System.out.println("id:"+deviceIds);
				            List<String> deviceName = printJsonNode3(objectMapper.readTree(devices), "name");
				        
				            for (int i = 0; i < deviceIds.size(); i++) {
				                String deviceId = deviceIds.get(i);
//				                String status= "liveStatus";
//				                Boolean deviceStatus = getDeviceStatus(deviceId,status);
				          //   System.out.println(deviceStatus);
				              
//				                if(!deviceStatus) {
//				                	continue;
//				                	}
//				                       
		                          // Proceed with the live device
		                          String deviceValues = getDeviceValues(deviceId, String.join(",", tags));
		          		//		System.out.println("device value: "+deviceValues);
					                JsonNode jsonNodeValues = objectMapper.readTree(deviceValues);
					                List <Object> tagValuesAPI = new ArrayList<Object>();
				                    for (String key : tags) {
					                   JsonNode keyNode = jsonNodeValues.get(key);
					                   String value = keyNode.get(0).get("value").toString();
					                   if (value.equals("null")){
					                	   tagValuesAPI.add("NA");
					                   }
					                   else {
					                	   value = value.replaceAll("\"", "");
					                	   double val = Double.parseDouble(value);
					                	   tagValuesAPI.add(val);
					                   }
					                   
						            }
						            
						            
						            
						//       System.out.println("value: "+tagValuesAPI);
						            Sheet customerSheet = null;
						            if (customerName.length()>31) {
						            	 customerName=customerName.toUpperCase().substring(0, 31);	
						             }
						            Sheet sheet = newWorkbook.getSheet(customerName);
						            if (sheet==null) {
								    	int rowIndex = 0;
								    	//System.out.println("tag111: "+tagValuesAPI);
								    	try {
								    		customerSheet = newWorkbook.createSheet(customerName);
								    		Row headerRow = customerSheet.createRow(rowIndex);   
						                    headerRow.createCell(0).setCellValue("Date");
						                    headerRow.createCell(1).setCellValue("Device");
						                   // headerRow.createCell(5).setCellValue("");
								            int idx = 2;
								            
						                    for (String heading : headings) {
						                    	
						                    	Cell cell = headerRow.createCell(idx);
						                    	cell.setCellValue(heading);
						                    	idx++;
						                    }
						                    int[] columnsToAdjust = {0,1,2,3,4,5,6,7,8,9};
						                    int columnWidth = 5000;  // Adjust this value based on your needs

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
						            Row header1 = customerSheet.createRow(nxtRow++);
						            header1.createCell(0).setCellValue(start.toString());    
				                    header1.createCell(1).setCellValue(deviceName.get(i).replaceAll("\"", ""));
				                  
				                    addToExcel(2, header1, tagValuesAPI, headingLen);
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
         FileOutputStream fileOutputStream = new FileOutputStream("D:\\CRF_AllValues1.xlsx");
         newWorkbook.write(fileOutputStream);
         fileOutputStream.close();
        // sendEmail(newWorkbook);
         newWorkbook.close();

         System.out.println("Sheet processed successfully.");
         
         
        }  
         
    
   
    private static void sendEmail(Workbook newWorkbook) throws IOException  {

    	  // Sender's email address
        String from = "goresumit33@gmail.com";

        // Recipient's email address
      //  String to = "goresumit41@gmail.com";
        String[] toAddresses = {"goresumit41@gmail.com", "goresumit3376@gmail.com"};

        // Sender's email password (for authentication)
        String password = "vovtyksmuccwhmbi";

        // Set up mail server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");


        // Get the Session object
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
        	for(String to:toAddresses) {
            // Create a default MimeMessage object
            MimeMessage message = new MimeMessage(session);

            // Set the sender and recipient addresses
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set the email subject
            message.setSubject("CRF ");

            // Create a MimeBodyPart object to represent the email body
            BodyPart messageBodyPart = new MimeBodyPart();

            // Set the text content of the email
            messageBodyPart.setText("This is a test email with an attachment.");

            // Create a MimeBodyPart object to represent the attachment
            MimeBodyPart attachmentPart = new MimeBodyPart();

            // Convert Workbook to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            newWorkbook.write(bos);

            // Create an InputStream from the byte array
            InputStream is = new ByteArrayInputStream(bos.toByteArray());

            // Set the data source for the attachment
            DataSource source = new ByteArrayDataSource(is, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName("CRF_Values1.xlsx");
         
            // Create a Multipart object to add the body and attachment parts
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);

            // Set the content of the message to the Multipart object
            message.setContent(multipart);

            // Send the message
            Transport.send(message);

            System.out.println("Email sent successfully.");

        	}
        } catch (MessagingException e) {
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
		//System.out.println(headings+"tags value");
		Workbook workbook = headerRow.getSheet().getWorkbook();
		Sheet conditionalFormatingSheet= workbook.createSheet("conditional sheet");
        for (Object heading : headings) {
        	
        	Cell cell = headerRow.createCell(idx);
        	if( heading instanceof Double) {
        		double doubleValue= (Double) heading;
        		int intValue=(int) doubleValue;
        		if(Double.isInfinite(doubleValue)) {
        			String str="Infinity";
        			cell.setCellValue(str);
        		}else if(Double.isNaN(doubleValue)) {
        			String nanValue= "NaN";
        			cell.setCellValue(nanValue);
        		}
        		else {
        			cell.setCellValue(intValue);
        		//	System.out.println(heading + " = " + doubleValue + " converted to " + intValue);
        			CellStyle cellstyle = workbook.createCellStyle();        	
        			if (idx == 2) {
        				//System.out.println("eeeeeeee"+headingLen);
        				Cell newCell = headerRow.createCell(headingLen+idx-3);
        				if(intValue>10) {
        					newCell.setCellValue("OK");
        				}else {
        					newCell.setCellValue("NOT OK");
        				}
        			}
        			if (idx==3) {
        				Cell newCell = headerRow.createCell(headingLen+idx-3);
        				if(intValue>10) {
        					newCell.setCellValue("OK");
        				}else {
        					newCell.setCellValue("NOT OK");
        				}
        			}
        			if(idx==4)
        			{
        				Cell newCell = headerRow.createCell(headingLen+idx-3);
        				if(intValue >5 && intValue <100)
        				{
        					newCell.setCellValue("OK");
        					setCellColor(cellstyle, IndexedColors.GREEN.getIndex());
        					newCell.setCellStyle(cellstyle);
        				}else {
        					newCell.setCellValue("Not ok");
        					setCellColor(cellstyle, IndexedColors.RED.getIndex());
        					newCell.setCellStyle(cellstyle);
        				}
        			}
        		}
        	}
        	else if(heading instanceof String) {
        		String strValue= (String) heading;
        		cell.setCellValue(strValue);
        		CellStyle cellStyle = workbook.createCellStyle();
        		if(idx==2) {
        			Cell newCell=headerRow.createCell(headingLen+idx-3);
        			newCell.setCellValue("Disconnect");
        		}
        		if(idx==3) {
        			Cell newCell= headerRow.createCell(headingLen+idx-3);
        			newCell.setCellValue("Disconnect");
        		}
        		if(idx==4) {
        			Cell newCell= headerRow.createCell(headingLen+idx-3);
        			newCell.setCellValue("Disconnect");
        			setCellColor(cellStyle, IndexedColors.YELLOW.getIndex());
        			newCell.setCellStyle(cellStyle);
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
				client.logout();
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
   
   private static Boolean getDeviceStatus(String deviceId, String findkey) {
	   String response = "";
	   deviceId = deviceId.replaceAll("\"", "");
	   try {
	        ObjectMapper objectMapper = new ObjectMapper();

			   String encodedDevice =  URLEncoder.encode(deviceId, StandardCharsets.UTF_8.toString());
			  // String encodedTags =  URLEncoder.encode(tags, StandardCharsets.UTF_8.toString());
			
			   HttpClient client = HttpClient.newHttpClient();
			  // String apiUrl = "https://eversense.forbesmarshall.com:443/api/plugins/telemetry/" + "DEVICE" + "/" + encodedDevice + "/values/timeseries?" + "keys=" + encodedTags;
			   String apiUrl = "https://eversense.forbesmarshall.com:443/api/plugins/telemetry/" + "DEVICE" + "/" + encodedDevice + "/values/attributes?" + "keys=" + findkey ;
			   HttpRequest request = HttpRequest.newBuilder()
			           .uri(URI.create(apiUrl))
			           .header("accept", "application/json")
			           .header("X-Authorization", "Bearer " + jwtToken)
			           .GET()
			           .build();
			
			   HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
			   
			   response = httpResponse.body();
			   JsonNode statusArray = objectMapper.readTree(response);
			   Boolean status = statusArray.get(0).get("value").asBoolean();
			   return status;
			   }
	   catch (Exception e) {
		return false;
	   }
   }
   

}



