package com.policyconfig.contracts;

import java.util.Objects;

/** {@link PolicyResolver#inspect(PolicyKey)} 결과. */
public final class PolicyResolution<T> {

	private final PolicyKey<T> key;
	private final T value;
	private final String rawValue;
	private final String matchedName;
	private final String sourceName;
	private final boolean present;
	private final boolean defaulted;

	private PolicyResolution(Builder<T> builder) {
		this.key = builder.key;
		this.value = builder.value;
		this.rawValue = builder.rawValue;
		this.matchedName = builder.matchedName;
		this.sourceName = builder.sourceName;
		this.present = builder.present;
		this.defaulted = builder.defaulted;
	}

	public PolicyKey<T> key() {
		return key;
	}

	public T value() {
		return value;
	}

	public String rawValue() {
		return rawValue;
	}

	public String matchedName() {
		return matchedName;
	}

	public String sourceName() {
		return sourceName;
	}

	public boolean present() {
		return present;
	}

	public boolean defaulted() {
		return defaulted;
	}

	public String displayValue() {
		if (key.isSensitive() && value != null) {
			return "***";
		}
		return value == null ? null : String.valueOf(value);
	}

	@Override
	public String toString() {
		return "PolicyResolution{"
			+ "key=" + key.getName()
			+ ", value=" + displayValue()
			+ ", rawValue=" + (key.isSensitive() ? "***" : rawValue)
			+ ", matchedName=" + matchedName
			+ ", sourceName=" + sourceName
			+ ", present=" + present
			+ ", defaulted=" + defaulted
			+ '}';
	}

	public static <T> Builder<T> builder(PolicyKey<T> key) {
		return new Builder<>(key);
	}

	public static final class Builder<T> {
		private final PolicyKey<T> key;
		private T value;
		private String rawValue;
		private String matchedName;
		private String sourceName;
		private boolean present;
		private boolean defaulted;

		private Builder(PolicyKey<T> key) {
			this.key = Objects.requireNonNull(key, "key");
		}

		public Builder<T> value(T value) {
			this.value = value;
			return this;
		}

		public Builder<T> rawValue(String rawValue) {
			this.rawValue = rawValue;
			return this;
		}

		public Builder<T> matchedName(String matchedName) {
			this.matchedName = matchedName;
			return this;
		}

		public Builder<T> sourceName(String sourceName) {
			this.sourceName = sourceName;
			return this;
		}

		public Builder<T> present(boolean present) {
			this.present = present;
			return this;
		}

		public Builder<T> defaulted(boolean defaulted) {
			this.defaulted = defaulted;
			return this;
		}

		public PolicyResolution<T> build() {
			return new PolicyResolution<>(this);
		}
	}
}
