package com.google.sps.servlets;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import com.joestelmach.natty.Parser;
import com.joestelmach.natty.DateGroup;

class CustomSearchResults {
  List<Result> items;

  List<Article> getArticles() {
    List<Article> articles = new ArrayList<>();
    for (Result result : items) {
      try {
        articles.add(result.getArticle());
      } catch (NullPointerException e) {
        // Ignore this error, because we don't want the entire program 
        // to halt because one article failed to parse.
        // TODO add logging so that articles that fail to parse won't be missed.
      }
    }
    return articles;
  }

  private class Result {
    String title;
    @SerializedName("pagemap")
    PageMap pageMap; 
    String snippet;
    String link;

    Article getArticle() {
      return new Article(getTitle(),
        getPublisher(),
        getDate(),
        getDescription(),
        getUrl(),
        getThumbnailUrl(),
        getLocation()
      );
    }

    private String getTitle() {
      String title = null;
      if (pageMap.newsArticles != null && !pageMap.newsArticles.isEmpty()) {
        title = pageMap
          .newsArticles
          .get(0)
          .headline;
      }

      if (title == null) {
        title = this.title;
      }

      return title;
    }

    private String getPublisher() {
      String publisher = null;
      if (pageMap.metaTags != null && !pageMap.metaTags.isEmpty()) {
        publisher = pageMap
          .metaTags
          .get(0)
          .ogSiteName;
      }

      if (publisher == null && pageMap.organizations != null && !pageMap.organizations.isEmpty()) {
        publisher = pageMap
          .organizations
          .get(0)
          .name;
      }

      if (publisher == null) {
        try {
          //Use the site's host name as a fallback when no organization was supplied.
          String url = getUrl();
          publisher = getHostName(url);
        } catch (NullPointerException | URISyntaxException e) { }
      }

      return publisher;
    }

    private String getHostName(String url) throws URISyntaxException {
      URI uri = new URI(url);
      String hostName = uri.getHost();
      if (hostName != null) {
          return hostName.startsWith("www.") ? hostName.substring(4) : hostName;
      }
      return hostName;
    }

    private Instant getDate() {
      String formattedDate = getFormattedDate();
      Instant date;

      if (formattedDate != null) {
        try {
          date = parseDate(formattedDate);
        } catch(NullPointerException e) {
          date = null;
        }
      } else {
        date = null;
      }

      return date;
    }

    private String getFormattedDate() {
      String formattedDate = null;
      List<NewsArticle> newsArticles = pageMap.newsArticles;

      if (newsArticles != null && !newsArticles.isEmpty()) {
        NewsArticle article = newsArticles.get(0);
        String[] potentialDates = {article.datePublished, article.datePosted, article.dateCreated, article.dateModified};
        for (String date : potentialDates) {
          formattedDate = date;
          if (formattedDate != null) {
            break;
          }
        }
      }

      return formattedDate;
    }

    private Instant parseDate(String formattedDate) {
      Parser dateParser = new Parser();
      Instant now = Instant.now();
      Instant truncatedNow = now.truncatedTo(ChronoUnit.DAYS);
      List<DateGroup> dateGroups = dateParser.parse(formattedDate);
      // Search for the first date that isn't in the future.
      // Sometimes natty has issues with parsing things like "Friday", 
      // and assigns them as relative dates.
      for (DateGroup group : dateGroups) {
        for (Date date : group.getDates()) {
          Instant potentialDate = date.toInstant();
          Instant truncatedPotentialDate = potentialDate.truncatedTo(ChronoUnit.DAYS);
          if (truncatedPotentialDate.compareTo(truncatedNow) <= 0) {
            return potentialDate;
          }
        }
      }
      return null;
    }

    private String getDescription() {
      String description = null;
      List<NewsArticle> newsArticles = pageMap.newsArticles;

      if (newsArticles != null && !newsArticles.isEmpty()) {
        NewsArticle article = newsArticles.get(0);
        String[] potentialDescriptions = {article.description, article.articleBody};
        for (String desc : potentialDescriptions) {
          description = desc;
          if (description != null) {
            break;
          }
        }
      }

      if (description == null) {
        description = snippet;
      }
      return description;
    }

    private String getUrl() {
      return link;
    }

    private String getThumbnailUrl() {
      String thumbnailUrl = null;
      if (pageMap.cseImages != null && !pageMap.cseImages.isEmpty()) {
        thumbnailUrl = pageMap.cseImages.get(0).src;
      }
      if (pageMap.cseThumbnails != null && !pageMap.cseThumbnails.isEmpty()) {
        thumbnailUrl = pageMap.cseThumbnails.get(0).src;
      }
      return thumbnailUrl;
    }

    //TODO implement an actual location determination algorithm
    private Location getLocation() {
      return new Location("City", "State", "Country");
    }
  }

  private class PageMap {
    @SerializedName(value = "newsarticle", alternate = {"newsArticle", "NewsArticle", "Newsarticle"})
    List<NewsArticle> newsArticles;
    @SerializedName(value = "metatags", alternate = {"metaTags", "MetaTags", "Metatags"})
    List<MetaTags> metaTags;
    @SerializedName(value = "organization", alternate = {"Organization"})
    List<Organization> organizations;
    @SerializedName("cse_image")
    List<Thumbnail> cseImages;
    @SerializedName("cse_thumbnail")
    List<Thumbnail> cseThumbnails;
  }

  private class NewsArticle {
    @SerializedName(value = "headline", alternate = {"Headline"})
    String headline;
    @SerializedName(value = "datepublished", alternate = {"datePublished", "DatePublished", "Datepublished"})
    String datePublished;
    @SerializedName(value = "datecreated", alternate = {"dateCreated", "DateCreated", "Datecreated"})
    String dateCreated;
    @SerializedName(value = "datemodified", alternate = {"dateModified", "DateModified", "Datemodified"})
    String dateModified;
    @SerializedName(value = "dateposted", alternate = {"datePosted", "DatePosted", "Dateposted"})
    String datePosted;
    @SerializedName(value = "description", alternate = {"Description"})
    String description;
    @SerializedName(value = "articlebody", alternate = {"articleBody", "ArticleBody", "Articlebody"})
    String articleBody;
  }
  
  private class MetaTags {
    @SerializedName("og:site_name")
    String ogSiteName;
  }

  private class Organization {
    @SerializedName(value = "name", alternate = {"Name"})
    String name;
  }

  private class Thumbnail {
    String src;
  }
}