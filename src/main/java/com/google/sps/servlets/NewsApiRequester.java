package com.google.sps.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class NewsApiRequester implements Requester {

  private final String apiKey;
  private final HttpRequestHandler requestHandler;
  private final static String API_KEY_HEADER = "X-Api-Key";
  
  NewsApiRequester() {
    this.apiKey = getApiKey();
    this.requestHandler = new HttpRequestHandler();
  }
  
  private String getApiKey() {
    
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(getClass().getResourceAsStream("/keys/NEWS_API_KEY.txt"))
    )) {
      return reader.readLine();
    } catch (IOException e) {
      throw new NewsUnavailableException("Unable to read API key.", e);
    }
  }

  public List<String> request(String region, String topic, int count) {
    String encodedSearchQuery = buildEncodedSearchQuery(region, topic);
    String url = buildUrl(encodedSearchQuery);
    Map<String, String> headers = new HashMap<>();
    headers.put(API_KEY_HEADER, apiKey);
    return Arrays.asList(requestHandler.getRequest(url, headers));
  }

  private String buildEncodedSearchQuery(String region, String topic) {
    try {
      String searchQuery = buildSearchQuery(region, topic);
      return URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      String errorMessage = 
          String.format("Could not encode search query for region \"%s\" and topic\"%s\"",
          region, topic);
      throw new IllegalArgumentException(errorMessage);
    }
  }
  
  private String buildSearchQuery(String region, String topic) {
    String searchQuery = region;
    if (topic != null && !topic.isEmpty()) {
      searchQuery += String.format(" +\"%s\"", topic);
    }
    return searchQuery;
  }

  private String buildUrl(String encodedSearchQuery) {
    return String.format("https://newsapi.org/v2/everything?q=%s",
        encodedSearchQuery);
  }
}