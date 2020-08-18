package com.google.sps.servlets;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;

class NewsApiArticleAdapter {
  private static final DateTimeFormatter formatWithTimeZone = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd'T'HH:mm:ssX")
        .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
        .toFormatter();

  List<Article> buildArticlesFrom(NewsApiResults results) {
    List<Article> articles = new ArrayList<>();
    int i = 0;
    for (NewsApiResults.Result result : results.articles) {
      try {
        articles.add(buildSingleArticleFrom(result));
      } catch (NullPointerException e) {
        System.err.printf("Failed to parse article %d\n", i);
      }
      i++;
    }

    return articles;
  }

  private Article buildSingleArticleFrom(NewsApiResults.Result result) {
    return new Article(
        removeHtmlTags(result.title),
        removeHtmlTags(result.source.name),
        parseDate(result.publishedAt),
        removeHtmlTags(result.description),
        result.url,
        result.urlToImage,
        getLocation(result),
        "miscellaneous"
    );
  }

  private Instant parseDate(String formattedDate) {
    try {
      return Instant.parse(formattedDate);
    } catch (DateTimeParseException e) {
      e.printStackTrace();
    }

    try {
      return formatWithTimeZone.parse(formattedDate, Instant::from);
    } catch (DateTimeParseException e) {
      e.printStackTrace();
      System.err.println("Bad Formatting on date " + formattedDate);
      return null;
    }
  }

  private String removeHtmlTags(String text) {
    try {
      return Jsoup.parse(text).text();
    } catch (NullPointerException e) {
      System.out.printf("Failed to remove HTML from text %s", text);
      return text;
    }
  }

  // TODO implement Max's location script.
  private Location getLocation(NewsApiResults.Result result) {
    return null;
  }
}
