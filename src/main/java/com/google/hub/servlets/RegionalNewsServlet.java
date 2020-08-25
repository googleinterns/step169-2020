package com.google.hub.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.hub.news.Article;
import com.google.hub.news.NewsService;
import com.google.hub.news.NewsUnavailableException;
import com.google.hub.news.newsapi.NewsApiNewsService;
import com.google.hub.search.DatastoreSearchHistoryService;
import com.google.hub.search.SearchHistoryService;
import com.google.hub.search.SearchRequest;
import com.google.hub.users.UserManager;
import com.google.hub.users.UsersApiUserManager;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/region-news")
public class RegionalNewsServlet extends HttpServlet {
  private final NewsService newsService;
  private final Gson gson;
  private final UserManager userManager;
  private final SearchHistoryService searchHistoryService;
  
  public RegionalNewsServlet() {
    newsService = new NewsApiNewsService();
    gson = new GsonBuilder()
        .disableHtmlEscaping()
        .create();
    userManager = new UsersApiUserManager();
    searchHistoryService = new DatastoreSearchHistoryService();
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json; charset=utf-8");

    String region = request.getParameter("region");

    if (region == null) {
      response.setStatus(400);
      response.getWriter().write("Requests must contain a region parameter");
    } else {
      String topic = request.getParameter("topic");
      if (topic == null) {
        topic = ""; 
        // No topic is acceptable, so we replace 
        // it with an empty string to avoid NullPointerExceptions
      }

      if (userManager.userIsLoggedIn()) {
        SearchRequest search = new SearchRequest(region, topic, Instant.now());
        searchHistoryService.addSearch(userManager.currentUserId(), search);
      }

      try {
        List<Article> retrievedArticles = newsService.getRegionalNews(region, topic, 100);
        response.getWriter().write(gson.toJson(retrievedArticles));
      } catch (NewsUnavailableException e) {
        e.printStackTrace();
        response.setStatus(500);
        response.getWriter().write(e.getMessage());
      }
    }
  }
}