package com.demo.demo;

import org.thingsboard.rest.client.RestClient;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.permission.AllowedPermissionsInfo;
import org.thingsboard.server.common.data.permission.Operation;
import org.thingsboard.server.common.data.permission.Resource;

public class TBAPITest000 {

	public static void main(String[] args) {
		String url = "https://eversense.forbesmarshall.com";

		// Perform login with default Customer User credentials
		String username = "testfm@forbesmarshall.com";
		String password = "testfm@123";
		RestClient client = new RestClient(url);
		client.login(username, password);

		// Get if user has generic read permission on device entities
		        
//		PageData<Device> tenantDevices;
//		PageLink pageLink = new PageLink(10);
//		do {
//		    // Fetch all tenant devices using current page link and print each of them
//		    tenantDevices = client.getUserDevices("", pageLink);
//		    tenantDevices.getData().forEach(System.out::println);
//		    pageLink = pageLink.nextPageLink();
//		} while (tenantDevices.hasNext());
		
		String token = client.getToken();
		System.out.println(token);
		
		// Perform logout of current user and close client
		client.logout();
		client.close();
	}
	
}
