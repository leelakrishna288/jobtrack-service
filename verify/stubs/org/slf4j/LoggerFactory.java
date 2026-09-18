package org.slf4j;
public class LoggerFactory {
  public static Logger getLogger(Class<?> c) {
    return new Logger() {
      public void info(String msg, Object... args) {}
      public void warn(String msg, Object... args) {}
      public void error(String msg, Object... args) {}
    };
  }
}
