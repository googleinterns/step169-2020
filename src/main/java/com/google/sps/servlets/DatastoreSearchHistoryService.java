package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class DatastoreSearchHistoryService implements SearchHistoryService {

  private static final String SEARCH_KIND_NAME = "Search";
  private static final FetchOptions DEFAULT_OPTIONS = FetchOptions.Builder.withDefaults();

  @Override
  public void addSearch(String userId, SearchRequest search) {
    long timestamp = System.currentTimeMillis();
    Entity searchEntity = toEntity(userId, search);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(searchEntity);
  }

  private Entity toEntity(String userId, SearchRequest search) {
    Entity searchEntity = new Entity(SEARCH_KIND_NAME);
    searchEntity.setProperty("userId", userId);
    searchEntity.setProperty("region", search.region);
    searchEntity.setProperty("topic", search.topic);
    searchEntity.setProperty("timestamp", search.timestamp.toEpochMilli());
    return searchEntity;
  } 

  private Query buildQuery(String userId, boolean isKeysOnly) {
    Query.Filter userIdFilter = new Query.FilterPredicate(
        "userId",
        Query.FilterOperator.EQUAL,
        userId
    );
    Query query = new Query("Search")
        .addSort("timestamp", SortDirection.DESCENDING)
        .setFilter(userIdFilter);

    if (isKeysOnly) {
      query.setKeysOnly();
    }

    return query;
  }

  private PreparedQuery buildPreparedQuery(Query query) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    return results;
  }

  @Override
  public List<SearchHistory> getSearches(String userId, int pageSize, int pageNum) {
    Query searchQuery =  buildQuery(userId, false);
    PreparedQuery preparedQuery = buildPreparedQuery(searchQuery);
    int offset = pageNum * pageSize;
    FetchOptions options = FetchOptions.Builder
        .withLimit(pageSize)
        .offset(offset);
    return fetchSearchesWithOptions(preparedQuery, options);
  }

  private List<SearchHistory> fetchSearchesWithOptions(PreparedQuery preparedQuery,
       FetchOptions options) {
    return preparedQuery.asList(options)
        .stream()
        .map(e -> new SearchHistory(
            e.getKey().getId(),
            (String) e.getProperty("region"), 
            (String) e.getProperty("topic"), 
            Instant.ofEpochMilli((Long) e.getProperty("timestamp"))))
        .collect(Collectors.toList());
  }

  @Override
  public void deleteSearch(long searchId, String userId) {
    Key searchKey = KeyFactory.createKey(SEARCH_KIND_NAME, searchId);

    try {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Entity searchEntity = datastore.get(searchKey);

      String fetchedUserId = (String) searchEntity.getProperty("userId");
      if (fetchedUserId.equals(userId)) {
        datastore.delete(searchKey);
      } else {
        String message = String.format("Requesting user did not make search %d", 
            searchId);
        throw new SearchDeleteException(message);
      }
    } catch (EntityNotFoundException e) {
      String message = String.format("Could not find search with id %d", 
          searchId);
      throw new SearchDeleteException(message, e);
    }
  }

  @Override
  public void deleteAllSearches(String userId) {
    Query keyQuery =  buildQuery(userId, true);
    PreparedQuery preparedQuery = buildPreparedQuery(keyQuery);

    List<Key> searchKeys = preparedQuery.asList(DEFAULT_OPTIONS)
        .stream()
        .map(e -> e.getKey())
        .collect(Collectors.toList());

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.delete(searchKeys);
  }
}