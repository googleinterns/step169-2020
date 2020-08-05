package com.google.sps.servlets;

import java.util.List;

interface CustomSearchNewsParser {
  List<Article> parseSingleResult(String resultJson);
  List<Article> parseResults(List<String> resultJsons);
}