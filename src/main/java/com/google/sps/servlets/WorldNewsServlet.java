package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import com.google.sps.ArticleLabeler;
import java.util.Scanner;
import java.util.ArrayList;

import com.google.gson.Gson;

@WebServlet("/world-news")
public class WorldNewsServlet extends HttpServlet {
  private final NewsService newsService;
  private final Gson gson;
  
  public WorldNewsServlet() {
    newsService = new MockNewsService();
    gson = new Gson();
  }

  private void runArticleLabelerTests(String sourcePath) {
    ArticleLabeler labeler = ArticleLabeler.getArticleLabeler(this.getServletContext(), "/WEB-INF/fullcitylist.csv");
    System.err.println("-- BEGIN ARTICLE LABELER TESTS --\n-- LEFT SIDE IS CORRECT | RIGHT SIDE IS GUESS --");
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
                        System.err.println("CITY : " + city + " | " + guess[0] + ", SUBCOUNTRY : " + subCountry + " | " + guess[1] + ", COUNTRY : " + country + " | " + guess[2] + " | SCORE : " + score +"/3" + " | URL: " + url);
                    }
                }
            }
        }
        System.err.println("SCORES : CITY " + cityScore +"/" + count + " | SUBCOUNTRY " + subCountryScore + "/" + count + " | COUNTRY " + countryScore + "/" + count);
    } catch (Exception e) {
        System.err.println(e);
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    this.runArticleLabelerTests("/WEB-INF/SearchTestCases.csv");

    response.setContentType("application/json;");

    List<Article> retrievedArticles = newsService.getWorldNews(-1);
    response.getWriter().println(gson.toJson(retrievedArticles));
  }
}