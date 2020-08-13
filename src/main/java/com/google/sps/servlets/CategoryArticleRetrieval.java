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

// Servlet which retrieves client String comment entries, stores them on server, and displays them on wall 
@WebServlet("/articles")
public class CategoryArticleRetrieval extends HttpServlet {
  
  //   Sends array of previous recent entries to be fetched
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    mockData();
    response.setContentType("application/json;");
    response.getWriter().println();
  }

  //   Stores the user's comments in the datastore service
  public void storeArticle(String title, String publisher, String date, String description, String url, String thumbnailUrl, String location, String theme){

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity article = new Entity("Article");
    article.setProperty("title", title);
    article.setProperty("publisher",publisher);
    article.setProperty("date", date);
    article.setProperty("description", description);    
    article.setProperty("url", url);
    article.setProperty("thumbnailUrl", thumbnailUrl);
    article.setProperty("location", location);
    article.setProperty("theme", theme);    

    datastore.put(article);
  }

  public void mockData(){

    String title = "Nikola Sells 2,500 Garbage Trucks. It’s Not the Badger, but It’s a Better Deal. - Barron's";
    String publisher = "Barron's";
    String date = "2020-08-11T18:08:00Z";
    String description = "Nikola\r\n shares were trading lower Tuesday, after a 22% bounce on Monday following the announcement of an order for 2,500 battery-powered trucks from waste hauler\r\n Republic Services.Investors were h…";
    String url = "https://www.barrons.com/articles/nikola-battery-trucks-republic-services-binding-order-51597158795";
    String thumbnailUrl = "https://images.barrons.com/im-219399/social";
    String location = "England";
    String theme = "business";
    storeArticle(title, publisher, date, description, url, thumbnailUrl, location, theme);
    
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
}
