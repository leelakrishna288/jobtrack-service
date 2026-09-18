package org.springframework.data.domain;
import java.util.function.Function;
public interface Page<T> { <U> Page<U> map(Function<? super T, ? extends U> converter); }
