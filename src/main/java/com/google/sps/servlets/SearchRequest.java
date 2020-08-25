package com.google.sps.servlets;

import java.time.Instant;

class SearchRequest {
  final String region;
  final String topic;
  final Instant timestamp;

  SearchRequest(String region, String topic, Instant timestamp) {
    this.region = region;
    this.topic = topic;
    this.timestamp = timestamp;
  }
}