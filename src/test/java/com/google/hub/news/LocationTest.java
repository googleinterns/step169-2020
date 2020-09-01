package com.google.hub.news;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** */
@RunWith(JUnit4.class)
public final class LocationTest {
  private static final String CITY_1 = "Sunnyvale";
  private static final String SUBCOUNTRY_1 = "California";
  private static final String COUNTRY_1 = "USA";
  private static final Location LOCATION_1 = new Location(CITY_1, 
      SUBCOUNTRY_1, 
      COUNTRY_1);
  private static final String CITY_2 = "Toronto";
  private static final String SUBCOUNTRY_2 = "Ontario";
  private static final String COUNTRY_2 = "Canada";

  @Test
  public void constructorSetsAppropriateFields() {
    Location location = new Location(CITY_1, SUBCOUNTRY_1, COUNTRY_1);

    Assert.assertEquals(location.city, CITY_1);
    Assert.assertEquals(location.subcountry, SUBCOUNTRY_1);
    Assert.assertEquals(location.country, COUNTRY_1);
  }

  @Test
  public void equalsSelf() {
    Assert.assertEquals(LOCATION_1, LOCATION_1);
  }

  @Test
  public void equalsOther() {
    Location location2 = new Location(CITY_1, 
      SUBCOUNTRY_1, 
      COUNTRY_1); // Should be identical to LOCATION_1
    Assert.assertEquals(LOCATION_1, location2);
  }

  @Test
  public void unequalCity() {
    Location location2 = new Location(CITY_2, 
      SUBCOUNTRY_1, 
      COUNTRY_1); // Should be identical to LOCATION_1 besides city
    Assert.assertNotEquals(LOCATION_1, location2);
  }

  @Test
  public void unequalSubcountry() {
    Location location2 = new Location(CITY_1, 
      SUBCOUNTRY_2, 
      COUNTRY_1); // Should be identical to LOCATION_1 besides subcountry
    Assert.assertNotEquals(LOCATION_1, location2);
  }

  @Test
  public void unequalCountry() {
    Location location2 = new Location(CITY_1, 
      SUBCOUNTRY_1, 
      COUNTRY_2); // Should be identical to LOCATION_1 besides country
    Assert.assertNotEquals(LOCATION_1, location2);
  }
}