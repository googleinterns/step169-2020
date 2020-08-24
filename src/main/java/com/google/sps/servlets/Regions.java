package com.google.sps.servlets;

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

public class Regions {

    final Map<String, Set<String>> countryToSubCountriesMap;
    final Map<String, Set<String>> countryPlusSubCountryToCitiesMap;
    final Map<String, Set<String>> subCountryToCitiesMap;
    final Map<String, Set<String>> cityToSubCountriesMap;
    final Map<String, Set<String>> subCountriesToCountryMap;
    final Map<String, Set<String>> cityToCountryMap;
    final int windowSize;
    private static Regions regions;

    private void addToMap(Map<String, Set<String>> map, String key, String item) {
        if (!map.containsKey(key)) {
            map.put(key, new HashSet<String>());
            map.get(key).add(item);
        } else {
            map.get(key).add(item);
        }
    }

    private Regions(ServletContext context, String sourcePath) {
        countryToSubCountriesMap = new HashMap<String, Set<String>>();
        countryPlusSubCountryToCitiesMap = new HashMap<String, Set<String>> ();
        subCountryToCitiesMap = new HashMap<String, Set<String>>();
        cityToSubCountriesMap = new HashMap<String, Set<String>>();
        subCountriesToCountryMap = new HashMap<String, Set<String>>();
        cityToCountryMap = new HashMap<String, Set<String>>();
        int tempWindowSize = 0;
        try {
            Scanner scanner = new Scanner(context.getResourceAsStream(sourcePath));
            scanner.useDelimiter("\n");
            boolean firstEntry = true;
            while (scanner.hasNext()) {
                List<String> contents = new ArrayList<String>();
                Scanner innerScanner = new Scanner(scanner.next());
                innerScanner.useDelimiter(",");
                while (innerScanner.hasNext()) {
                    String text = innerScanner.next();
                    List<String> words = TextNormalizer.getNormalizedWords(text);
                    if (words.size() > tempWindowSize) {
                        tempWindowSize = words.size();
                    }
                    String word = "";
                    for (int i = 0; i < words.size(); i++) {
                        word += words.get(i);
                        if (i != words.size() - 1) {
                            word += " ";
                        }
                    }
                    contents.add(word);
                }
                if (contents.size() == 4 && !firstEntry) {
                    addToMap(countryToSubCountriesMap, contents.get(1), contents.get(2));
                    addToMap(countryPlusSubCountryToCitiesMap, contents.get(1) + contents.get(2), contents.get(0));
                    addToMap(subCountryToCitiesMap, contents.get(2), contents.get(0));
                    addToMap(cityToSubCountriesMap, contents.get(0), contents.get(2));
                    addToMap(subCountriesToCountryMap, contents.get(2), contents.get(1));
                    addToMap(cityToCountryMap, contents.get(0), contents.get(1));
                }
                firstEntry = false;
            }
            //this.testCloud();
        } catch(Exception e) {
            System.err.println(e);
            //System.err.println("Failed to find ArticleLabeler source file at: " + sourcePath);
        }
        windowSize = tempWindowSize;
    }

    public static synchronized Regions getRegions(ServletContext context, String sourcePath) {
        if (Regions.regions == null) {
            Regions.regions = new Regions(context, sourcePath);
        }
        return Regions.regions;
    }


}