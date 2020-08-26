package com.google.hub.servlets;

import com.google.gson.Gson;
import com.google.hub.search.DatastoreSearchHistoryService;
import com.google.hub.search.SearchDeleteException;
import com.google.hub.search.SearchHistory;
import com.google.hub.search.SearchHistoryService;
import com.google.hub.users.UserManager;
import com.google.hub.users.UsersApiUserManager;
import java.io.IOException;
import java.util.Scanner;
import java.lang.NullPointerException;
import java.lang.NumberFormatException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/search-history")
public class SearchHistoryServlet extends HttpServlet { 
  private static final int PAGE_SIZE = 5;

  private final SearchHistoryService searchHistoryService;
  private final UserManager userManager;
  private final Gson gson;

  public SearchHistoryServlet() {
    searchHistoryService = new DatastoreSearchHistoryService();
    userManager = new UsersApiUserManager();
    gson = new Gson();
  }
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json; charset=utf-8");
    if (!userManager.userIsLoggedIn()) {
      response.setStatus(401);
      response.getWriter().write("User must be logged in to see search history");
      return;    
    } 

    String page = request.getParameter("page");
    int pageNumber = parsePageNumber(page);

    if (pageNumber < 0) {
      response.setStatus(400);
      response.getWriter().write("Request must contain a page parameter greater than or equal to 0.");
      return;
    }

    String userId = userManager.currentUserId();
    List<SearchHistory> searches = searchHistoryService.getSearches(userId, PAGE_SIZE, pageNumber);
    String responseJson = gson.toJson(searches);
    response.getWriter().write(responseJson);
  }

  private int parsePageNumber(String page) {
    try { 
      return Integer.parseInt(page);
    } catch (NumberFormatException e) {
      return -1;
    } catch (NullPointerException e) {
      return -1;
    }
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!userManager.userIsLoggedIn()) {
      response.setStatus(400);
      response.getWriter().write("User must be logged in to delete searches");
      return;
    }

    String userId = userManager.currentUserId();

    Map<String, String> bodyParams = null;
    try {
      bodyParams = getBodyParams(request);
    } catch (IOException e) {
      response.setStatus(400);
      response.getWriter().write("Could not read the request body");
      return;
    }

    String searchIdString = bodyParams.get("searchId");
    try {
      if (searchIdString != null) {
        long searchId = -1L;
        try {
          searchId = Long.parseLong(searchIdString);
        } catch (NumberFormatException e) {
          response.setStatus(400);
          String message = String.format("Could not parse searchId %s as an integer", searchIdString);
          response.getWriter().write(message);
          return;
        }
        searchHistoryService.deleteSearch(searchId, userId);
      } else {
        searchHistoryService.deleteAllSearches(userId);
      }
    } catch (SearchDeleteException e) {
      response.setStatus(400);
      response.getWriter().write("Could not delete searches");
      return;
    }
  }

  private String getBody(HttpServletRequest request) throws IOException {
    StringBuilder output = new StringBuilder();
    try (Scanner in = new Scanner(request.getReader())) {
      String line = null;
      while (in.hasNextLine()) {
        line = in.nextLine();
        output.append(line);
      }
    }
    return output.toString();
  }

  private Map<String, String> getBodyParams(HttpServletRequest request) throws IOException {
    String body = getBody(request);
    String[] params = body.split("&");
    Map<String, String> paramMap = new HashMap<>();
    for (String param : params) {
      String[] keyValuePair = param.split("=");
      if (keyValuePair.length > 1) {
        paramMap.put(keyValuePair[0], keyValuePair[1]);
      }
    } 
    return paramMap;
  }

  private long parseSearchId(String searchIdString) {
    try { 
      return Long.parseLong(searchIdString);
    } catch (NumberFormatException e) {
      return -1;
    } catch (NullPointerException e) {
      return -1;
    }
  }
}