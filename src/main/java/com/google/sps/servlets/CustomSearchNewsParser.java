package com.google.sps.servlets;

import java.util.List;

interface CustomSearchNewsParser {
  List<Article> parseResults(List<String> resultJsons);
}