// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.hub.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.hub.users.UserManager;
import com.google.hub.users.UsersApiUserManager;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Servlet which logs the user out of their google account
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
  private static final String INDEX_URL = "/";

  private final UserManager userManager;

  public LogoutServlet() {
    userManager = new UsersApiUserManager();
  }

  // Redirect to logout link if user is logged in
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    
    if (!userManager.userIsLoggedIn()) {
      // Redirect back to the HTML page.
      response.sendRedirect(INDEX_URL);
    } else {
      // Return logout link
      String logoutUrl = userManager.createLogoutUrl(INDEX_URL);
      out.println(logoutUrl);
    }
  }

}
