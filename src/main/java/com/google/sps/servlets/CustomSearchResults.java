package com.google.sps.servlets;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import com.joestelmach.natty.Parser;
import com.joestelmach.natty.DateGroup;

class CustomSearchResults {
  final List<Result> items;

  CustomSearchResults(List<Result> items) {
    this.items = Collections.unmodifiableList(items);
  }

  List<Article> getArticles() {
    List<Article> articles = new ArrayList<>();
    for (Result result : items) {
      try {
        articles.add(result.getArticle());
      } catch (NullPointerException e) {
        // Ignore this error, because we don't want the entire program 
        // to halt because one article failed to parse.
        // TODO add logging so that articles that fail to parse won't be missed.
        e.printStackTrace();
      }
    }
    return articles;
  }

  class Result {
    final String title;
    @SerializedName("pagemap")
    final PageMap pageMap; 
    final String snippet;
    final String link;

    Result(String title, PageMap pageMap, String snippet, String link) {
      this.title = title;
      this.pageMap = pageMap;
      this.snippet = snippet;
      this.link = link;
    }

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

      List<MetaTags> metaTags = pageMap.metaTags;
      if (metaTags != null && !metaTags.isEmpty()) {
        title = metaTags
          .get(0)
          .ogTitle;
      }

      List<NewsArticle> newsArticles = pageMap.newsArticles;
      if (title == null && newsArticles != null && !newsArticles.isEmpty()) {
        title = newsArticles
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
      List<MetaTags> metaTags = pageMap.metaTags;
      if (metaTags != null && !metaTags.isEmpty()) {
        publisher = metaTags
          .get(0)
          .ogSiteName;
      }

      List<Organization> organizations = pageMap.organizations;
      if (publisher == null && organizations != null && !organizations.isEmpty()) {
        publisher = organizations
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
        String[] potentialDates = {article.datePublished, article.datePosted,
         article.dateCreated, article.dateModified};
        for (String date : potentialDates) {
          formattedDate = date;
          if (formattedDate != null) {
            break;
          }
        }
      }

      List<MetaTags> metaTags = pageMap.metaTags;

      if (formattedDate == null && metaTags != null && !metaTags.isEmpty()) {
        MetaTags tags = metaTags.get(0);
        String[] potentialDates = {tags.articlePublishedTime, tags.dateToday,
         tags.articleModifiedtime, tags.lastModified, tags.ogUpdatedTime};
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
      
      List<MetaTags> metaTags = pageMap.metaTags;
      if (metaTags != null && !metaTags.isEmpty()) {
        description = metaTags
          .get(0)
          .ogDescription;
      }

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
      String url = link;

      List<MetaTags> metaTags = pageMap.metaTags;
      if (url == null && metaTags != null && !metaTags.isEmpty()) {
        url = metaTags
          .get(0)
          .ogUrl;
      }

      return url;
    }

    private String getThumbnailUrl() {
      String thumbnailUrl = null;
      List<MetaTags> metaTags = pageMap.metaTags;
      if (metaTags != null && !metaTags.isEmpty()) {
        thumbnailUrl = metaTags
          .get(0)
          .ogImage;
      }
      List<Thumbnail> cseImages = pageMap.cseImages;
      if (cseImages != null && !cseImages.isEmpty()) {
        thumbnailUrl = cseImages
          .get(0)
          .src;
      }
      List<Thumbnail> cseThumbnails = pageMap.cseThumbnails;
      if (cseThumbnails != null && !cseThumbnails.isEmpty()) {
        thumbnailUrl = cseThumbnails
          .get(0)
          .src;
      }
      return thumbnailUrl;
    }

    //TODO implement an actual location determination algorithm
    private Location getLocation() {
      return new Location("City", "State", "Country");
    }
  }

  class PageMap {
    @SerializedName(value = "newsarticle", alternate = {"newsArticle", "NewsArticle", "Newsarticle"})
    final List<NewsArticle> newsArticles;
    @SerializedName(value = "metatags", alternate = {"metaTags", "MetaTags", "Metatags"})
    final List<MetaTags> metaTags;
    @SerializedName(value = "organization", alternate = {"Organization"})
    final List<Organization> organizations;
    @SerializedName("cse_image")
    final List<Thumbnail> cseImages;
    @SerializedName("cse_thumbnail")
    final List<Thumbnail> cseThumbnails;

    PageMap(List<NewsArticle> newsArticles, List<MetaTags> metaTags, List<Organization> organizations, 
        List<Thumbnail> cseImages, List<Thumbnail> cseThumbnails) {
      this.newsArticles = Collections.unmodifiableList(newsArticles);
      this.metaTags = Collections.unmodifiableList(metaTags);
      this.organizations = Collections.unmodifiableList(organizations);
      this.cseImages = Collections.unmodifiableList(cseImages);
      this.cseThumbnails = Collections.unmodifiableList(cseThumbnails);
    }
  }

  class NewsArticle {
    @SerializedName(value = "headline", alternate = {"Headline"})
    final String headline;
    @SerializedName(value = "datepublished", alternate = {"datePublished", "DatePublished", "Datepublished"})
    final String datePublished;
    @SerializedName(value = "datecreated", alternate = {"dateCreated", "DateCreated", "Datecreated"})
    final String dateCreated;
    @SerializedName(value = "datemodified", alternate = {"dateModified", "DateModified", "Datemodified"})
    final String dateModified;
    @SerializedName(value = "dateposted", alternate = {"datePosted", "DatePosted", "Dateposted"})
    final String datePosted;
    @SerializedName(value = "description", alternate = {"Description"})
    final String description;
    @SerializedName(value = "articlebody", alternate = {"articleBody", "ArticleBody", "Articlebody"})
    final String articleBody;
    NewsArticle(String headline, String datePublished, String dateCreated, String dateModified,
        String datePosted, String description, String articleBody) {
      this.headline = headline;
      this.datePublished = datePublished;
      this.dateCreated = dateCreated;
      this.dateModified = dateModified;
      this.datePosted = datePosted;
      this.description = description;
      this.articleBody = articleBody;
    }
  }
  
  class MetaTags {
    @SerializedName("og:site_name")
    final String ogSiteName;
    @SerializedName("og:title")
    final String ogTitle;
    @SerializedName("og:description")
    final String ogDescription;
    @SerializedName("og:image")
    final String ogImage;
    @SerializedName("og:url")
    final String ogUrl;
    @SerializedName("og:updated_time")
    final String ogUpdatedTime;
    @SerializedName(value = "article:published_time", alternate = {"og:article:published_time"})
    final String articlePublishedTime;    
    @SerializedName(value = "article:modified_time", alternate = {"og:article:modified_time"})
    final String articleModifiedtime;
    @SerializedName("last-modified")
    final String lastModified;
    @SerializedName(value = "datetoday", alternate = {"dateToday", "DateToday", "Datetoday"})
    final String dateToday;

    MetaTags(String ogSiteName, String ogTitle, String ogDescription, String ogImage,
       String ogUrl, String ogUpdatedTime, String articlePublishedTime,
       String articleModifiedtime, String lastModified, String dateToday) {
      this.ogSiteName = ogSiteName;
      this.ogTitle = ogTitle;
      this.ogDescription = ogDescription;
      this.ogImage = ogImage;
      this.ogUrl = ogUrl;
      this.ogUpdatedTime = ogUpdatedTime;
      this.articlePublishedTime = articlePublishedTime;
      this.articleModifiedtime = articleModifiedtime;
      this.lastModified = lastModified;
      this.dateToday = dateToday;
    }
  }

  class Organization {
    @SerializedName(value = "name", alternate = {"Name"})
    String name;

    Organization(String name) {
      this.name = name;
    }
  }

  class Thumbnail {
    final String src;

    Thumbnail(String src) {
      this.src = src;
    }
  }
}
