package com.google.hub.news.newsapi;

import com.google.hub.news.Article;
import com.google.hub.news.NewsService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewsApiNewsService implements NewsService {

  private final NewsApiRequester requester;
  private final NewsApiParser parser;
  private final NewsApiArticleAdapter adapter;

  public NewsApiNewsService() {
    requester = new NewsApiRequester();
    parser = new NewsApiParser();
    adapter = new NewsApiArticleAdapter();
  }

  @Override
  public List<Article> getWorldNews(String category, int count) {
    List<String> apiResponse = requester.requestWorldNews(category, count);
    return parseResponse(apiResponse);
  }
  
  @Override
  public List<Article> getRegionalNews(String region, String topic, int count) {
    List<String> apiResponse = requester.requestRegionalNews(region, topic, count);
    return parseResponse(apiResponse);
  }

  private List<Article> parseResponse(List<String> apiResponse) {
    List<NewsApiResults> parsedResults = parser.parseResults(apiResponse);
    List<Article> regionalArticles = new ArrayList<>();
    for (NewsApiResults results : parsedResults) {
      regionalArticles.addAll(adapter.buildArticlesFrom(results));
    }
    return regionalArticles;
  }
}