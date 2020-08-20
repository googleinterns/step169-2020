package com.google.sps;

import java.io.IOException;  
import org.jsoup.Jsoup;  
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
        Document doc = Jsoup.connect(url).get();
        typeWeight.put("p", 1);
        typeWeight.put("h1", 5);
        for (String type : typeWeight.keySet()) {
          Elements elements = doc.select(type);
          for (Element e : elements) {
            List<String> contents = TextNormalizer.getNormalizedWords(e.text());
            for (int i = 0; i < contents.size(); i++) {
              text += contents.get(i) + " ";
              for (int size = 1; size <= regions.windowSize; size++) {
                if (i + size < contents.size()) {
                  String searchTerm = "";
                  for (int j = 0; j < size; j++) {
                    searchTerm += contents.get(i + j);
                    if (j < size - 1) {
                      searchTerm += " ";
                    }
                  }
                  if (countryCounts.containsKey(searchTerm)) {
                    countryCounts.put(searchTerm, countryCounts.get(searchTerm) + typeWeight.get(type));
                    foundCountries.add(searchTerm);
                  }
                  if (subCountryCounts.containsKey(searchTerm)) {
                    subCountryCounts.put(searchTerm, subCountryCounts.get(searchTerm) + typeWeight.get(type));
                    foundSubCountries.add(searchTerm);
                  }
                  if (cityCounts.containsKey(searchTerm)) {
                    cityCounts.put(searchTerm, cityCounts.get(searchTerm) + typeWeight.get(type));
                    //foundCities.add(searchTerm);
                  }
                }
              }
            }
          }
        }
        ServletLogger.logText("Good End parseArticle() : " + url);
        return text;
      } catch(Exception e) {
        ServletLogger.logText("Bad End parseArticle() : " + url + " | Error : " + e.toString());
        return "";
      }
    }

    private boolean addNLPFindings(String text, Map<String, Integer> cityCounts, Map<String, Integer> subCountryCounts, Map<String, Integer> countryCounts, 
      Set<String> foundCities, Set<String> foundSubCountries, Set<String> foundCountries) {
      ServletLogger.logText("Start addNLPFindings()");
      try (LanguageServiceClient language = LanguageServiceClient.create()) {

          com.google.cloud.language.v1.Document doc = com.google.cloud.language.v1.Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
          AnalyzeEntitiesRequest request =
          AnalyzeEntitiesRequest.newBuilder()
          .setDocument(doc)
          .setEncodingType(EncodingType.UTF16)
          .build();

          AnalyzeEntitiesResponse response = language.analyzeEntities(request);

          for (Entity entity : response.getEntitiesList()) {
              List<String> nameContents = TextNormalizer.getNormalizedWords(entity.getName());
              String name = "";
              for (int i = 0; i < nameContents.size(); i++) {
                  name += nameContents.get(i);
                  if (i < nameContents.size() - 1) {
                      name += " ";
                  }
              }
              for (EntityMention mention : entity.getMentionsList()) {
                  if (mention.getType().toString().equals("PROPER") || mention.getType().toString().equals("LOCATION")) {
                      if (countryCounts.containsKey(name)) {
                          foundCountries.add(name);
                          countryCounts.put(name, countryCounts.get(name) + 20);
                      }
                      if (subCountryCounts.containsKey(name)) {
                          foundSubCountries.add(name);
                          subCountryCounts.put(name, subCountryCounts.get(name) + 20);
                      }
                      if (cityCounts.containsKey(name)) {
                          foundCities.add(name);
                          cityCounts.put(name, cityCounts.get(name) + 20);
                      }
                  }
              }
          }   
          ServletLogger.logText("Good End addNLPFindings()");
          return true;
      } catch (Exception e) { 
          ServletLogger.logText("Bad End addNLPFindings() | Error : " + e.toString());
          return false;
      }
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