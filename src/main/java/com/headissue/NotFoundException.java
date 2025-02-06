package com.headissue;

/** @author Jens Wilke */
public class NotFoundException extends RuntimeException {

  public NotFoundException() {}

  public NotFoundException(Throwable cause) {
    super(cause);
  }
}
