package com.google.hub.news.newsapi;

import com.google.hub.networking.HttpRequestHandler;
import com.google.hub.news.NewsUnavailableException;
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

class NewsApiRequester {

  private final String apiKey;
  private final HttpRequestHandler requestHandler;
  private final Map<String, String> stateAbbreviationMap;
  private final Map<String, String> headers;
  private static final String API_KEY_HEADER = "X-Api-Key";
  private static final String ENGLISH_LANGUAGE_CODE = "en";
  private static final String US_COUNTRY_CODE = "us";
  private static final int MAX_PAGE_SIZE = 100;

  NewsApiRequester() {
    this.requestHandler = new HttpRequestHandler();
    this.stateAbbreviationMap = buildStateAbbreviationMap();
    this.apiKey = getApiKey();

    Map<String, String> temporaryHeaders = new HashMap<>();
    temporaryHeaders.put(API_KEY_HEADER, apiKey);
    this.headers = Collections.unmodifiableMap(temporaryHeaders);
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

  private Map<String, String> buildStateAbbreviationMap() {
    Map<String, String> countryNameToCode = new HashMap<>();

    try (Scanner reader = new Scanner(new InputStreamReader(
        getClass().getResourceAsStream("/subcountry_abbreviations.csv")))) {
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

  List<String> requestRegionalNews(String region, String topic, int count) {
    String encodedSearchQuery = buildEncodedRegionalSearchQuery(region, topic);
    List<String> responses = new ArrayList<>();
    int pageSize = Math.min(count, MAX_PAGE_SIZE);

    for (int pageNum = 0; pageNum * pageSize < count; pageNum++) {
      String url = buildRegionalNewsUrl(encodedSearchQuery, pageSize, pageNum);
      responses.add(sendRequest(url));
      // Technically this will overcount for something like count=101,  
      // but it won't give us too few articles unless an article is improperly 
      // returned or too few articles exist.
    }

    return responses;
  }

  private String buildEncodedRegionalSearchQuery(String region, String topic) {
    try {
      String searchQuery = buildRegionalSearchQuery(region, topic);
      return encode(searchQuery);
    } catch (UnsupportedEncodingException e) {
      String errorMessage = 
          String.format("Could not encode search query for region \"%s\" and topic \"%s\"",
          region, topic);
      throw new IllegalArgumentException(errorMessage, e);
    }
  }

  private String buildRegionalSearchQuery(String region, String topic) {
    String replacedAnds = region.replaceAll("\\band\\b", "&");
    String[] regionParts = replacedAnds.split(", ");
    if (regionParts.length >= 2 && stateAbbreviationMap.containsKey(regionParts[1])) {
      regionParts[1] = stateAbbreviationMap.get(regionParts[1]);
    }
    String searchQuery = String.join(" AND ", regionParts);

    if (topic != null && !topic.isEmpty()) {
      searchQuery += String.format(" AND %s", topic);
    }

    return searchQuery;
  }

  private String buildRegionalNewsUrl(String encodedSearchQuery, int pageSize, int pageNum) {
    String baseUrl = String.format("https://newsapi.org/v2/everything?q=%s&language=%s",
        encodedSearchQuery,
        ENGLISH_LANGUAGE_CODE);
    String pageSizeParameter = buildPageSizeParameter(pageSize);
    String url = baseUrl + pageSizeParameter;
    if (pageNum != 0) {
      url += buildPageNumberParameter(pageNum);
    }
    return url;
  }

  List<String> requestWorldNews(String category, int count) {
    List<String> responses = new ArrayList<>();
    int pageSize = Math.min(count, MAX_PAGE_SIZE);

    for (int pageNum = 0; pageNum * pageSize < count; pageNum++) {
      String url = buildWorldNewsUrl(category, pageSize, pageNum);
      responses.add(sendRequest(url));
      // Technically this will overcount for something like count=101,  
      // but it won't give us too few articles unless an article is improperly 
      // returned or too few articles exist.
    }

    return responses;
  }

  private String buildWorldNewsUrl(String category, int pageSize, int pageNum) {
    String baseUrl = String.format("https://newsapi.org/v2/top-headlines?category=%s&country=%s",
        category,
        US_COUNTRY_CODE);
    String pageSizeParameter = buildPageSizeParameter(pageSize);
    String url = baseUrl + pageSizeParameter;
    if (pageNum != 0) {
      url += buildPageNumberParameter(pageNum);
    }
    return url;
  }

  private String sendRequest(String url) {
    return requestHandler.getRequest(url, headers);
  }

  private String encode(String toEncode) throws UnsupportedEncodingException {
    return URLEncoder.encode(toEncode, StandardCharsets.UTF_8.toString());
  }

  private String buildPageSizeParameter(int count) {
    return String.format("&pageSize=%d", count);
  }

  private String buildPageNumberParameter(int pageNum) {
    return String.format("&page=%d", pageNum);
  }
}