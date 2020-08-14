package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/world-news")
public class WorldNewsServlet extends HttpServlet {
  private final NewsService newsService;
  private final Gson gson;
  private static final List<String> ALLOWED_TOPICS = Arrays.asList(
      "business", "entertainment", "health", "science", "sports", "technology", "general"
  );

  public WorldNewsServlet() {
    newsService = new MockNewsService();
    gson = new Gson();
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json; charset=utf-8");

    String topic = request.getParameter("topic");

    if (topic == null || !ALLOWED_TOPICS.contains(topic)) {
      response.setStatus(400);
      String allowedTopicText = String.join(", ", ALLOWED_TOPICS);
      String message = String.format("Requests must contain one of the following topics: %s",
          allowedTopicText);
      response.getWriter().write(message);
    } else {

      try {
        List<Article> retrievedArticles = newsService.getWorldNews(topic, 100);
        response.getWriter().write(gson.toJson(retrievedArticles));
      } catch (NewsUnavailableException e) {
        e.printStackTrace();
        response.setStatus(500);
        response.getWriter().write(e.getMessage());
      }
    }
  }
}