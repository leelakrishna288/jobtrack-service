package org.springframework.http;
public class ProblemDetail {
  public static ProblemDetail forStatusAndDetail(HttpStatus status, String detail) { return new ProblemDetail(); }
  public void setTitle(String title) {}
  public void setProperty(String name, Object value) {}
}
