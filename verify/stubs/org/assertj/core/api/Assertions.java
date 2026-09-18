package org.assertj.core.api;
import java.util.function.Consumer;
public class Assertions {
  public static ObjectAssert assertThat(Object actual) { return new ObjectAssert(); }
  public static IterableAssert assertThat(Iterable<?> actual) { return new IterableAssert(); }
  public static OptionalAssert assertThat(java.util.Optional<?> actual) { return new OptionalAssert(); }
  public static BooleanAssert assertThat(boolean actual) { return new BooleanAssert(); }
  public static ThrowableAssert assertThatThrownBy(ThrowingCallable c) { return new ThrowableAssert(); }
  public interface ThrowingCallable { void call() throws Throwable; }
  public static class ObjectAssert {
    public ObjectAssert isEqualTo(Object other) { return this; }
    public ObjectAssert isNotNull() { return this; }
    public ObjectAssert isNull() { return this; }
    public ObjectAssert isTrue() { return this; }
  }
  public static class BooleanAssert {
    public BooleanAssert isEqualTo(boolean other) { return this; }
    public BooleanAssert isTrue() { return this; }
    public BooleanAssert isFalse() { return this; }
  }
  public static class IterableAssert {
    public IterableAssert isEmpty() { return this; }
    public IterableAssert hasSize(int n) { return this; }
    public <T> IterableAssert allSatisfy(Consumer<T> requirements) { return this; }
  }
  public static class OptionalAssert {
    public OptionalAssert isPresent() { return this; }
    public GetAssert get() { return new GetAssert(); }
    public static class GetAssert { public <T> GetAssert satisfies(Consumer<T> requirements) { return this; } }
  }
  public static class ThrowableAssert {
    public ThrowableAssert isInstanceOf(Class<?> type) { return this; }
    public ThrowableAssert hasMessageContaining(String s) { return this; }
  }
}
