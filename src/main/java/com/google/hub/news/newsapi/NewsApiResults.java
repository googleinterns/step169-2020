package com.google.hub.news.newsapi;

import java.util.Collections;
import java.util.List;

final class NewsApiResults {
  final String status;
  final int totalResults;
  final List<Result> articles;

  NewsApiResults(String status, int totalResults, List<Result> articles) {
    this.status = status;
    this.totalResults = totalResults;
    this.articles = Collections.unmodifiableList(articles);
  }

  final class Result {
    final Source source;
    final String author;
    final String title;
    final String description;
    final String url;
    final String urlToImage;
    final String publishedAt;
    final String content;

    Result(Source source, String author, String title, String description,
        String url, String urlToImage, String publishedAt, String content) {
      this.source = source;
      this.author = author;
      this.title = title;
      this.description = description;
      this.url = url;
      this.urlToImage = urlToImage;
      this.publishedAt = publishedAt;
      this.content = content;
    }
  }

  final class Source {
    final String id;
    final String name;

    Source(String id, String name) {
      this.id = id;
      this.name = name;
    }
  }
}