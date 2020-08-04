package com.google.sps.servlets;

import java.util.List;
import java.lang.IllegalArgumentException;

class CustomSearchNewsService implements NewsService {

  private final CustomSearchNewsRequester requester;
  private final CustomSearchNewsParser parser;

  CustomSearchNewsService() {
    requester = new CustomSearchNewsRequester();
    parser = new CustomSearchNewsParser();
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
