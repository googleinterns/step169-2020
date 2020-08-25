package com.google.sps.servlets; 

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

class UsersApiUserManager implements UserManager {

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