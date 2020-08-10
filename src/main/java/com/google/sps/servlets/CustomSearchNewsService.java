package com.google.sps.servlets;

import java.lang.IllegalArgumentException;
import java.util.List;

class CustomSearchNewsService implements NewsService {

  private final Requester requester;
  private final CustomSearchNewsParser parser;
  private final NewsApiArticleAdapter adapter;

  CustomSearchNewsService() {
    requester = new CustomSearchNewsRequester();
    parser = new PojoCustomSearchNewsParser();
    adapter = new NewsApiArticleAdapter();
  }

  @Override
  public List<Article> getWorldNews(int count) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Article> getRegionalNews(String region, String topic, int count) {
    if (region == null || region.isEmpty()) {
      throw new IllegalArgumentException("A region must be defined");
    }

    List<String> customSearchResponses = requester.request(region, topic, count);

    return parser.parseResults(customSearchResponses);
  }
}
