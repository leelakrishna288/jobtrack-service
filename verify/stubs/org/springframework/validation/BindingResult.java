package org.springframework.validation;
import java.util.List;
public interface BindingResult { List<FieldError> getFieldErrors(); }
