package com.demo.demo;


import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.aspose.cells.WorksheetCollection;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExportExcelToGoogle {

	private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

	public static void main(String[] args) throws Exception {		
		// Export Excel data to Google Sheets
		exportExcelToGoogle("D:\\CRF_OneValues1.xlsx");
	}

	private static void exportExcelToGoogle(String _excelFileName) {
		try {
			// Build a new authorized API client service.
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			
			Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME).build();
			
			// Load an Excel workbook
			Workbook wb = new Workbook(_excelFileName);
			
			// Get name of the first worksheet
			String defaultWorksheetName = wb.getWorksheets().get(0).getName().trim();
			
			// Get all worksheets
			WorksheetCollection collection = wb.getWorksheets();
	
			// Create a new Google spreadsheet with default worksheet
			Spreadsheet spreadsheet = createSpreadsheet(service, wb.getFileName(), defaultWorksheetName);
	
			String range;
	
			// Loop through worksheets
			for (int worksheetIndex = 0; worksheetIndex < collection.getCount(); worksheetIndex++) {
				// Get reference of the worksheet
				Worksheet ws = collection.get(worksheetIndex);
				String sheetName = ws.getName().trim();
	
				if (worksheetIndex == 0) {
					// First sheet is created by default, so only set range
					range = "'" + sheetName + "'!A:Y";
				} else {
					// Add a new sheet
					addSheet(service, spreadsheet.getSpreadsheetId(), sheetName);
					 range = "'" + sheetName + "'!A:Y";
				}
	
				// Get number of rows and columns
				int rows = ws.getCells().getMaxDataRow();
				int cols = ws.getCells().getMaxDataColumn();
				List<List<Object>> worksheetData = new ArrayList<List<Object>>();
				
				// Loop through rows
				for (int i = 0; i <= rows; i++) {
					List<Object> rowData = new ArrayList<Object>();
	
					// Loop through each column in selected row
					for (int j = 0; j <= cols; j++) {
						if (ws.getCells().get(i, j).getValue() == null)
							rowData.add("");
						else
							rowData.add(ws.getCells().get(i, j).getValue());
					}
					

					// Add to worksheet data
					worksheetData.add(rowData);
				}
	
				// Set range
				ValueRange body = new ValueRange();
				body.setRange(range);
	
				// Set values
				body.setValues(worksheetData);
	
				// Export values to Google Sheets
				UpdateValuesResponse result = service.spreadsheets().values()
						.update(spreadsheet.getSpreadsheetId(), range, body).setValueInputOption("USER_ENTERED")
						.execute();
			//	System.out.println("Update Result: " + result);

				// Print output
				System.out.printf("%d cells updated.", result.getUpdatedCells());
	
			}
	
			// Open Google spreadsheet in browser
			Desktop desk = Desktop.getDesktop();
			desk.browse(new URI("https://docs.google.com/spreadsheets/d/" + spreadsheet.getSpreadsheetId()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Spreadsheet createSpreadsheet(Sheets _service, String spreadsheetName, String _defaultSheetName)
			throws IOException {
		// Create a new spreadsheet
		Spreadsheet spreadsheet = new Spreadsheet()
				.setProperties(new SpreadsheetProperties().setTitle(spreadsheetName));
	
		// Create a new sheet
		Sheet sheet = new Sheet();
		sheet.setProperties(new SheetProperties());
		sheet.getProperties().setTitle(_defaultSheetName);
	
		// Add sheet to list
		List<Sheet> sheetList = new ArrayList<Sheet>();
		sheetList.add(sheet);
		spreadsheet.setSheets(sheetList);
	
		// Execute request
		spreadsheet = _service.spreadsheets().create(spreadsheet).setFields("spreadsheetId").execute();
		System.out.println("Spreadsheet ID: " + spreadsheet.getSpreadsheetId());
	
		return spreadsheet;
	}

	private static void addSheet(Sheets _service, String _spreadSheetID, String _sheetName) {
		try {
			// Add new Sheet
			AddSheetRequest addSheetRequest = new AddSheetRequest();
			addSheetRequest.setProperties(new SheetProperties());
			addSheetRequest.getProperties().setTitle(_sheetName);
	
			// Create update request
			BatchUpdateSpreadsheetRequest batchUpdateSpreadsheetRequest = new BatchUpdateSpreadsheetRequest();
			Request req = new Request();
			req.setAddSheet(addSheetRequest);
			batchUpdateSpreadsheetRequest.setRequests(new ArrayList<Request>());
			batchUpdateSpreadsheetRequest.getRequests().add(req);
	
			// Execute request
			_service.spreadsheets().batchUpdate(_spreadSheetID, batchUpdateSpreadsheetRequest).execute();
		} catch (Exception e) {
			System.out.println("Error in creating sheet: " + e.getMessage());
		}
	}

	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets
		InputStream in = new FileInputStream(new File("D:\\jsonKey\\tokens.json"));
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
		// Build flow and trigger user authorization request
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}
}
