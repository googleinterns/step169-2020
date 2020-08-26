package com.google.hub.news;

import java.io.IOException;
import java.util.List;

public interface NewsService {
  public List<Article> getWorldNews(String category, int count);
  
  public List<Article> getRegionalNews(String region, String topic, int count);
}