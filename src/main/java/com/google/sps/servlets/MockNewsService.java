package com.google.sps.servlets;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.Instant;
import java.lang.IllegalArgumentException;

class MockNewsService implements NewsService {
  @Override
  public List<Article> getWorldNews(int count) {
    return new ArrayList<>();
  }

  @Override
  public List<Article> getRegionalNews(String region, String topic, int count) {
    if (region == null || region.isEmpty()) {
      throw new IllegalArgumentException("A region must be defined");
    }

    Article firstArticle = new Article(region + " " + topic + " 1",
     "First Provider",
      Instant.parse("2007-12-03T10:15:30.00Z"),
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque porttitor odio nisl, sit amet sollicitudin metus eleifend eu. Praesent quis mauris sollicitudin, pretium nisl at, commodo lacus. In hac habitasse platea dictumst. ",
      "https://example.com/sample1",
      "https://example.com/samplethumbnail1",
      "New York,NY,USA");
    Article secondArticle = new Article(region + " " + topic + " 2",
     "Second Provider",
      Instant.parse("2007-12-03T10:15:30.00Z"),
      "Etiam vitae augue eu felis euismod tempor id sed purus. Sed ut elit sollicitudin, rhoncus dolor eu, pellentesque nisi. Sed ultricies semper neque non accumsan. Ut interdum turpis purus, at tempor neque rhoncus quis.",
      "https://example.com/sample2",
      "https://example.com/samplethumbnail2",
      "New York,NY,USA");
    Article thirdArticle = new Article(region + " " + topic + " 3",
     "Third Provider",
      Instant.parse("2007-12-03T10:15:30.00Z"),
      "Proin sodales lacinia augue, non posuere purus suscipit vitae. Aliquam ultrices metus in magna pretium, ac vehicula nunc ultrices.",
      "https://example.com/sample3",
      "https://example.com/samplethumbnail3",
      "New York,NY,USA");
    return Arrays.asList(firstArticle, secondArticle, thirdArticle);
  }
}