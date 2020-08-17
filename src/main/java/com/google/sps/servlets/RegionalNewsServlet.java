package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/region-news")
public class RegionalNewsServlet extends HttpServlet {
  private final NewsService newsService;
  private final Gson gson;
  
  public RegionalNewsServlet() {
    newsService = new NewsApiNewsService();
    gson = new GsonBuilder()
        .disableHtmlEscaping()
        .create();
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