package com.google.sps.servlets;

import java.util.List;
import java.util.ArrayList;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import com.joestelmach.natty.Parser;
import com.joestelmach.natty.DateGroup;

class ManualCustomSearchNewsParser implements CustomSearchNewsParser {

  private final Gson gson;
  
  ManualCustomSearchNewsParser() {
    gson = new Gson();
  }

  public List<Article> parseResults(List<String> resultJsons) {
    List<Article> parsedArticles = new ArrayList<>();

    for (String json : resultJsons) {
      try {
        JsonObject results = gson.fromJson(json, JsonObject.class);
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
      title = article.getAsJsonObject("pagemap")
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
      publisher = pagemap.getAsJsonArray("metatags")
        .get(0)
        .getAsJsonObject()
        .getAsJsonPrimitive("og:site_name")
        .getAsString();
    } catch (NullPointerException e) { }

    if (publisher == null) {
      try {
        publisher = pagemap.getAsJsonArray("organization")
          .get(0)
          .getAsJsonObject()
          .getAsJsonPrimitive("name")
          .getAsString();
      } catch (NullPointerException e) { }
    }

    if (publisher == null) {
      try {
        //Use the site's host name as a fallback when no organization was supplied.
        String url = getUrl(article);
        publisher = getHostName(url);
      } catch (NullPointerException | URISyntaxException e) { }
    }

    return publisher;
  }

  private String getHostName(String url) throws URISyntaxException {
    URI uri = new URI(url);
    String hostName = uri.getHost();
    if (hostName != null) {
        return hostName.startsWith("www.") ? hostName.substring(4) : hostName;
    }
    return hostName;
  }

  private Instant getDate(JsonObject article) {
    String formattedDate = getFormattedDate(article);
    Instant date;

    if (formattedDate != null) {
      try {
        date = parseDate(formattedDate);
      } catch(DateTimeParseException e) {
        date = null;
      }
    } else {
      date = null;
    }

    return date;
  }

  private Instant parseDate(String formattedDate) {
    Parser dateParser = new Parser();
    Instant now = Instant.now();
    Instant truncatedNow = now.truncatedTo(ChronoUnit.DAYS);
    List<DateGroup> dateGroups = dateParser.parse(formattedDate);
    // Search for the first date that isn't in the future.
    // Sometimes natty has issues with parsing things like "Friday", 
    // and assigns them as relative dates.
    for (DateGroup group : dateGroups) {
      for (Date date : group.getDates()) {
        Instant potentialDate = date.toInstant();
        Instant truncatedPotentialDate = potentialDate.truncatedTo(ChronoUnit.DAYS);
        if (truncatedPotentialDate.compareTo(truncatedNow) <= 0) {
          return potentialDate;
        }
      }
    }
    return null;
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

    if (formattedDate == null) {
      try {
        formattedDate = articleData.getAsJsonPrimitive("dateposted")
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
      description = articleData.getAsJsonPrimitive("description")
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

  private String getUrl(JsonObject article) {
    return article.getAsJsonPrimitive("link")
      .getAsString();
  }

  private String getThumbnailUrl(JsonObject article) {
    JsonObject pagemap = article.getAsJsonObject("pagemap");

    String thumbnailUrl = null;

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
  private Location getLocation(JsonObject article) {
    return new Location("City", "State", "Country");
  }
}