package com.google.sps.servlets;

import java.util.List;
import java.io.IOException;

interface NewsService {
  List<Article> getWorldNews(int count);
  List<Article> getRegionalNews(String region, String topic, int count);
}