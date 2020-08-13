package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import com.google.sps.ArticleLabeler;
import java.util.Scanner;
import java.util.ArrayList;
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

    List<Article> retrievedArticles = newsService.getWorldNews(-1);
    response.getWriter().println(gson.toJson(retrievedArticles));
  }
}