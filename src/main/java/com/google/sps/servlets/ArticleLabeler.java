package com.google.sps;

import java.io.IOException;  
import org.jsoup.Jsoup;  
import org.jsoup.Connection;
import org.jsoup.nodes.Document; 
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;   
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import javax.servlet.ServletContext;
import java.util.Scanner;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.EntityMention;
import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.Document.Type;
import com.google.sps.servlets.Location;
import com.google.sps.servlets.ServletLogger;

public class ArticleLabeler {

  private Regions regions;

  public ArticleLabeler(ServletContext context, String sourcePath) {
    regions = Regions.getRegions(context, sourcePath);
  }

  public Location useCloudToFindLocation(String url) {
    Map<String, Integer> subCountryCounts = getNewSubCountryCounts();
    Map<String, Integer> cityCounts = getNewCityCounts();
    Map<String, Integer> countryCounts = getNewCountryCounts();
    Set<String> foundCities = new HashSet<String>();
    Set<String> foundSubCountries = new HashSet<String>();
    Set<String> foundCountries = new HashSet<String>();
    String text = parseArticle(url, cityCounts, subCountryCounts, countryCounts, foundCities, foundSubCountries, foundCountries);
    if (!text.equals("")) {
      if (addNLPFindings(text, cityCounts, subCountryCounts, countryCounts, foundCities, foundSubCountries, foundCountries)) {
        addCityCountsToSubCountryCounts(cityCounts, subCountryCounts, countryCounts, foundCities, foundSubCountries, foundCountries);
        addSubCountryCountsToCountryCounts(cityCounts, subCountryCounts, countryCounts, foundCities, foundSubCountries, foundCountries);
        Location location = getBestCandidate(cityCounts, subCountryCounts, countryCounts, foundCities, foundSubCountries, foundCountries);
        return location;
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  private String parseArticle(String url, Map<String, Integer> cityCounts, Map<String, Integer> subCountryCounts, Map<String, Integer> countryCounts, 
    Set<String> foundCities, Set<String> foundSubCountries, Set<String> foundCountries) {
    Map<String, Integer> typeWeight = new HashMap<String, Integer>();
    ServletLogger.logText("Start parseArticle() : " + url);
    try {
      String text = "";
      //ServletLogger.logText("Parse -- Check 1");
      Connection connection = Jsoup.connect(url);
      if (connection != null) {
        Document doc = connection.get();
        if (doc != null) {
          text += processDocumentTag(doc, "p", 1, cityCounts, subCountryCounts, countryCounts, foundCities, foundSubCountries, foundCountries);
          text += processDocumentTag(doc, "h1", 5, cityCounts, subCountryCounts, countryCounts, foundCities, foundSubCountries, foundCountries);
        }
      }
      ServletLogger.logText("Good End parseArticle() : " + url);
      return text;
    } catch(Exception e) {
      ServletLogger.logText("Bad End parseArticle() : " + url + " | Error : " + e.toString());
      return "";
    }
  }

  private String processDocumentTag(Document doc, String tag, int weight, Map<String, Integer> cityCounts, Map<String, Integer> subCountryCounts, Map<String, Integer> countryCounts, 
    Set<String> foundCities, Set<String> foundSubCountries, Set<String> foundCountries) {
    String text = "";
    //ServletLogger.logText("Parse Document Document -- Check 1");
    Elements elements = doc.select(tag);
    //ServletLogger.logText("Parse Document Document -- Check 2");
    for (Element e : elements) {
      //ServletLogger.logText("Parse Document -- Check 3");
      List<String> contents = TextNormalizer.getNormalizedWords(e.text());
      //ServletLogger.logText("Parse Document -- Check 4");
      for (int i = 0; i < contents.size(); i++) {
        //ServletLogger.logText("Parse Document -- Check 5");
        text += contents.get(i) + " ";
        //ServletLogger.logText("Parse Document -- Check 6");
        for (int size = 1; size <= regions.windowSize && i + size < contents.size(); size++) {
          //ServletLogger.logText("Parse Document -- Check 7");
          String searchTerm = "";
          for (int j = 0; j < size; j++) {
            //ServletLogger.logText("Parse Document -- Check 8");
            searchTerm += contents.get(i + j);
            //ServletLogger.logText("Parse Document -- Check 9");
            if (j < size - 1) {
              searchTerm += " ";
            }
            //ServletLogger.logText("Parse Document -- Check 10");
          }
          //ServletLogger.logText("Parse Document -- Check 11");
          if (countryCounts.containsKey(searchTerm)) {
            //ServletLogger.logText("Parse Document -- Check 12");
            countryCounts.put(searchTerm, countryCounts.get(searchTerm) + weight);
            //ServletLogger.logText("Parse Document -- Check 13");
            foundCountries.add(searchTerm);
            //ServletLogger.logText("Parse Document -- Check 14");
          }
          //ServletLogger.logText("Parse Document -- Check 15");
          if (subCountryCounts.containsKey(searchTerm)) {
            //ServletLogger.logText("Parse Document -- Check 16");
            subCountryCounts.put(searchTerm, subCountryCounts.get(searchTerm) + weight);
            //ServletLogger.logText("Parse Document -- Check 17");
            foundSubCountries.add(searchTerm);
          }
          //ServletLogger.logText("Parse Document -- Check 18");
          if (cityCounts.containsKey(searchTerm)) {
            //ServletLogger.logText("Parse Document -- Check 19");
            cityCounts.put(searchTerm, cityCounts.get(searchTerm) + weight);
            //foundCities.add(searchTerm);
            //ServletLogger.logText("Parse Document -- Check 20");
          }
        }
      }
    }
    //ServletLogger.logText("Parse Document -- Check 21");
    return text;
  }

  private boolean addNLPFindings(String text, Map<String, Integer> cityCounts, Map<String, Integer> subCountryCounts, Map<String, Integer> countryCounts, 
    Set<String> foundCities, Set<String> foundSubCountries, Set<String> foundCountries) {
    ServletLogger.logText("Start addNLPFindings()");
    try (LanguageServiceClient language = LanguageServiceClient.create()) {
      //ServletLogger.logText("NLP -- Check 1");
      com.google.cloud.language.v1.Document doc = com.google.cloud.language.v1.Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
      AnalyzeEntitiesRequest request =
      AnalyzeEntitiesRequest.newBuilder()
      .setDocument(doc)
      .setEncodingType(EncodingType.UTF16)
      .build();
      //ServletLogger.logText("NLP -- Check 2");
      AnalyzeEntitiesResponse response = language.analyzeEntities(request);
      //ServletLogger.logText("NLP -- Check 3");
      for (Entity entity : response.getEntitiesList()) {
        processEntity(entity, cityCounts, subCountryCounts, countryCounts, foundCities, foundSubCountries, foundCountries);
      }   
      ServletLogger.logText("Good End addNLPFindings()");
      return true;
    } catch (Exception e) { 
      ServletLogger.logText("Bad End addNLPFindings() | Error : " + e.toString());
      return false;
    }
  }

  private void processEntity(Entity entity, Map<String, Integer> cityCounts, Map<String, Integer> subCountryCounts, Map<String, Integer> countryCounts, 
    Set<String> foundCities, Set<String> foundSubCountries, Set<String> foundCountries) {
    //ServletLogger.logText("Process Entity -- Check 1");
    List<String> nameContents = TextNormalizer.getNormalizedWords(entity.getName());
    //ServletLogger.logText("Process Entity -- Check 2");
    String name = "";
    for (int i = 0; i < nameContents.size(); i++) {
        name += nameContents.get(i);
        if (i < nameContents.size() - 1) {
            name += " ";
        }
    }
    //ServletLogger.logText("Process Entity -- Check 3");
    for (EntityMention mention : entity.getMentionsList()) {
      //ServletLogger.logText("Process Entity -- Check 4");
      String type = mention.getType().toString();
      //ServletLogger.logText("Process Entity -- Check 5");
      if (type.equals("PROPER") || type.equals("LOCATION")) {
        //ServletLogger.logText("Process Entity -- Check 6");
        if (countryCounts.containsKey(name)) {
          //ServletLogger.logText("Process Entity -- Check 7");
          foundCountries.add(name);
          //ServletLogger.logText("Process Entity -- Check 8");
          countryCounts.put(name, countryCounts.get(name) + 20);
        }
        //ServletLogger.logText("Process Entity -- Check 9");
        if (subCountryCounts.containsKey(name)) {
          //ServletLogger.logText("Process Entity -- Check 10");
          foundSubCountries.add(name);
          //ServletLogger.logText("Process Entity -- Check 11");
          subCountryCounts.put(name, subCountryCounts.get(name) + 20);
        }
        //ServletLogger.logText("Process Entity -- Check 12");
        if (cityCounts.containsKey(name)) {
          //ServletLogger.logText("Process Entity -- Check 13");
          foundCities.add(name);
          //ServletLogger.logText("Process Entity -- Check 14");
          cityCounts.put(name, cityCounts.get(name) + 20);
        }
      }
    }
    //ServletLogger.logText("Process Entity -- Check 15");
  }

  private void addCityCountsToSubCountryCounts(Map<String, Integer> cityCounts, Map<String, Integer> subCountryCounts, Map<String, Integer> countryCounts, 
    Set<String> foundCities, Set<String> foundSubCountries, Set<String> foundCountries) {
    for (String city : foundCities) {
      for (String subCountry : regions.cityToSubCountriesMap.get(city)) {
        int multiplier = foundSubCountries.contains(subCountry) ? 3 : 1;
        foundSubCountries.add(subCountry);
        subCountryCounts.put(subCountry, (subCountryCounts.get(subCountry) + cityCounts.get(city)) * multiplier);
      }
    }
  }

  private void addSubCountryCountsToCountryCounts(Map<String, Integer> cityCounts, Map<String, Integer> subCountryCounts, Map<String, Integer> countryCounts, 
    Set<String> foundCities, Set<String> foundSubCountries, Set<String> foundCountries) {
    for (String subCountry : foundSubCountries) {
      for (String country : regions.subCountriesToCountryMap.get(subCountry)) {
        int multiplier = foundCountries.contains(country) ? 3 : 1;
        foundCountries.add(country);
        countryCounts.put(country, (countryCounts.get(country) + subCountryCounts.get(subCountry)) * multiplier);
      }
    }
  }

  private Location getBestCandidate(Map<String, Integer> cityCounts, Map<String, Integer> subCountryCounts, Map<String, Integer> countryCounts, 
    Set<String> foundCities, Set<String> foundSubCountries, Set<String> foundCountries) {
    ServletLogger.logText("Start getBestCandidate()");
    String[] bestCandidate = {"Unknown", "Unknown", "Unknown"};
    int highScore = -1;
    for (String country : foundCountries) {
      for (String subCountry : regions.countryToSubCountriesMap.get(country)) {
        for (String city : regions.countryPlusSubCountryToCitiesMap.get(country + subCountry)) {
          int score = cityCounts.get(city) + subCountryCounts.get(subCountry) + countryCounts.get(country);
          if (score > highScore) {
            //System.err.println(city + ", " + subCountry + ", " + country + " | Score " + score);
            bestCandidate[0] = city;
            bestCandidate[1] = subCountry;
            bestCandidate[2] = country;
            highScore = score;
          }
        }
      }
    }
    if (bestCandidate[0].equals("Unknown")) {
      ServletLogger.logText("End getBestCandidate() | Candidate Not Found");
      return null;
    } else {
      ServletLogger.logText("End getBestCandidate() | Candidate Found");
      return new Location(bestCandidate[0], bestCandidate[1], bestCandidate[2]);
    }
  }

  private Map<String, Integer> getNewCityCounts() {
    Map<String, Integer> cityCounts = new HashMap<String, Integer>();
    for (String city : regions.cityToCountryMap.keySet()) {
      cityCounts.put(city, 0);
    }
    return cityCounts;
  }

  private Map<String, Integer> getNewSubCountryCounts() {
    Map<String, Integer> subCountryCounts = new HashMap<String, Integer>();
    for (String subCountry : regions.subCountriesToCountryMap.keySet()) {
      subCountryCounts.put(subCountry, 0);
    }
    return subCountryCounts;
  }

  private Map<String, Integer> getNewCountryCounts() {
    Map<String, Integer> countryCounts = new HashMap<String, Integer>();
    for (String country : regions.countryToSubCountriesMap.keySet()) {
      countryCounts.put(country, 0);
    }
    return countryCounts;
  }

}