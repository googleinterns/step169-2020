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
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import com.joestelmach.natty.Parser;
import com.joestelmach.natty.DateGroup;

class CustomSearchNewsService implements NewsService {

  public static final int MAX_ARTICLES_PER_REQUEST = 10;
  public static final String CUSTOM_SEARCH_API_URL = "https://www.googleapis.com/customsearch/v1";
  public static final String SEARCH_ENGINE_ID = "018391807022102648047:p8ac_1pp880";
  public static final String ENGLISH_LANGUAGE_CODE = "lang_en";
  public static final String RECENT_DATE_BIAS = "date:d:s";

  private final Gson gson;
  private final String apiKey;

  CustomSearchNewsService() {
    gson = new Gson();
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

    String encodedSearchQuery = buildEncodedSearchQuery(region, topic);

    return retrieveArticles(encodedSearchQuery, count);
  }

  private String buildEncodedSearchQuery(String topic, String region) {
    try {
      String searchQuery = buildSearchQuery(topic, region);
      return URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      String errorMessage = 
        String.format("Could not encode search query for topic \"%s\" and region\" %s\"", topic, region);
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

  private List<Article> retrieveArticles(String encodedSearchQuery, int count) {
    List<Article> articles = new ArrayList<>();
    for (int i = 0; i < Math.ceil((float) count / MAX_ARTICLES_PER_REQUEST); i++) {
      int articlesRemaining = count - MAX_ARTICLES_PER_REQUEST*i;
      int numberOfArticlesForNextRequest = 
        (articlesRemaining < MAX_ARTICLES_PER_REQUEST) ? articlesRemaining : MAX_ARTICLES_PER_REQUEST;
      int offset = MAX_ARTICLES_PER_REQUEST * i;
      String response = queryCustomSearch(encodedSearchQuery, numberOfArticlesForNextRequest, offset);
      articles.addAll(parseResults(response));
    }
    return articles;
  }

  private String queryCustomSearch(String searchQuery, int count, int offset) {
    String queryString = String.format("cx=%s&key=%s&q=%s&lr=%s&sort=%s",
      SEARCH_ENGINE_ID, 
      apiKey, 
      searchQuery, 
      ENGLISH_LANGUAGE_CODE,
      RECENT_DATE_BIAS);
    String fullSearchUrl = String.format("%s?%s", CUSTOM_SEARCH_API_URL, queryString);

    return sendSearchGetRequest(fullSearchUrl);
  }

  private String sendSearchGetRequest(String searchUrl) {
    try {
      URL requestUrl = new URL(searchUrl);
      HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
      return retrieveAPIResponse(conn);
    } catch (IOException e) {
      throw new NewsUnavailableException("Could not connect to API.", e);
    }
  }

  private String retrieveAPIResponse(HttpURLConnection conn) {
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
        throw new NewsUnavailableException(String.format("Response code %d from API", responseCode));
      }
    } catch (IOException e) {
      throw new NewsUnavailableException("Failed to retrieve data from API.", e);
    }

    return response.toString();
  }

  private List<Article> parseResults(String json) {
    List<Article> parsedArticles = new ArrayList<>();
    JsonObject results = gson.fromJson(json, JsonObject.class);

    try {
      JsonArray jsonArticles = results.getAsJsonArray("items");

      for (JsonElement article : jsonArticles) {
        try {
          parsedArticles.add(parseArticle(article.getAsJsonObject()));
        } catch (NullPointerException e) {
          // Ignore this error, because we don't want the entire program 
          // to halt because one article failed to parse.
          // TODO add logging so that articles that fail to parse won't be missed.
        }
      }
    } catch (NullPointerException e) {
      throw new NewsUnavailableException("Failed to parse received json", e);
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

  //TODO implement an actual date parsing algorithm
  private Instant getDate(JsonObject article) {
    String formattedDate = getFormattedDate(article);
    try {
      return parseDate(formattedDate);
    } catch(DateTimeParseException e) {
      return Instant.EPOCH;
    }
  }

  private Instant parseDate(String formattedDate) {
    Parser dateParser = new Parser();
    List<DateGroup> dateGroups = dateParser.parse(formattedDate);
    Instant parsedDate = dateGroups
                    .get(0)
                    .getDates()
                    .get(0)
                    .toInstant();
    return parsedDate;
  }

  private String getFormattedDate(JsonObject article) {
    JsonObject pagemap = article.getAsJsonObject("pagemap");
    JsonArray newsArticleSchema = pagemap.getAsJsonArray("newsarticle");
    JsonObject articleData = newsArticleSchema.get(0).getAsJsonObject();

    String formattedDate = null;

    try {
      formattedDate = articleData.getAsJsonPrimitive("datepublished")
        .getAsString();
    } catch (NullPointerException e) { }

    if (formattedDate == null) {
      try {
        formattedDate = articleData.getAsJsonPrimitive("datecreated")
          .getAsString();
      } catch (NullPointerException e) { }
    }

    if (formattedDate == null) {
      try {
        formattedDate = articleData.getAsJsonPrimitive("datemodified")
          .getAsString();
      } catch (NullPointerException e) { }
    }
    return formattedDate;
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
      thumbnailUrl = pagemap.getAsJsonArray("cse_image")
        .get(0)
        .getAsJsonObject()
        .getAsJsonPrimitive("src")
        .getAsString();
    } catch (NullPointerException e) { }

    if (thumbnailUrl == null) {
      try {
        thumbnailUrl = pagemap.getAsJsonArray("cse_thumbnail")
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