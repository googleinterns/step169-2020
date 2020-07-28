package com.google.sps.servlets;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.Instant;
import java.lang.IllegalArgumentException;
import java.lang.UnsupportedOperationException;
import java.lang.StringBuilder;
import java.util.Collections;
import java.util.stream.Collectors;
import java.lang.Math;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

class CustomSearchNewsService implements NewsService {

  public static final int MAX_ARTICLES_PER_REQUEST = 10;
  public static final String CUSTOM_SEARCH_API_URL = "https://www.googleapis.com/customsearch/v1";
  public static final String SEARCH_ENGINE_ID = "018391807022102648047:p8ac_1pp880";
  public static final String ENGLISH_LANGUAGE_CODE = "lang_en";

  private final Gson gson;
  private final String apiKey;

  CustomSearchNewsService() {
    gson = new GsonBuilder()
      .setFieldNamingStrategy(f -> f.getName().toLowerCase())
      .create();
    apiKey = getApiKey();
  }

  private static String getApiKey() {
    return System.getenv("CSE_API_KEY");
  }

  @Override
  public List<Article> getWorldNews(int count) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Article> getRegionalNews(String region, String topic, int count) {
    if (region == null || region.isEmpty()) {
      throw new IllegalArgumentException("A region must be defined");
    }

    String searchQuery = region;
    if (topic != null && !topic.isEmpty()) {
      searchQuery += String.format(" +\"%s\"", topic);
    }

    String encodedSearchQuery;
    try {
      encodedSearchQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      String errorMessage = 
        String.format("Could not encode search query for topic \"%s\" and region\" %s\"", topic, region);
      throw new IllegalArgumentException(errorMessage);
    }

    List<Article> articles = new ArrayList<>();
    for (int i = 0; i < Math.ceil((float) count / MAX_ARTICLES_PER_REQUEST); i++) {
      int articlesRemaining = count - MAX_ARTICLES_PER_REQUEST*i;
      int numberOfArticlesForNextRequest = 
        (articlesRemaining < MAX_ARTICLES_PER_REQUEST) ? articlesRemaining : MAX_ARTICLES_PER_REQUEST;
      int offset = MAX_ARTICLES_PER_REQUEST * i;
      try {
        String response = queryCustomSearch(encodedSearchQuery, numberOfArticlesForNextRequest, offset);
          articles.addAll(parseResults(response));
      } catch (IOException e) {
        //TODO Find a way to gandle this exception
        // throw new IOException(
        //   String.format("Failed to retrieve articles %d to %d from API", 
        //     offset, offset + numberOfArticlesForNextRequest - 1)
        // );
      }
    }

    return articles;
  }

  private String queryCustomSearch(String searchQuery, int count, int offset) throws IOException {
    String queryString = String.format("cx=%s&key=%s&q=%s&lr=%s", 
      SEARCH_ENGINE_ID, 
      apiKey, 
      searchQuery, 
      ENGLISH_LANGUAGE_CODE);
    String fullSearchUrl = String.format("%s?%s", CUSTOM_SEARCH_API_URL, queryString);

    try {
      return sendSearchGetRequest(fullSearchUrl);
    } catch (IOException e) {
      throw new IOException("Failed to retrieve news from API");
    }
  }

  private String sendSearchGetRequest(String searchUrl) throws IOException {
    URL requestUrl = new URL(searchUrl);
    HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();

    StringBuilder response = new StringBuilder(); 

    conn.setRequestMethod("GET");
    int responseCode = conn.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
      try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
        for (String line = in.readLine(); line != null; line = in.readLine()) {
          response.append(line);
        } 
      }
    }

    return response.toString();
  }

  private List<Article> parseResults(String json) {
    JsonObject results = gson.fromJson(json, JsonObject.class);
    JsonArray jsonArticles = results.getAsJsonArray("items");
    
    List<Article> parsedArticles = new ArrayList<>();
    for (JsonElement article : jsonArticles) {
      try {
        parsedArticles.add(parseArticle(article.getAsJsonObject()));
      } catch (NullPointerException e) {
         // Ignore this error, because we don't want the entire program 
         // to halt because one article failed to parse.
         // TODO add logging so that articles that fail to parse won't be missed.
      }
    }

    return parsedArticles;
  }

  private Article parseArticle(JsonObject article) {
    return new Article(getTitle(article),
      getPublisher(article),
      getDate(article),
      getDescription(article),
      getUrl(article),
      getThumbnailUrl(article),
      getLocation(article));
  }

  private String getTitle(JsonObject article) {
    String title = null;
    try {
      article.getAsJsonObject("pagemap")
        .getAsJsonArray("newsarticle")
        .get(0)
        .getAsJsonObject()
        .getAsJsonPrimitive("headline")
        .getAsString();
    } catch (NullPointerException e) { }

    if (title == null) {
      try {
        title = article.getAsJsonPrimitive("title")
          .getAsString();
      } catch (NullPointerException e) { }
    }

    return title;
  }

  private String getPublisher(JsonObject article) {
    JsonObject pagemap = article.getAsJsonObject("pagemap");
    String publisher = null;
    try {
     pagemap.getAsJsonArray("organization")
        .get(0)
        .getAsJsonObject()
        .getAsJsonPrimitive("name")
        .getAsString();
    } catch (NullPointerException e) { }

    if (publisher == null) {
      try {
        publisher = pagemap.getAsJsonArray("metatags")
          .get(0)
          .getAsJsonObject()
          .getAsJsonPrimitive("og:site_name")
          .getAsString();
      } catch (NullPointerException e) { }
    }

    return publisher;
  }

  //TODO implement an actual date parsing algortihm
  private Instant getDate(JsonObject article) {
    return Instant.now();
  }

  private String getDescription(JsonObject article) {
    JsonObject pagemap = article.getAsJsonObject("pagemap");
    JsonArray newsArticleSchema = pagemap.getAsJsonArray("newsarticle");
    JsonObject articleData = newsArticleSchema.get(0).getAsJsonObject();

    String description =  null; 
    
    try {
      articleData.getAsJsonPrimitive("description")
      .getAsString();
    } catch (NullPointerException e) { }

    if (description == null) {
      try {
        description = articleData.getAsJsonPrimitive("articlebody")
          .getAsString();
      } catch (NullPointerException e) { }
    }

    if (description == null) {
      try {
        description = article.getAsJsonPrimitive("snippet")
          .getAsString();
      } catch (NullPointerException e) { }
    }

    return description;
  }

  String getUrl(JsonObject article) {
    return article.getAsJsonPrimitive("link")
      .getAsString();
  }

  String getThumbnailUrl(JsonObject article) {
    String thumbnailUrl = null;
    JsonObject pagemap = article.getAsJsonObject("pagemap");

    try {
      pagemap.getAsJsonArray("cse_image")
        .get(0)
        .getAsJsonObject()
        .getAsJsonPrimitive("src")
        .getAsString();
    } catch (NullPointerException e) { }

    if (thumbnailUrl == null) {
      try {
      pagemap.getAsJsonArray("cse_thumbnail")
        .get(0)
        .getAsJsonObject()
        .getAsJsonPrimitive("src")
        .getAsString();
      } catch (NullPointerException e) { }
    }

    return thumbnailUrl;
  }

  //TODO implement an actual location determination algorithm
  String getLocation(JsonObject article) {
    return "Unknown";
  }
}