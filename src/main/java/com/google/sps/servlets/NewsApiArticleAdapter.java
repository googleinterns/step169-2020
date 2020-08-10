package com.google.sps.servlets;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

class NewsApiArticleAdapter {
  List<Article> buildArticlesFrom(NewsApiResults results) {
    return results.articles.stream()
        .map(result -> buildSingleArticleFrom(result))
        .collect(Collectors.toList());
  }

  private Article buildSingleArticleFrom(NewsApiResults.Result result) {
    return new Article(
        result.title,
        result.source.name,
        Instant.parse(result.publishedAt),
        result.description,
        result.url,
        result.urlToImage,
        getLocation(result)
    );
  }

  private Location getLocation(NewsApiResults.Result result) {
    return new Location("City", "Subcountry", "Country");
  }
}
