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
  public boolean equals(Object other) {
    if (other instanceof Location) {
      Location otherLocation = (Location) other;
      return this.city.equals(otherLocation.city) 
          && this.subcountry.equals(otherLocation.subcountry) 
          && this.country.equals(otherLocation.country);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return String.format("%s, %s, %s", city, subcountry, country);
  }
}