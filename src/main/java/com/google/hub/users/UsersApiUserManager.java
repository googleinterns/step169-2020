package com.google.hub.users; 

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class UsersApiUserManager implements UserManager {

  public boolean userIsLoggedIn() {
    UserService userService = UserServiceFactory.getUserService();
    return userService.isUserLoggedIn();
  }

  public String currentUserEmail() {
    UserService userService = UserServiceFactory.getUserService();
    return userService.getCurrentUser().getEmail();
  }
  
  public String currentUserId() {
    UserService userService = UserServiceFactory.getUserService();
    return userService.getCurrentUser().getUserId();
  }

  public String createLoginUrl(String redirectUrl) {
    UserService userService = UserServiceFactory.getUserService();
    return userService.createLoginURL(redirectUrl);
  }

  public String createLogoutUrl(String redirectUrl) {
    UserService userService = UserServiceFactory.getUserService();
    return userService.createLogoutURL(redirectUrl);
  }
}