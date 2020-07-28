package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import com.google.gson.Gson;

@WebServlet("/world-news")
public class WorldNewsServlet extends HttpServlet {
  private final NewsService newsService;
  private final Gson gson;
  
  public WorldNewsServlet() {
    newsService = new MockNewsService();
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
        topic = ""; // No topic is acceptable, so we just replace it with an empty string to avoid NullPointerExceptions
      }

      List<Article> retrievedArticles = newsService.getWorldNews(10);
      response.getWriter().println(gson.toJson(retrievedArticles));
    }
  }
}