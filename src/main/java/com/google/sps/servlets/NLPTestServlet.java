package com.google.sps.servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.EntityMention;
import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.Document.Type;
import com.google.gson.Gson;
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
import com.google.sps.ArticleLabeler;

@WebServlet("/nlp-test")
public class NLPTestServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/text;");
    String url = request.getParameter("url");
    String testResults = "";
    //testResults += runArticleLabelerTests("/WEB-INF/SearchTestCases.csv");
    //testResults += testCloud("/WEB-INF/SearchTestCases.csv");
    testResults += testCloud(url);
    response.getWriter().println(testResults);
  }

  private String runArticleLabelerTests(String sourcePath) {
    ArticleLabeler labeler = ArticleLabeler.getArticleLabeler(this.getServletContext(), "/WEB-INF/fullcitylist.csv");
    String ret = "";
    ret += "-- BEGIN ARTICLE LABELER TESTS --\n-- LEFT SIDE IS CORRECT | RIGHT SIDE IS GUESS --\n";
    try {
        Scanner scanner = new Scanner(this.getServletContext().getResourceAsStream(sourcePath));
        scanner.useDelimiter("\n");
        int cityScore = 0;
        int subCountryScore = 0;
        int countryScore = 0;
        int count = 0;
        while (scanner.hasNext()) {
            List<String> contents = new ArrayList<String>();
            Scanner innerScanner = new Scanner(scanner.next());
            innerScanner.useDelimiter(",");
            while (innerScanner.hasNext()) {
                String text = innerScanner.next();
                contents.add(text);
            }

            if (contents.size() == 4) {
                String city = contents.get(0);
                String subCountry = contents.get(1);
                String country = contents.get(2);
                String url = contents.get(3);
                String[] guess = labeler.getMostLikelyLocation(url);
                if (guess != null) {
                    count += 1;
                    int ciScore = city.equals(guess[0]) ? 1 : 0;
                    int scScore = subCountry.equals(guess[1]) ? 1 : 0;
                    int coScore = country.equals(guess[2]) ? 1 : 0;
                    int score = ciScore + scScore + coScore;
                    cityScore += ciScore;
                    subCountryScore += scScore;
                    countryScore += coScore;
                    if (guess.length == 3) {
                        ret += "CITY : " + city + " | " + guess[0] + ", SUBCOUNTRY : " + subCountry + " | " + guess[1] + ", COUNTRY : " + country + " | " + guess[2] + " | SCORE : " + score +"/3" + " | URL: " + url + "\n";
                    }
                    break;
                }
            }
        }
        ret += "SCORES : CITY " + cityScore +"/" + count + " | SUBCOUNTRY " + subCountryScore + "/" + count + " | COUNTRY " + countryScore + "/" + count + "\n";
    } catch (Exception e) {
        ret += e.toString();
    }
    return ret;
  }

  private String testCloud(String url) {
        ArticleLabeler labeler = ArticleLabeler.getArticleLabeler(this.getServletContext(), "/WEB-INF/fullcitylist.csv");
        String ret = "";
        //ret += "-- BEGIN ARTICLE LABELER TESTS --\n-- LEFT SIDE IS CORRECT | RIGHT SIDE IS GUESS --\n";
        String[] ret1 = labeler.useCloudToFindLocation(url);
        for (int i = 0; i < ret1.length; i++) {
            ret += ret1[i];
            if (i < ret1.length - 1) {
                ret += ",";
            }
        }
        String[] ret2 = labeler.getMostLikelyLocation(url);
        ret += " | ";
        for (int i = 0; i < ret2.length; i++) {
            ret += ret2[i];
            if (i < ret2.length - 1) {
                ret += ",";
            }
        }
        return ret;
    }

}