package com.google.sps.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class NewsApiRequester implements Requester {

  private final String apiKey;
  private final HttpRequestHandler requestHandler;
  private final Map<String, String> countryCodeMap;
  private static final String API_KEY_HEADER = "X-Api-Key";
  private static final String ENGLISH_LANGUAGE_CODE = "en";
  private static final int MAX_PAGE_SIZE = 100;

  NewsApiRequester() {
    this.apiKey = getApiKey();
    this.requestHandler = new HttpRequestHandler();
    this.countryCodeMap = buildCountryCodeMap();
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

  private Map<String, String> buildCountryCodeMap() {
    Map<String, String> countryNameToCode = new HashMap<>();

    try (Scanner reader = new Scanner(new InputStreamReader(
        getClass().getResourceAsStream("/country_codes.csv")))) {
      while (reader.hasNextLine()) {
        String line = reader.nextLine();
        String[] cells = line.split(",");
        String countryName = cells[0];
        String countryCode = cells[1];

        countryNameToCode.put(countryName, countryCode);
      }
    }

    return Collections.unmodifiableMap(countryNameToCode);
  }

  public List<String> request(String region, String topic, int count) {
    String encodedSearchQuery = buildEncodedSearchQuery(region, topic);
    String baseUrl = buildBaseUrl(encodedSearchQuery);
    int pageSize = Math.min(count, MAX_PAGE_SIZE);
    String pageSizeParameter = buildPageSizeParameter(count);
    String url = baseUrl + pageSizeParameter;
    List<String> responses = new ArrayList<>();
    
    Map<String, String> headers = new HashMap<>();
    headers.put(API_KEY_HEADER, apiKey);

    for (int pageNum = 0; pageNum * pageSize < count; pageNum++) {
      String urlToRequest = url;
      if (pageNum != 0) {
        urlToRequest += buildPageNumberParameter(pageNum);
      }
      responses.add(requestHandler.getRequest(urlToRequest, headers));
      // Technically this will overcount for something like count=101,  
      // but it won't give us too few articles unless an article is improperly 
      // returned or too few articles exist.
    }

    return responses;
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
    String[] regionParts = splitCountryFromRegion(region);
    String preCountry = regionParts[0];
    String searchQuery = preCountry;

    if (topic != null && !topic.isEmpty()) {
      searchQuery += String.format(" +\"%s\"", topic);
    }

    return searchQuery;
  }

  private String buildPageSizeParameter(int count) {
    return String.format("&pageSize=%d", count);
  }

  private String buildPageNumberParameter(int pageNum) {
    return String.format("&page=%d", pageNum);
  }


  private String[] splitCountryFromRegion(String region) {
    String[] regionParts = region.split("[, ]+");
    String country = regionParts[regionParts.length - 1];
    String searchQuery;
    String[] nonCountryParts = Arrays.copyOfRange(regionParts, 0, regionParts.length - 1);
    String prefix = String.join(" ", nonCountryParts);
    return new String[] {prefix, country};
  }

  private String buildBaseUrl(String encodedSearchQuery) {
    return String.format("https://newsapi.org/v2/everything?q=%s&language=%s",
        encodedSearchQuery,
        ENGLISH_LANGUAGE_CODE);
  }
}