package com.policyconfig.spring;

import com.policyconfig.api.PolicyValueConverter;

import java.util.Objects;

public final class PolicyConverterBinding<T> {

	private final Class<T> type;
	private final PolicyValueConverter<T> converter;

	private PolicyConverterBinding(Class<T> type, PolicyValueConverter<T> converter) {
		this.type = Objects.requireNonNull(type, "type");
		this.converter = Objects.requireNonNull(converter, "converter");
	}

	public static <T> PolicyConverterBinding<T> of(Class<T> type, PolicyValueConverter<T> converter) {
		return new PolicyConverterBinding<>(type, converter);
	}

	public Class<T> type() {
		return type;
	}

	public PolicyValueConverter<T> converter() {
		return converter;
	}
}
