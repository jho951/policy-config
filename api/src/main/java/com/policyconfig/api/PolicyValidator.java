package com.policyconfig.api;

@FunctionalInterface
public interface PolicyValidator<T> {

	void validate(String key, T value);
}
