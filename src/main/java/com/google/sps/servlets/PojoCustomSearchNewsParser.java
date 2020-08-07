package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;

class PojoCustomSearchNewsParser implements CustomSearchNewsParser {

  private final Gson gson;

  PojoCustomSearchNewsParser() {
    gson = new GsonBuilder()
      .serializeNulls()
      .create();
  }

  public List<Article> parseResults(List<String> resultJsons) {
    List<Article> parsedArticles = new ArrayList<>();

    for (String json : resultJsons) {
      parsedArticles.addAll(parseSingleResult(json));
    }

    return parsedArticles;
  }

  private List<Article> parseSingleResult(String resultJson) {
    List<Article> parsedArticles = new ArrayList<>();
    CustomSearchResults results = gson.fromJson(resultJson, CustomSearchResults.class);
    parsedArticles.addAll(results.getArticles());
    return parsedArticles;
  }
}