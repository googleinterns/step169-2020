package com.google.sps.servlets;

import java.lang.IllegalArgumentException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class MockNewsService implements NewsService {
  @Override
  public List<Article> getWorldNews(int count) {
    Location[] cities = {
      new Location("Mountain View", "CA", "USA"), 
      new Location("Los Angeles", "CA", "USA"),
      new Location("Chicago", "IL", "USA"),
      new Location("New York", "NY", "USA"),
      new Location("Austin", "TX", "USA"),
      new Location("Boston", "MA", "USA"),

      new Location("Toronto", "Ontario", "Canada"),
      new Location("Tianchang", "Anhui", "China"),
      new Location("Longnan", "Gansu", "China"),
      new Location("Manchester", "Manchester", "England"),
      new Location("Quebec City", "Quebec", "Canada"),

      new Location("Mexico City", "Mexico City", "Mexico"),
      new Location("Moscow", "Central", "Russia"),
      new Location("Barcelona", "Catalonia", "Spain"),
      new Location("Lagos", "Lagos State", "Nigeria"),
      new Location("Sydney", "Sydney", "Australia")
    //   new Location("Boston", "MA", "USA"),
    //   new Location("Los Angeles", "CA", "USA"),
    //   new Location("Chicago", "IL", "USA"),
    //   new Location("New York", "NY", "USA"),
    //   new Location("Austin", "TX", "USA"),
    //   new Location("Boston", "MA", "USA")
    };
    String[] themes = {"sports","sports","sports","business","business","business","business","politics","politics","politics","politics","politics","politics","miscellaneous","miscellaneous","miscellaneous"};
    int[] counts = {6, 2, 1, 10, 3, 7, 8, 9, 4, 11, 15, 13, 21, 20, 18, 19};
    List<Article> articles = new ArrayList<>();
    for (int i = 0; i < cities.length; i++) {
      for (int j = 0; j < counts[i]; j++) {
        Article article = new Article(cities[i].toString() + " " + (j + 1),
            "First Provider",
            Instant.parse("2007-12-03T10:15:30.00Z"),
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque porttitor odio nisl, sit amet sollicitudin metus eleifend eu. Praesent quis mauris sollicitudin, pretium nisl at, commodo lacus. In hac habitasse platea dictumst. ",
            "https://example.com/sample1",
            "https://interactive-examples.mdn.mozilla.net/media/examples/grapefruit-slice-332-332.jpg",
<<<<<<< HEAD
            cities[i],
            themes[i]);
            articles.add(article);
        }
=======
            cities[i]);
        articles.add(article);
      }
>>>>>>> ab776251032fdce4164211a9d0978da31a5456a0
    }
    Collections.shuffle(articles);
    return articles;
  }

  @Override
  public List<Article> getRegionalNews(String region, String topic, int count) {
    if (region == null || region.isEmpty()) {
      throw new IllegalArgumentException("A region must be defined");
    }

    Article firstArticle = new Article(region + " " + topic + " 1",
<<<<<<< HEAD
     "First Provider",
      Instant.parse("2007-12-03T10:15:30.00Z"),
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque porttitor odio nisl, sit amet sollicitudin metus eleifend eu. Praesent quis mauris sollicitudin, pretium nisl at, commodo lacus. In hac habitasse platea dictumst. ",
      "https://example.com/sample1",
      "https://interactive-examples.mdn.mozilla.net/media/examples/grapefruit-slice-332-332.jpg",
      new Location("New York", "NY", "USA"),
      "miscellaneous");
    Article secondArticle = new Article(region + " " + topic + " 2",
     "Second Provider",
      Instant.parse("2007-12-03T10:15:30.00Z"),
      "Etiam vitae augue eu felis euismod tempor id sed purus. Sed ut elit sollicitudin, rhoncus dolor eu, pellentesque nisi. Sed ultricies semper neque non accumsan. Ut interdum turpis purus, at tempor neque rhoncus quis.",
      "https://example.com/sample2",
      "https://interactive-examples.mdn.mozilla.net/media/examples/grapefruit-slice-332-332.jpg",
      new Location("New York", "NY", "USA"),
      "miscellaneous");
    Article thirdArticle = new Article(region + " " + topic + " 3",
     "Third Provider",
      Instant.parse("2007-12-03T10:15:30.00Z"),
      "Proin sodales lacinia augue, non posuere purus suscipit vitae. Aliquam ultrices metus in magna pretium, ac vehicula nunc ultrices.",
      "https://example.com/sample3",
      "https://interactive-examples.mdn.mozilla.net/media/examples/grapefruit-slice-332-332.jpg",
      new Location("New York", "NY", "USA"),
      "miscellaneous");
=======
        "First Provider",
        Instant.parse("2007-12-03T10:15:30.00Z"),
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque porttitor odio nisl, sit amet sollicitudin metus eleifend eu. Praesent quis mauris sollicitudin, pretium nisl at, commodo lacus. In hac habitasse platea dictumst. ",
        "https://example.com/sample1",
        "https://interactive-examples.mdn.mozilla.net/media/examples/grapefruit-slice-332-332.jpg",
        new Location("New York", "NY", "USA"));
    Article secondArticle = new Article(region + " " + topic + " 2",
        "Second Provider",
        Instant.parse("2007-12-03T10:15:30.00Z"),
        "Etiam vitae augue eu felis euismod tempor id sed purus. Sed ut elit sollicitudin, rhoncus dolor eu, pellentesque nisi. Sed ultricies semper neque non accumsan. Ut interdum turpis purus, at tempor neque rhoncus quis.",
        "https://example.com/sample2",
        "https://interactive-examples.mdn.mozilla.net/media/examples/grapefruit-slice-332-332.jpg",
        new Location("New York", "NY", "USA"));
    Article thirdArticle = new Article(region + " " + topic + " 3",
        "Third Provider",
        Instant.parse("2007-12-03T10:15:30.00Z"),
        "Proin sodales lacinia augue, non posuere purus suscipit vitae. Aliquam ultrices metus in magna pretium, ac vehicula nunc ultrices.",
        "https://example.com/sample3",
        "https://interactive-examples.mdn.mozilla.net/media/examples/grapefruit-slice-332-332.jpg",
        new Location("New York", "NY", "USA"));
>>>>>>> ab776251032fdce4164211a9d0978da31a5456a0
    return Arrays.asList(firstArticle, secondArticle, thirdArticle);
  }
}