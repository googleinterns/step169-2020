package com.google.hub.search;

import java.util.List;

public interface SearchHistoryService {
  public void addSearch(String userId, SearchRequest search);

  public List<SearchHistory> getSearches(String userId, int pageSize, int pageNum);

  public void deleteSearch(long searchId, String userId);

  public void deleteAllSearches(String userId);
}