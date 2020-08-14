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

package com.google.sps.servlets;

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
import com.google.sps.ArticleLabeler;


// Servlet which retrieves client String comment entries, stores them on server, and displays them on wall 
@WebServlet("/articles")
public class CategoryArticleRetrieval extends HttpServlet {
  
  //   Sends array of previous recent entries to be fetched
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    storeArticle();
    response.setContentType("application/json;");
    response.getWriter().println();
  }

  //   Stores the user's comments in the datastore service
  public void storeArticle(){

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity article = new Entity("Article");
    article.setProperty("title", "Nikola Sells 2,500 Garbage Trucks. It’s Not the Badger, but It’s a Better Deal. - Barron's");
    article.setProperty("publisher","Barron's");
    article.setProperty("date", "2020-08-11T18:08:00Z");
    article.setProperty("description", "Nikola\r\n shares were trading lower Tuesday, after a 22% bounce on Monday following the announcement of an order for 2,500 battery-powered trucks from waste hauler\r\n Republic Services.Investors were h…");    
    article.setProperty("url", "https://www.barrons.com/articles/nikola-battery-trucks-republic-services-binding-order-51597158795");
    article.setProperty("thumbnailUrl", "https://images.barrons.com/im-219399/social");
    article.setProperty("languageTest", inEnglish("test"));
    String[] location = getBestLocationGuess("https://www.barrons.com/articles/nikola-battery-trucks-republic-services-binding-order-51597158795");
    String loc = "";
    if (location != null) {
        loc = location[0] + "," + location[1] + "," + location[2];
    } else {
        loc = "NOT FOUND";
    }
    article.setProperty("location", loc);
    article.setProperty("theme", "business");    
    datastore.put(article);
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
    Returns the article with the correct location if its valid (findable location and in english), otherwise returns null. 
  */
  private Article getArticleWithCorrectLocation(Article article) {
      if (inEnglish(article.description)) {
          String[] bestLocation = getBestLocationGuess(article.url);
          if (bestLocation != null) {
              String location = bestLocation[0] + "," + bestLocation[1] + "," + bestLocation[2];
              return new Article(article.title, article.publisher, article.date, article.description, article.url, article.thumbnailUrl, article.location, article.theme);
          } else {
              return null;
          }
      } else {
          return null;
      }
  }

  /**
    Returns the guessed location for the provided url. Returns null if none can be found.
  */
  private String[] getBestLocationGuess(String url) {
      int attempts = 0;
      String[] guess = null;
      ArticleLabeler labeler = new ArticleLabeler(this.getServletContext(), "/WEB-INF/fullcitylist.csv");
      while (attempts < 5 && guess == null) {
          guess = labeler.useCloudToFindLocation(url);
          attempts++;
      }
      return guess;
  }

  /**
    Returns if the provided text is in english.
  */
  private boolean inEnglish(String text) {
        Translate translate = TranslateOptions.getDefaultInstance().getService();
        Detection detection = translate.detect(text);
        if (detection != null) {
            return detection.getLanguage().equals("en");
        } else {
            return false;
        }
  }
}
