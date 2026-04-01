package com.policyconfig.api;

@FunctionalInterface
public interface PolicyValueConverter<T> {

	T convert(String key, String raw);
}
