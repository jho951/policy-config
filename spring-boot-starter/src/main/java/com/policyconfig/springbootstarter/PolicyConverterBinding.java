package com.policyconfig.springbootstarter;

import com.policyconfig.contracts.PolicyValueConverter;

import java.util.Objects;

/** Spring converter 바인딩. */
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
