package com.demo.demo;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class test11 {
    public static void main(String[] args) throws Exception {
        // Set the URL for the GET request
        URL url = new URL("https://eversense.forbesmarshall.com:443/api/plugins/telemetry/DEVICE/5e6965c0-4390-11ec-979c-b32a87074242/values/attributes");

        // Open a connection to the URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set the request method to GET
        connection.setRequestMethod("GET");

        // Set request headers
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestProperty("X-Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkaXBha3NoaW5kZUBmb3JiZXNtYXJzaGFsbC5jb20iLCJ1c2VySWQiOiI2Zjc4OWZlMC02NzBmLTExZWQtYThiZi04YjBlNDk2NzZjYmQiLCJzY29wZXMiOlsiVEVOQU5UX0FETUlOIl0sInNlc3Npb25JZCI6ImU2ODRkZDcwLWI5NTItNDMwZi1iZmIzLTY5YTg0ZWU4YTU1NSIsImlzcyI6InRoaW5nc2JvYXJkLmlvIiwiaWF0IjoxNzAxMDgzNDQ4LCJleHAiOjE3MDEwOTI0NDgsImZpcnN0TmFtZSI6IkRpcGFrIiwibGFzdE5hbWUiOiJTaGluZGUiLCJlbmFibGVkIjp0cnVlLCJpc1B1YmxpYyI6ZmFsc2UsInRlbmFudElkIjoiMDE0YjdmMzAtZjRiZS0xMWVhLTk3YjUtZTE4YjM1MDg5MmI1IiwiY3VzdG9tZXJJZCI6IjEzODE0MDAwLTFkZDItMTFiMi04MDgwLTgwODA4MDgwODA4MCJ9.3SVuqZCJQN299JbXGGM6JaatT3kbmOdSnTMUouKSueKtPx4-Q5XGCyEGw857dolPwcMisfG9Okc_A00uzgX14Q");

        // Get the response code
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        // Read the response
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Print the response
        System.out.println("Response: " + response.toString());
    }
}