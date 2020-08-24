package com.google.sps.servlets;

import java.lang.RuntimeException;

class SearchDeleteException extends RuntimeException {
  public SearchDeleteException(String message) {
    super(message);
  }

  public SearchDeleteException(String message, Throwable cause) {
    super(message, cause);
  }
}