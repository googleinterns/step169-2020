package com.google.hub.servlets;

import com.google.hub.labeler.ArticleLabeler;
import com.google.hub.news.Location;
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

  private String testCloud(String url) {
        ArticleLabeler labeler = new ArticleLabeler(this.getServletContext(), "/WEB-INF/fullcitylist.csv");
        //ret += "-- BEGIN ARTICLE LABELER TESTS --\n-- LEFT SIDE IS CORRECT | RIGHT SIDE IS GUESS --\n";
        Location ret = labeler.useCloudToFindLocation(url);
        return ret.toString();
    }

}