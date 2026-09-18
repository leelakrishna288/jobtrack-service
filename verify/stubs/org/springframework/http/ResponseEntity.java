package org.springframework.http;
import java.net.URI;
public class ResponseEntity<T> {
  public static BodyBuilder created(URI location) { return new BodyBuilder(); }
  public static class BodyBuilder { public <T> ResponseEntity<T> body(T body) { return new ResponseEntity<T>(); } }
}
