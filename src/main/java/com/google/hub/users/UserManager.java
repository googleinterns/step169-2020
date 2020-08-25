package com.google.hub.users;

public interface UserManager {
  public boolean userIsLoggedIn();

  public String currentUserEmail();

  public String currentUserId();

  public String createLoginUrl(String redirectUrl);
  
  public String createLogoutUrl(String redirectUrl);
}