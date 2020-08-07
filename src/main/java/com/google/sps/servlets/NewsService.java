package com.google.sps.servlets;

import java.io.IOException;
import java.util.List;

interface NewsService {
  List<Article> getWorldNews(int count);
  
  List<Article> getRegionalNews(String region, String topic, int count);
}