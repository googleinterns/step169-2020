package com.google.hub.news;

import java.time.Instant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** */
@RunWith(JUnit4.class)
public final class ArticleTest {
  private static final String TITLE_1 = "Brian Stelter, CNN Business";
  private static final String PUBLISHER_1 = "CNN";
  private static final Instant DATE_1 = Instant.parse("2020-08-28T19:50:00Z");
  private static final String DESCRIPTION_1 = "President Trump's speech making the case for his"
  + "reelection was lower-rated than his challenger Joe Biden's speech one" 
  + "week ago, according to overnight Nielsen ratings.";
  private static final String URL_1 = "https://www.cnn.com/2020/08/28/media/ratings-trump-vs-biden-convention-speeches/index.html";
  private static final String THUMBNAIL_URL_1 = "https://cdn.cnn.com/cnnnext/dam/assets/200828124814-trump-biden-convention-speeches-split-super-tease.jpg";
  private static final Location LOCATION_1 = new Location("Washington",
   "DC", 
   "USA");
  private static final String THEME_1 = "politics";
  private static final Article ARTICLE_1 = new Article(TITLE_1, 
      PUBLISHER_1, DATE_1, DESCRIPTION_1, URL_1, THUMBNAIL_URL_1, LOCATION_1, THEME_1);
  
  @Test
  public void constructorSetsAppropriateFields() {
    Article article = new Article(TITLE_1, PUBLISHER_1, DATE_1, DESCRIPTION_1,
        URL_1, THUMBNAIL_URL_1, LOCATION_1, THEME_1);

    Assert.assertEquals(article.title, TITLE_1);
    Assert.assertEquals(article.publisher, PUBLISHER_1);
    Assert.assertEquals(article.date, DATE_1);
    Assert.assertEquals(article.description, DESCRIPTION_1);
    Assert.assertEquals(article.url, URL_1);
    Assert.assertEquals(article.thumbnailUrl, THUMBNAIL_URL_1);
    Assert.assertEquals(article.location, LOCATION_1);
    Assert.assertEquals(article.theme, THEME_1);
  }

  @Test
  public void equalsSelf() {
    Assert.assertEquals(ARTICLE_1, ARTICLE_1);
  }

  @Test
  public void equalsOther() {
    Article article2 = new Article(TITLE_1, 
      PUBLISHER_1, DATE_1, DESCRIPTION_1, URL_1, THUMBNAIL_URL_1, LOCATION_1, THEME_1); // Should be identical to ARTICLE_1
    Assert.assertEquals(ARTICLE_1, article2);
  }
}