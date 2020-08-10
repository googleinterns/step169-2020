package com.google.sps.servlets;

import com.google.gson.Gson;
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
    gson = new Gson();
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    String region = request.getParameter("region");

    if (region == null) {
      response.setStatus(400);
      response.getWriter().println("Requests must contain a region parameter");
    } else {
      String topic = request.getParameter("topic");
      if (topic == null) {
        topic = ""; 
        // No topic is acceptable, so we replace 
        // it with an empty string to avoid NullPointerExceptions
      }

      try {
        List<Article> retrievedArticles = newsService.getRegionalNews(region, topic, 5);
        response.getWriter().println(gson.toJson(retrievedArticles));
      } catch (NewsUnavailableException e) {
        e.printStackTrace();
        response.setStatus(500);
        response.getWriter().println(e.getMessage());
      }
    }
  }
}