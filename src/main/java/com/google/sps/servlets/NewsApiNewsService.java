package com.google.sps.servlets;

import java.util.ArrayList;
import java.util.List;

class NewsApiNewsService implements NewsService {

  private final Requester requester;
  private final NewsApiParser parser;
  private final NewsApiArticleAdapter adapter;

  NewsApiNewsService() {
    requester = new NewsApiRequester();
    parser = new NewsApiParser();
    adapter = new NewsApiArticleAdapter();
  }

  public List<Article> getWorldNews(int count) {
    throw new UnsupportedOperationException();
  }
  
  public List<Article> getRegionalNews(String region, String topic, int count) {
    List<String> apiResponse = requester.request(region, topic, count);
    System.out.println(apiResponse.get(0));
    List<NewsApiResults> parsedResults = parser.parseResults(apiResponse);
    List<Article> regionalArticles = new ArrayList<>();
    for (NewsApiResults results : parsedResults) {
      regionalArticles.addAll(adapter.buildArticlesFrom(results));
    }
    return regionalArticles;
  }
}