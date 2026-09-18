package jakarta.persistence;
public @interface Column { String name() default ""; boolean nullable() default true; int length() default 255; boolean unique() default false; boolean updatable() default true; }
