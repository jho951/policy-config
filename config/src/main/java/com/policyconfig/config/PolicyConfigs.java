package com.policyconfig.config;

import com.policyconfig.api.PolicyResolver;
import com.policyconfig.core.source.ConfigSource;
import com.policyconfig.core.source.DefaultPolicyResolver;
import com.policyconfig.core.source.EnvConfigSource;
import com.policyconfig.core.source.MapConfigSource;
import com.policyconfig.core.source.PropertiesConfigSource;
import com.policyconfig.core.source.PropertiesFileConfigSource;
import com.policyconfig.core.source.SystemPropertyConfigSource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * policy-config의 "조립(구성)" 유틸리티.
 *
 * <pre>{@code
 * PolicyResolver resolver = PolicyConfigs.builder()
 *     .env()
 *     .systemProperties()
 *     .map(Map.of("feature.myFeature", "true")) // override
 *     .build();
 * }</pre>
 */
public final class PolicyConfigs {

	private PolicyConfigs() {}

	public static Builder builder() {
		return new Builder();
	}

	/** 가장 단순: 환경변수만 */
	public static PolicyResolver envOnly() {
		return builder().env().build();
	}

	public static final class Builder {
		private final List<ConfigSource> sources = new ArrayList<>();

		/** 환경변수(System.getenv) */
		public Builder env() {
			sources.add(new EnvConfigSource());
			return this;
		}

		/** 시스템 프로퍼티(System.getProperties) */
		public Builder systemProperties() {
			sources.add(new SystemPropertyConfigSource());
			return this;
		}

		/** Properties 기반 */
		public Builder properties(Properties properties) {
			sources.add(new PropertiesConfigSource(properties));
			return this;
		}

		/** UTF-8 .properties 파일 */
		public Builder propertiesFile(Path path) {
			sources.add(new PropertiesFileConfigSource(path));
			return this;
		}

		/** Map 기반(테스트/오버라이드) */
		public Builder map(Map<String, String> entries) {
			sources.add(new MapConfigSource(entries));
			return this;
		}

		/** 커스텀 소스 추가 */
		public Builder add(ConfigSource source) {
			sources.add(Objects.requireNonNull(source, "source"));
			return this;
		}

		public PolicyResolver build() {
			return new DefaultPolicyResolver(sources);
		}
	}
}
