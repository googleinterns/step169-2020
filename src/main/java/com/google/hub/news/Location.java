package com.google.hub.news;

public final class Location {
  public final String city;
  public final String subcountry;
  public final String country;

  public Location(String city, String subcountry, String country) {
    this.city = city;
    this.subcountry = subcountry;
    this.country = country;
  }
  
  @Override
  public String toString() {
    return String.format("%s, %s, %s", city, subcountry, country);
  }
}