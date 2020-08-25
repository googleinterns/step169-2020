package com.google.hub.networking;

import com.google.hub.news.NewsUnavailableException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HttpRequestHandler {
  public String getRequest(String url) {
    return getRequest(url, Collections.emptyMap());
  }

  public String getRequest(String url, Map<String, String> headers) {
    try {
      System.out.printf("Sending request to %s\n", url);
      URL requestUrl = new URL(url);
      HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
      conn.setRequestMethod("GET");
      
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        conn.setRequestProperty(entry.getKey(), entry.getValue());
      }

      return extractResultsFromConnection(conn);
    } catch (IOException e) {
      throw new NewsUnavailableException("Could not connect to API.", e);
    }
  }

  private String extractResultsFromConnection(HttpURLConnection conn) {
    try {
      conn.setRequestMethod("GET");
      int responseCode = conn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        return readApiResponse(conn);
      } else {
        System.err.printf("Bad API Response: %s\n", readApiResponse(conn));
        throw new NewsUnavailableException(String.format("Response code %d from API", 
            responseCode));
      }
    } catch (IOException e) {
      throw new NewsUnavailableException("Failed to get data from API.", e);
    }
  }

  private String readApiResponse(HttpURLConnection conn) {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
      StringBuilder response = new StringBuilder(); 
      for (String line = in.readLine(); line != null; line = in.readLine()) {
        response.append(line);
      } 
      return response.toString();
    } catch (IOException e) {
      throw new NewsUnavailableException("Failed to read API response.", e);
    }
  }
}