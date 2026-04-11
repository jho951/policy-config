package com.policyconfig.builder;

import com.policyconfig.contracts.PolicyResolver;
import com.policyconfig.contracts.PolicyValueConverter;
import com.policyconfig.resolvercore.source.ConfigSource;
import com.policyconfig.resolvercore.source.DefaultPolicyResolver;
import com.policyconfig.resolvercore.source.EnvConfigSource;
import com.policyconfig.resolvercore.source.MapConfigSource;
import com.policyconfig.resolvercore.source.PolicyConverterRegistry;
import com.policyconfig.resolvercore.source.PropertiesConfigSource;
import com.policyconfig.resolvercore.source.PropertiesFileConfigSource;
import com.policyconfig.resolvercore.source.ReloadablePolicyResolver;
import com.policyconfig.resolvercore.source.SystemPropertyConfigSource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 1계층 조립 유틸리티.
 * 여러 ConfigSource를 묶어 PolicyResolver를 만드는 범용 helper다.
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
		private final PolicyConverterRegistry converterRegistry = new PolicyConverterRegistry();
		private boolean reloadable;

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

		public <T> Builder converter(Class<T> type, PolicyValueConverter<T> converter) {
			converterRegistry.register(type, converter);
			return this;
		}

		public Builder converters(Iterable<? extends PolicyConverterRegistry.RegisteredConverter<?>> converters) {
			converterRegistry.registerAll(converters);
			return this;
		}

		public Builder reloadable() {
			this.reloadable = true;
			return this;
		}

		public ReloadablePolicyResolver buildReloadable() {
			return new ReloadablePolicyResolver(sources, converterRegistry);
		}

		public PolicyResolver build() {
			if (reloadable) {
				return buildReloadable();
			}
			return new DefaultPolicyResolver(sources, converterRegistry);
		}
	}
}
