package com.google.hub.news;

import java.time.Instant;

public final class Article {
  public final String title;
  public final String publisher;
  public final Instant date;
  public final String description;
  public final String url;
  public final String thumbnailUrl;
  public final Location location;
  public final String theme;

  public Article(String title, String publisher, Instant date, String description,
       String url, String thumbnailUrl, Location location, String theme) {
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
