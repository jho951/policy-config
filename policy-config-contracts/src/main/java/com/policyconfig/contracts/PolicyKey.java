package com.policyconfig.contracts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 타입 안전한 정책 키.
 *
 * @param <T> 정책 값의 타입
 */
public final class PolicyKey<T> {

	private final String name;
	private final Class<T> type;
	private final T defaultValue;
	private final String description;
	private final boolean sensitive;
	private final List<String> aliases;
	private final List<PolicyValidator<T>> validators;

	/** 빌더로 생성합니다. */
	private PolicyKey(Builder<T> builder) {
		this.name = builder.buildName();
		this.type = builder.type;
		this.defaultValue = builder.defaultValue;
		this.description = builder.description;
		this.sensitive = builder.sensitive;
		this.aliases = builder.buildAliases();
		this.validators = builder.buildValidators();
	}

	/** 정책 이름을 반환합니다. */
	public String getName() {
		return name;
	}

	/** 정책 값의 타입을 반환합니다. */
	public Class<T> getType() {
		return type;
	}

	/** 기본값을 반환합니다. */
	public T getDefaultValue() {
		return defaultValue;
	}

	/** 설명을 반환합니다. */
	public String getDescription() {
		return description;
	}

	/** 민감한 값인지 여부를 반환합니다. */
	public boolean isSensitive() {
		return sensitive;
	}

	/** 별칭 목록을 반환합니다. */
	public List<String> getAliases() {
		return aliases;
	}

	/** 검증기 목록을 반환합니다. */
	public List<PolicyValidator<T>> getValidators() {
		return validators;
	}

	/** 새 {@code PolicyKey} 빌더를 시작합니다. */
	public static <T> Builder<T> builder(String name, Class<T> type) {
		return new Builder<>(name, type);
	}

	/** {@link PolicyKey} 빌더. */
	public static final class Builder<T> {
		private final String name;
		private final Class<T> type;
		private T defaultValue;
		private String description;
		private boolean sensitive;
		private String namespace;
		private final List<String> aliases = new ArrayList<>();
		private final List<PolicyValidator<? super T>> validators = new ArrayList<>();

		/** 필수 필드를 초기화합니다. */
		private Builder(String name, Class<T> type) {
			if (name == null || name.isBlank()) {
				throw new IllegalArgumentException("name must not be blank");
			}
			if (type == null) {
				throw new IllegalArgumentException("type must not be null");
			}
			this.name = name;
			this.type = type;
		}

		/** 기본값을 설정합니다. */
		public Builder<T> defaultValue(T defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		public Builder<T> description(String description) {
			this.description = description;
			return this;
		}

		public Builder<T> sensitive(boolean sensitive) {
			this.sensitive = sensitive;
			return this;
		}

		public Builder<T> namespace(String namespace) {
			this.namespace = namespace;
			return this;
		}

		public Builder<T> alias(String alias) {
			if (alias != null && !alias.isBlank()) {
				this.aliases.add(alias.trim());
			}
			return this;
		}

		public Builder<T> aliases(List<String> aliases) {
			if (aliases != null) {
				for (String alias : aliases) {
					alias(alias);
				}
			}
			return this;
		}

		public Builder<T> validator(PolicyValidator<? super T> validator) {
			this.validators.add(Objects.requireNonNull(validator, "validator"));
			return this;
		}

		public Builder<T> validators(List<PolicyValidator<? super T>> validators) {
			if (validators != null) {
				for (PolicyValidator<? super T> validator : validators) {
					validator(validator);
				}
			}
			return this;
		}

		/** 빌더를 완성합니다. */
		public PolicyKey<T> build() {
			return new PolicyKey<>(this);
		}

		private String buildName() {
			return qualify(namespace, name);
		}

		private List<String> buildAliases() {
			List<String> resolved = new ArrayList<>(aliases.size());
			for (String alias : aliases) {
				resolved.add(qualify(namespace, alias));
			}
			return Collections.unmodifiableList(resolved);
		}

		@SuppressWarnings("unchecked")
		private List<PolicyValidator<T>> buildValidators() {
			List<PolicyValidator<T>> resolved = new ArrayList<>(validators.size());
			for (PolicyValidator<? super T> validator : validators) {
				resolved.add((PolicyValidator<T>) validator);
			}
			return Collections.unmodifiableList(resolved);
		}

		private static String qualify(String namespace, String key) {
			if (namespace == null || namespace.isBlank()) {
				return key;
			}
			String normalized = namespace.trim();
			if (normalized.endsWith(".")) {
				normalized = normalized.substring(0, normalized.length() - 1);
			}
			if (key.startsWith(normalized + ".")) {
				return key;
			}
			return normalized + "." + key;
		}
	}
}
