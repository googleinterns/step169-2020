package com.google.sps.servlets;

public class Location {
  final String city;
  final String subcountry;
  final String country;

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