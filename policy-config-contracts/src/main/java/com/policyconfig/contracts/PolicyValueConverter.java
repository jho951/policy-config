package com.policyconfig.contracts;

@FunctionalInterface
public interface PolicyValueConverter<T> {

	T convert(String key, String raw);
}
