package com.google.hub.search;

import java.time.Instant;

public class SearchRequest {
  final String region;
  final String topic;
  final Instant timestamp;

  public SearchRequest(String region, String topic, Instant timestamp) {
    this.region = region;
    this.topic = topic;
    this.timestamp = timestamp;
  }
}