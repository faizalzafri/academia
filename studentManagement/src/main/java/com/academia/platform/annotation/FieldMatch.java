package com.academia.platform.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import com.academia.platform.validator.FieldMatchValidator;


@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = FieldMatchValidator.class)
public @interface FieldMatch {
	String message() default "{constraints.field-match}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	String first();

	String second();

	@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface List {
		FieldMatch[] value();
	}
}
