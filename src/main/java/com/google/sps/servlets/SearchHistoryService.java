package com.google.sps.servlets;

import java.util.List;

interface SearchHistoryService {
  void addSearch(String userId, SearchRequest search);

  List<SearchHistory> getSearches(String userId, int pageSize, int pageNum);

  void deleteSearch(long searchId, String userId);

  void deleteAllSearches(String userId);
}