package com.google.hub.search;

import java.lang.RuntimeException;

public class SearchDeleteException extends RuntimeException {
  public SearchDeleteException(String message) {
    super(message);
  }

  public SearchDeleteException(String message, Throwable cause) {
    super(message, cause);
  }
}