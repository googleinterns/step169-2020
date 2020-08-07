package com.google.sps.servlets;

class NewsUnavailableException extends RuntimeException {

  public NewsUnavailableException(String message) {
    super(message);
  }

  public NewsUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}