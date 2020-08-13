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
    String articles = retrieveArticles();
    // storeComment();
    response.setContentType("application/json;");
    response.getWriter().println(articles);
  }

  // Retrieves comment entry and stores it in the datastore service
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    storeComment();
    // Respond with the result.
    response.setContentType("application/json;");
    response.getWriter().println();
  }
  
  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null || value.length() == 0) {
      return defaultValue;
    }
    return value;
  }
    
  //  Retrieves the user's comments from the datastore service and returns it as a Json String of their name and the comment
  public String retrieveArticles(){
    //   sort entries by most recent
    Query query = new Query("Article");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    ArrayList<Article> articles = new ArrayList<Article>();
    for (Entity entity : results.asIterable()) {
      articles.add(
    new Article(
      (String) entity.getProperty("title"),
      (String) entity.getProperty("publisher"),
      Instant.parse((String)entity.getProperty("date")),
      (String) entity.getProperty("description"),
      (String) entity.getProperty("url"),
      (String) entity.getProperty("thumbnailUrl"), 
      (new Location(null, null, (String) entity.getProperty("location"))), 
      (String) entity.getProperty("theme"))
      );
      System.out.println((String) entity.getProperty("description"));
    }
    
    return convertToJson(articles);
  }

  //   Stores the user's comments in the datastore service
  public void storeComment(){

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity userComments = new Entity("Article");
    userComments.setProperty("title", "Nikola Sells 2,500 Garbage Trucks. It’s Not the Badger, but It’s a Better Deal. - Barron's");
    userComments.setProperty("publisher","Barron's");
    userComments.setProperty("date", "2020-08-11T18:08:00Z");
    userComments.setProperty("description", "Nikola\r\n shares were trading lower Tuesday, after a 22% bounce on Monday following the announcement of an order for 2,500 battery-powered trucks from waste hauler\r\n Republic Services.Investors were h…");    
    userComments.setProperty("url", "https://www.barrons.com/articles/nikola-battery-trucks-republic-services-binding-order-51597158795");
    userComments.setProperty("thumbnailUrl", "https://images.barrons.com/im-219399/social");
    userComments.setProperty("location", "England");
    userComments.setProperty("theme", "business");    

    datastore.put(userComments);
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
