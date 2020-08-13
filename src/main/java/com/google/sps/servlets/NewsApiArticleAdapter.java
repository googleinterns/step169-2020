package com.google.sps.servlets;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;

class NewsApiArticleAdapter {
  private static final DateTimeFormatter formatWithTimeZone = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd'T'HH:mm:ssX")
        .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
        .toFormatter();

  List<Article> buildArticlesFrom(NewsApiResults results) {
    return results.articles.stream()
        .map(result -> buildSingleArticleFrom(result))
        .collect(Collectors.toList());
  }

  private Article buildSingleArticleFrom(NewsApiResults.Result result) {
    return new Article(
        removeHtmlTags(result.title),
        removeHtmlTags(result.source.name),
        parseDate(result.publishedAt),
        removeHtmlTags(result.description),
        result.url,
        result.urlToImage,
        getLocation(result)
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

  private String removeHtmlTags(String html) {
    return Jsoup.parse(html).text();
  }

  // TODO implement Max's location script.
  private Location getLocation(NewsApiResults.Result result) {
    return new Location("City", "Subcountry", "Country");
  }
}
