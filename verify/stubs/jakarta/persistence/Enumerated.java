package jakarta.persistence;
public @interface Enumerated { EnumType value() default EnumType.ORDINAL; }
