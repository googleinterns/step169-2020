package com.google.sps.servlets;

interface UserManager {
  boolean userIsLoggedIn();

  String currentUserEmail();

  String currentUserId();

  String createLoginUrl(String redirectUrl);
  
  String createLogoutUrl(String redirectUrl);
}