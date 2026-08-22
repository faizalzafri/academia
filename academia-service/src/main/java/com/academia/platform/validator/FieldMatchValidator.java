package com.academia.platform.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.apache.commons.beanutils.BeanUtils;

import com.academia.platform.annotation.FieldMatch;

public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {

	private String firstField;
	private String secondField;

	@Override
	public void initialize(FieldMatch constraintAnnotation) {
		this.firstField = constraintAnnotation.first();
		this.secondField = constraintAnnotation.second();
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		try {
			final Object firstObj = BeanUtils.getProperty(value, firstField);
			final Object secondObj = BeanUtils.getProperty(value, secondField);
			return firstObj == null && secondObj == null || firstObj != null && firstObj.equals(secondObj);
		} catch (final Exception ignore) {
		}
		return true;
	}
}
