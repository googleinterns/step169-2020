package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;

class NewsApiParser {

  private final Gson gson;

  NewsApiParser() {
    gson = new GsonBuilder()
      .serializeNulls()
      .create();
  }

  public List<NewsApiResults> parseResults(List<String> resultJsons) {
    List<NewsApiResults> parsedResults = new ArrayList<>();

    for (String json : resultJsons) {
      parsedResults.add(parseSingleResult(json));
    }

    return parsedResults;
  }

  private NewsApiResults parseSingleResult(String resultJson) {
    return gson.fromJson(resultJson, NewsApiResults.class);
  }
}