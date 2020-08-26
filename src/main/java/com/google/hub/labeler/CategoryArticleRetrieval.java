// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.hub.labeler;

import com.google.hub.news.newsapi.NewsApiNewsService;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList; 
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.util.List;
import java.util.ArrayList;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import com.google.cloud.translate.*;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.util.Arrays;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.cloud.MonitoredResource;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.logging.Payload.StringPayload;
import com.google.cloud.logging.Severity;
import com.google.hub.news.Article;
import com.google.hub.news.Location;
import com.google.hub.news.NewsService;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

/*
<basic-scaling>
    <max-instances>200</max-instances>
  </basic-scaling>
*/

// Servlet which retrieves and caches world news articles.
@WebServlet("/articles")
public class CategoryArticleRetrieval extends HttpServlet {

  private static final List<String> ALLOWED_TOPICS = Arrays.asList(
      "business", "entertainment", "health", "science", "sports", "technology", "general"
  );
  private final NewsService newsService;

  public CategoryArticleRetrieval() {
    newsService = new NewsApiNewsService();
  }
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    getFreshWorldNews();
    response.setContentType("application/json;");
    response.getWriter().println();
  }

  private void getFreshWorldNews() {
    ServletLogger.logText("Start getFreshWorldNews()");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String backFacingEntityName = getBackFacingEntityName(datastore);
    deleteOldEntites(datastore, backFacingEntityName);
    int countOfArticlesStored = getAndStoreArticles(datastore, backFacingEntityName);
    updateFrontFacingEntity(datastore, backFacingEntityName, countOfArticlesStored);
    ServletLogger.logText("End getFreshWorldNews()");
  }

/** 
    Gets and stores all the valid articles for all of the categories.
*/
  private int getAndStoreArticles(DatastoreService datastore, String backFacingEntityName) {
    int count = 0;
    ServletLogger.logText("Start getAndStoreArticles()");
    Set<String> foundUrls = new HashSet<String>();
    for (String topic : ALLOWED_TOPICS) {
      List<Article> topicArticles = retrieveTopic(topic);
      if (topicArticles != null) {
        for (Article article : topicArticles) {
          if (article != null && inEnglish(article.description) && !foundUrls.contains(article.url)) {
            ServletLogger.logText("Getting Correct Location For : " + article.title);
            Location bestLocation = getBestLocationGuess(article.url);
            ServletLogger.logText("Done Getting Correct Location For : " + article.title);
            if (bestLocation != null) {
              Article finalArticleVersion = new Article(article.title, article.publisher, article.date, article.description, article.url, article.thumbnailUrl, bestLocation, topic);
              ServletLogger.logText("Storing: " + finalArticleVersion.title);
              if (storeArticle(datastore, backFacingEntityName, finalArticleVersion)) {
                count += 1;
                foundUrls.add(finalArticleVersion.url);
              }
            }
          }
        }
      } 
    }
    ServletLogger.logText("End getAndStoreArticles() | Count " + count);
    return count;
  }

  private void updateFrontFacingEntity(DatastoreService datastore, String newName, int articleCount) {
    Query query = new Query("WorldNewsSource");
    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
        entity.setProperty("Source", newName);
        entity.setProperty("ArticleCount", articleCount);
        datastore.put(entity);
        return;
    }
  }

  private boolean storeArticle(DatastoreService datastore, String entityName, Article article) {
    try {
      if (article.title != null && article.publisher != null && article.date != null && article.description != null && article.url != null && article.thumbnailUrl != null && article.location != null && article.theme != null) {
        Entity entity = new Entity(entityName);
        entity.setProperty("title", article.title);
        entity.setProperty("publisher", article.publisher);
        entity.setProperty("date", article.date.toString());
        entity.setProperty("description", article.description);    
        entity.setProperty("url", article.url);
        entity.setProperty("thumbnailUrl", article.thumbnailUrl);
        entity.setProperty("locationCity", article.location.city);
        entity.setProperty("locationSubCountry", article.location.subcountry);
        entity.setProperty("locationCountry", article.location.country);
        entity.setProperty("theme", article.theme);    
        datastore.put(entity);
        ServletLogger.logText("Stored: " + article.title);
        return true;
      } else {
        ServletLogger.logText("Failed to Store: " + article.title);
        return false;
      }
    } catch (Exception e) {
      ServletLogger.logText("Failed to Store: " + article.title + " | Error : " + e);
      return false;
    }
  }

  private void deleteOldEntites(DatastoreService datastore, String name) {
    ServletLogger.logText("Start deleteOldEntites()");
    Query query = new Query(name);
    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
      datastore.delete(entity.getKey());
    }
    ServletLogger.logText("End deleteOldEntites()");
  } 

  private String getBackFacingEntityName(DatastoreService datastore) {
    ServletLogger.logText("Start getBackFacingEntityName()");
    Query query = new Query("WorldNewsSource");
    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
      String source = (String) entity.getProperty("Source");
      if (source.equals("Articles_A")){
        return "Articles_B";
      } else {
        return "Articles_A";
      }
    }
    ServletLogger.logText("End getBackFacingEntityName()");
    return "Articles_A";
  }

  private List<Article> retrieveTopic(String topic)  {
    ServletLogger.logText("Start retrieveTopic() | Topic : " + topic);
    if (topic != null && ALLOWED_TOPICS.contains(topic)) {
      try {
        List<Article> retrievedArticles = newsService.getWorldNews(topic, 100);
        ServletLogger.logText("Good End retrieveTopic() | Topic : " + topic);
        return retrievedArticles;
      } catch (Exception e) {
        ServletLogger.logText("Bad End retrieveTopic() | Topic : " + topic + " | Error " + e.toString());
        return null;
      }
    }
    ServletLogger.logText("Bad End retrieveTopic() | Topic : " + topic);
    return null;
  }

/**
   * Converts a  instance into a JSON string using manual String concatentation.
   */
  private String convertToJson(ArrayList<Article> articles) {
    Gson gson = new Gson();
    String articlesJson = gson.toJson(articles);

    String json = "{";
    json += "\"articles\":";
    json += articlesJson;
    json += "}";
    return json;
  }

  /**
    Returns the guessed location for the provided url. Returns null if none can be found.
  */
  private Location getBestLocationGuess(String url) {
    int attempts = 0;
    Location guess = null;
    ArticleLabeler labeler = new ArticleLabeler(this.getServletContext(), "/WEB-INF/fullcitylist.csv");
    while (attempts < 3 && guess == null) {
      guess = labeler.useCloudToFindLocation(url);
      attempts++;
    }
    return guess;
  }

  /**
    Returns if the provided text is in english.
  */
  private boolean inEnglish(String text) {
    if (text != null) {
      Translate translate = TranslateOptions.getDefaultInstance().getService();
      Detection detection = translate.detect(text);
      if (detection != null) {
        return detection.getLanguage().equals("en");
      } else {
        return false;
      }
    } else {
      return false;
    }
  }
  
}
