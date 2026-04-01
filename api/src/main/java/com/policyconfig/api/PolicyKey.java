package com.policyconfig.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 하나의 정책 설정 키를 표현하는 불변(Immutable) 값 객체입니다.
 * 이 클래스는 정책의 이름, 데이터 타입 및 기본값을 캡슐화하며,
 * 제네릭 타입 {@code T}를 통해 타입 안전한 정책 참조를 보장합니다.
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

	/**
	 * 빌더를 통해 {@code PolicyKey} 인스턴스를 생성합니다.
	 *
	 * @param builder 정책 키 설정을 포함하는 빌더 객체
	 */
	private PolicyKey(Builder<T> builder) {
		this.name = builder.buildName();
		this.type = builder.type;
		this.defaultValue = builder.defaultValue;
		this.description = builder.description;
		this.sensitive = builder.sensitive;
		this.aliases = builder.buildAliases();
		this.validators = builder.buildValidators();
	}

	/**
	 * 정책의 고유 이름을 반환합니다. (예: "filestorage.maxSizeBytes")
	 *
	 * @return 정책 이름
	 */
	public String getName() {
		return name;
	}

	/**
	 * 정책 값의 기대 타입을 반환합니다.
	 *
	 * @return 값의 {@link Class} 타입
	 */
	public Class<T> getType() {
		return type;
	}

	/**
	 * 정책 설정값이 존재하지 않을 경우 사용할 기본값을 반환합니다.
	 *
	 * @return 기본값 (설정되지 않은 경우 {@code null})
	 */
	public T getDefaultValue() {
		return defaultValue;
	}

	/**
	 * 정책에 대한 사람이 읽을 수 있는 설명입니다.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 민감한 값인지 여부입니다.
	 */
	public boolean isSensitive() {
		return sensitive;
	}

	/**
	 * 대체로 사용할 수 있는 이름들입니다.
	 */
	public List<String> getAliases() {
		return aliases;
	}

	/**
	 * 정책 값 검증기 목록입니다.
	 */
	public List<PolicyValidator<T>> getValidators() {
		return validators;
	}

	/**
	 * 새로운 {@code PolicyKey}를 생성하기 위한 빌더를 시작합니다.
	 *
	 * @param <T>  생성할 정책 값의 타입
	 * @param name 정책의 문자열 키 이름 (null 또는 공백 불가)
	 * @param type 정책 값의 클래스 타입 (null 불가)
	 * @return 설정을 위한 빌더 인스턴스
	 * @throws IllegalArgumentException name이 비어있거나 type이 null인 경우 발생
	 */
	public static <T> Builder<T> builder(String name, Class<T> type) {
		return new Builder<>(name, type);
	}

	/**
	 * {@link PolicyKey} 인스턴스를 생성하기 위한 Fluent API 빌더 클래스입니다.
	 *
	 * @param <T> 정책 값의 타입
	 */
	public static final class Builder<T> {
		private final String name;
		private final Class<T> type;
		private T defaultValue;
		private String description;
		private boolean sensitive;
		private String namespace;
		private final List<String> aliases = new ArrayList<>();
		private final List<PolicyValidator<? super T>> validators = new ArrayList<>();

		/**
		 * 빌더의 필수 필드를 초기화합니다.
		 *
		 * @param name 정책 이름
		 * @param type 정책 타입
		 */
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

		/**
		 * 정책의 기본값을 설정합니다.
		 *
		 * @param defaultValue 사용할 기본값
		 * @return 현재 빌더 인스턴스
		 */
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

		/**
		 * 구성된 설정을 바탕으로 {@link PolicyKey} 인스턴스를 생성합니다.
		 *
		 * @return 생성된 PolicyKey 객체
		 */
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
