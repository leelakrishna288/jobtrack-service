package org.junit.jupiter.params.provider;
public @interface EnumSource {
  enum Mode { INCLUDE, EXCLUDE }
  Class<? extends Enum<?>> value();
  String[] names() default {};
  Mode mode() default Mode.INCLUDE;
}
