package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import com.google.sps.ArticleLabeler;

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
    ArticleLabeler labeler = ArticleLabeler.getArticleLabeler(this.getServletContext(), "/WEB-INF/fullcitylist.csv");
    String[] urls = {
        "https://abcnews.go.com/Politics/fauci-warns-states-coronavirus-numbers-good/story?id=72059455&cid=clicksource_4380645_2_heads_hero_live_hero_hed",
        "https://abcnews.go.com/Politics/read-president-barack-obamas-eulogy-rep-john-lewis/story?id=72081189&cid=clicksource_4380645_5_three_posts_card_image",
        "https://abcnews.go.com/US/wireStory/philadelphia-trash-piles-pandemic-stymies-removal-72080100?cid=clicksource_4380645_2_heads_hero_live_headlines_hed"
    };
    for (String url : urls) {
        String[] location = labeler.getMostLikelyLocation(url);
        String out = "";
        for (String loc : location) {
            out += " " + loc;
        }
        System.err.println(url + out);
    }
    response.setContentType("application/json;");

    List<Article> retrievedArticles = newsService.getWorldNews(-1);
    response.getWriter().println(gson.toJson(retrievedArticles));
  }
}