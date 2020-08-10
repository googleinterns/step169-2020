package com.google.sps.servlets;

import java.util.List;

interface Requester {
  List<String> request(String region, String topic, int count);
}