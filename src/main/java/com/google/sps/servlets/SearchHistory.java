package com.google.sps.servlets;

import java.time.Instant;

class SearchHistory {
  final long searchId;
  final String region;
  final String topic;
  final Instant timestamp;

  SearchHistory(long searchId, String region, String topic, Instant timestamp) {
    this.searchId = searchId;
    this.region = region;
    this.topic = topic;
    this.timestamp = timestamp;
  }

  @Override
  public String toString() {
    return String.format("{searchId: %d, region: %s, topic: %s, timestamp: %d}", 
        searchId, region, topic, timestamp.toEpochMilli());
  }
}