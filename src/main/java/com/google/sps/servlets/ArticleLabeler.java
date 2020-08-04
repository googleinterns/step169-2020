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

public class ArticleLabeler {

    private Map<String, Set<String>> countryToSubCountriesMap;
    private Map<String, Set<String>> subCountryToCitiesMap;
    private Map<String, Set<String>> cityToSubCountriesMap;
    private Map<String, Set<String>> subCountriesToCountryMap;
    private Map<String, Set<String>> cityToCountryMap;
    private Map<String, Integer> subCountryCounts;
    private Map<String, Integer> cityCounts;
    private Map<String, Integer> countryCounts;
    private int windowSize;
    private String leaveOut = "\".'";
    private String splitTargets = "\n ";

    private static ArticleLabeler labeler = null;

    private void addToMap(Map<String, Set<String>> map, String key, String item) {
        if (!map.containsKey(key)) {
            map.put(key, new HashSet<String>());
            map.get(key).add(item);
        } else {
            map.get(key).add(item);
        }
    }

    private List<String> getNormalizedWords(String text) {
        text = text.toLowerCase();
        List<String> words = new ArrayList<String>();
        String word = "";
        for (char ch : text.toCharArray()) {
            if (!leaveOut.contains(ch + "")) {
                if (splitTargets.contains(ch + "")) {
                    if (!word.equals("")) {
                        words.add(word);
                    }
                    word = "";
                } else {
                    word += ch;
                }
            }
        }
        if (!word.equals("")) {
            words.add(word);
        }
        return words;
    }

    private String[] getBestCandidate(Set<String> foundCities, Set<String> foundSubCountries, Set<String> foundCountries) {
        // Added all cities found to their respective subcountry.
        for (String city : foundCities) {
            for (String subCountry : cityToSubCountriesMap.get(city)) {
                foundSubCountries.add(subCountry);
                int multiplier = foundSubCountries.contains(subCountry) ? 3 : 1;
                subCountryCounts.put(subCountry, (subCountryCounts.get(subCountry) * multiplier) + cityCounts.get(city));
            }
        }
        // Add all subcountries found to their respective countries.
        for (String subCountry : foundSubCountries) {
            for (String country : subCountriesToCountryMap.get(subCountry)) {
                foundCountries.add(country);
                int multiplier = foundCountries.contains(country) ? 3 : 1;
                countryCounts.put(country, (countryCounts.get(country) * multiplier) + subCountryCounts.get(subCountry));
            }
        }
        // Iterate through all possible combinations and find one with highest score.
        String[] bestCandidate = {"unknown", "unknown", "unknown"};
        int highScore = 0;
        for (String city : foundCities) {
            for (String subCountry : foundSubCountries) {
                for (String country : foundCountries) {
                    int score = cityCounts.get(city) + subCountryCounts.get(subCountry) + countryCounts.get(country);
                    if (score > highScore) {
                        bestCandidate[0] = city;
                        bestCandidate[1] = subCountry;
                        bestCandidate[2] = country;
                        highScore = score;
                    }
                }
            }
        }
        // Resting counts for next time.
        for (String city : foundCities) {
            cityCounts.put(city, 0);
        }
        for (String subCountry : foundSubCountries) {
            subCountryCounts.put(subCountry, 0);
        }
        for (String country : foundCountries) {
            countryCounts.put(country, 0);
        }
        return bestCandidate;
    }

    public synchronized String[] getMostLikelyLocation(String URL) {
        try {
            Document doc = Jsoup.connect(URL).get();
            Elements elements = doc.select("p");
            Set<String> foundCities = new HashSet<String>();
            Set<String> foundSubCountries = new HashSet<String>();
            Set<String> foundCountries = new HashSet<String>();
            for (Element e : elements) {
                List<String> contents = getNormalizedWords(e.text());
                for (int i = 0; i < contents.size(); i++) {
                    for (int size = 1; size <= windowSize; size++) {
                        if (i + size < contents.size()) {
                            String searchTerm = "";
                            for (int j = 0; j < size; j++) {
                                searchTerm += contents.get(i + j);
                                if (j < size - 1) {
                                    searchTerm += " ";
                                }
                            }
                            if (cityCounts.containsKey(searchTerm)) {
                                cityCounts.put(searchTerm, cityCounts.get(searchTerm) + 1);
                                foundCities.add(searchTerm);
                            }
                            if (subCountryCounts.containsKey(searchTerm)) {
                                subCountryCounts.put(searchTerm, subCountryCounts.get(searchTerm) + 1);
                                foundSubCountries.add(searchTerm);
                            }
                            if (countryCounts.containsKey(searchTerm)) {
                                countryCounts.put(searchTerm, countryCounts.get(searchTerm) + 1);
                                foundCountries.add(searchTerm);
                            }
                        }
                    }
                }
            }
            System.err.println(foundCities);
            System.err.println(foundSubCountries);
            System.err.println(foundCountries);
            return getBestCandidate(foundCities, foundSubCountries, foundCountries);
        } catch(Exception e) {
            System.err.println(e);
        }
        return null;
    }

    private ArticleLabeler(ServletContext context, String sourcePath) {
        countryToSubCountriesMap = new HashMap<String, Set<String>>();
        subCountryToCitiesMap = new HashMap<String, Set<String>>();
        cityToSubCountriesMap = new HashMap<String, Set<String>>();
        subCountriesToCountryMap = new HashMap<String, Set<String>>();
        cityToCountryMap = new HashMap<String, Set<String>>();
        cityCounts = new HashMap<String, Integer>();
        subCountryCounts = new HashMap<String, Integer>();
        countryCounts = new HashMap<String, Integer>();
        windowSize = 0;
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
                    List<String> words = getNormalizedWords(text);
                    if (words.size() > this.windowSize) {
                        this.windowSize = words.size();
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
                    cityCounts.put(contents.get(0), 0);
                    subCountryCounts.put(contents.get(2), 0);
                    countryCounts.put(contents.get(1), 0);
                    addToMap(countryToSubCountriesMap, contents.get(1), contents.get(2));
                    addToMap(subCountryToCitiesMap, contents.get(2), contents.get(0));
                    addToMap(cityToSubCountriesMap, contents.get(0), contents.get(2));
                    addToMap(subCountriesToCountryMap, contents.get(2), contents.get(1));
                    addToMap(cityToCountryMap, contents.get(0), contents.get(1));
                }
                firstEntry = false;
            }
            System.err.println("DONE | WINDOW SIZE : " + this.windowSize);
            this.testCloud();
        } catch(Exception e) {
            System.err.println(e);
            //System.err.println("Failed to find ArticleLabeler source file at: " + sourcePath);
        }
    }

    private void testCloud() {
        // Instantiate the Language client com.google.cloud.language.v1.LanguageServiceClient
        try (LanguageServiceClient language = LanguageServiceClient.create()) {
            String text = "After repeatedly seeking to discredit mail-in voting, President Donald Trump on Tuesday claimed Florida's election system is \"safe and secure\" and encourages Floridians to vote by mail. \"Whether you call it Vote by Mail or Absentee Voting, in Florida the election system is Safe and Secure, Tried and True. Florida's Voting system has been cleaned up (we defeated Democrats attempts at change), so in Florida I encourage all to request a Ballot & Vote by Mail! #MAGA,\" he tweeted. The President has recently begun laying the groundwork for the doubt and suspicion he could cast on election results if counting mail-in ballots -- which are expected to be more widely used as a result of the pandemic -- ultimately delays the declaration of a winner. He also floated delaying the election in a tweet last week that received bipartisan condemnation. The President does not have the power to do this -- Congress does.";

            com.google.cloud.language.v1.Document doc = com.google.cloud.language.v1.Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
            AnalyzeEntitiesRequest request =
            AnalyzeEntitiesRequest.newBuilder()
            .setDocument(doc)
            .setEncodingType(EncodingType.UTF16)
            .build();

            AnalyzeEntitiesResponse response = language.analyzeEntities(request);

            // Print the response
            for (Entity entity : response.getEntitiesList()) {
                System.out.printf("Entity: %s", entity.getName());
                System.out.printf("Salience: %.3f\n", entity.getSalience());
                System.out.println("Metadata: ");
                for (Map.Entry<String, String> entry : entity.getMetadataMap().entrySet()) {
                    System.out.printf("%s : %s", entry.getKey(), entry.getValue());
                }
                for (EntityMention mention : entity.getMentionsList()) {
                    System.out.printf("Begin offset: %d\n", mention.getText().getBeginOffset());
                    System.out.printf("Content: %s\n", mention.getText().getContent());
                    System.out.printf("Type: %s\n\n", mention.getType());
                }
            }   
        }
        catch (Exception e) { 
            System.err.println(e);
        }
    }

    public static synchronized ArticleLabeler getArticleLabeler(ServletContext context, String sourcePath) {
        if (ArticleLabeler.labeler == null) {
            ArticleLabeler.labeler = new ArticleLabeler(context, sourcePath);
        }
        return ArticleLabeler.labeler;
    }

}