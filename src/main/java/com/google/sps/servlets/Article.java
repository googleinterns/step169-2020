package com.google.sps.servlets;

import java.time.Instant;

final class Article {
  final String title;
  final String publisher;
  final Instant date;
  final String description;
  final String url;
  final String thumbnailUrl;
  final Location location;
  final String theme;

  Article(String title, String publisher, Instant date, String description, String url, String thumbnailUrl, Location location, String theme) {
    this.title = title;
    this.publisher = publisher;
    this.date = date;
    this.description = description;
    this.url = url;
    this.thumbnailUrl = thumbnailUrl;
    this.location = location;
    this.theme = theme;
  }
}
