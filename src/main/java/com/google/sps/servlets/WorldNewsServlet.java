package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Arrays;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import com.google.sps.ArticleLabeler;
import java.util.Scanner;
import java.util.ArrayList;
import com.google.gson.Gson;
import java.time.Instant;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

@WebServlet("/world-news")
public class WorldNewsServlet extends HttpServlet {
  private final Gson gson;

  public WorldNewsServlet() {
    gson = new Gson();
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json; charset=utf-8");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String frontFacingEntityName = getFrontFacingEntityName(datastore);
    List<Article> retrievedArticles = getArticles(datastore, frontFacingEntityName);
    response.getWriter().write(gson.toJson(retrievedArticles));
  }

  private String getFrontFacingEntityName(DatastoreService datastore) {
    Query query = new Query("WorldNewsSource");
    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
      String source = (String) entity.getProperty("Source");
      if (source != null) {
        return source;
      }
    }
    return "Articles_A";
  }

  private List<Article> getArticles(DatastoreService datastore, String name) {
    List<Article> articles = new ArrayList<Article>();
    Query query = new Query(name);
    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
      String title = (String) entity.getProperty("title");
      String publisher = (String) entity.getProperty("publisher");
      String dateString = (String) entity.getProperty("date");
      Instant date = Instant.parse(dateString);
      String description = (String) entity.getProperty("description");
      String url = (String) entity.getProperty("url");
      String thumbnailUrl = (String) entity.getProperty("thumbnailUrl");
      String city = (String) entity.getProperty("locationCity");
      String subCountry = (String) entity.getProperty("locationSubCountry");
      String country = (String) entity.getProperty("locationCountry");
      Location location = new Location(city, subCountry, country);
      String theme = (String) entity.getProperty("theme");
      if (title != null && publisher != null && date != null && description != null && url != null && thumbnailUrl != null && location != null && theme != null) {
        Article article = new Article(title, publisher, date, description, url, thumbnailUrl, location, theme);
        articles.add(article);
      }
    }
    return articles;
  }
}