package org.mockito;
public class Mockito {
  public static <T> OngoingStubbing<T> when(T call) { return new OngoingStubbing<T>(); }
  public static <T> T verify(T mock) { return mock; }
  public static <T> T verify(T mock, Object mode) { return mock; }
  public static Object never() { return null; }
  public static class OngoingStubbing<T> {
    public OngoingStubbing<T> thenReturn(T value) { return this; }
    public OngoingStubbing<T> thenThrow(Throwable t) { return this; }
  }
}
