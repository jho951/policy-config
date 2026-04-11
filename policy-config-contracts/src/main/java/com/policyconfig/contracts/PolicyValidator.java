package com.policyconfig.contracts;

@FunctionalInterface
public interface PolicyValidator<T> {

	void validate(String key, T value);
}
