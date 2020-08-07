package com.google.sps.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.lang.IllegalArgumentException;
import java.lang.Math;
import java.lang.StringBuilder;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class CustomSearchNewsRequester {

  private static final int MAX_ARTICLES_PER_REQUEST = 10;
  private static final String CUSTOM_SEARCH_API_URL = "https://www.googleapis.com/customsearch/v1";
  private static final String SEARCH_ENGINE_ID = "018391807022102648047:p8ac_1pp880";
  private static final String ENGLISH_LANGUAGE_CODE = "lang_en";
  private static final String RECENT_DATE_BIAS = "date:d:s";
  private static final Duration MAXIMUM_ARTICLE_AGE = Duration.ofDays(7);  

  private final String apiKey;

  CustomSearchNewsRequester() {
    this.apiKey = getApiKey();
  }
  
  private String getApiKey() {
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(getClass().getResourceAsStream("/CSE_API_KEY.txt"))
    );
    try {
      return reader.readLine();
    } catch (IOException e) {
      throw new NewsUnavailableException("Unable to read API key.", e);
    }
  }

  List<String> request(String region, String topic, int count) {
    String encodedSearchQuery = buildEncodedSearchQuery(region, topic);
    return getResults(encodedSearchQuery, count);
  }

  private String buildEncodedSearchQuery(String region, String topic) {
    try {
      String searchQuery = buildSearchQuery(region, topic);
      return URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      String errorMessage = 
          String.format("Could not encode search query for region \"%s\" and topic\" %s\"",
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

  private List<String> getResults(String encodedSearchQuery, int count) {
    List<String> results = new ArrayList<>();
    for (int i = 0; i < Math.ceil((float) count / MAX_ARTICLES_PER_REQUEST); i++) {
      int articlesRemaining = count - MAX_ARTICLES_PER_REQUEST * i;
      int numberOfArticlesForNextRequest = 
          (articlesRemaining < MAX_ARTICLES_PER_REQUEST) 
          ? articlesRemaining : MAX_ARTICLES_PER_REQUEST;
      int offset = MAX_ARTICLES_PER_REQUEST * i;
      String response = queryCustomSearch(encodedSearchQuery, numberOfArticlesForNextRequest,
          offset);
      results.add(response);
    }
    return results;
  }

  private String queryCustomSearch(String searchQuery, int count, int offset) {
    String queryString = String.format("cx=%s&key=%s&q=%s&lr=%s&sort=%s",
        SEARCH_ENGINE_ID, 
        apiKey, 
        searchQuery, 
        ENGLISH_LANGUAGE_CODE,
        buildSortParameter());
    String fullSearchUrl = String.format("%s?%s", CUSTOM_SEARCH_API_URL, queryString);
    System.out.println(fullSearchUrl);
    return sendSearchGetRequest(fullSearchUrl);
  }

  private String buildSortParameter() {
    Instant now = Instant.now();
    Instant lowerBound = now.minusSeconds(MAXIMUM_ARTICLE_AGE.getSeconds());
    ZoneId utc = ZoneId.of("UTC");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(utc);
    String isoFormattedDate = formatter.format(lowerBound);
    return String.format("%s,date:r:%s:", RECENT_DATE_BIAS, isoFormattedDate);
  }

  private String sendSearchGetRequest(String searchUrl) {
    try {
      URL requestUrl = new URL(searchUrl);
      HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
      return extractResultsFromConnection(conn);
    } catch (IOException e) {
      throw new NewsUnavailableException("Could not connect to API.", e);
    }
  }

  private String extractResultsFromConnection(HttpURLConnection conn) {
    StringBuilder response = new StringBuilder(); 

    try {
      conn.setRequestMethod("GET");
      int responseCode = conn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
          for (String line = in.readLine(); line != null; line = in.readLine()) {
            response.append(line);
          } 
        } catch (IOException e) {
          throw new NewsUnavailableException("Failed to read API response.", e);
        }
      } else {
        throw new NewsUnavailableException(String.format("Response code %d from API", 
            responseCode));
      }
    } catch (IOException e) {
      throw new NewsUnavailableException("Failed to get data from API.", e);
    }

    return response.toString();
  }
}