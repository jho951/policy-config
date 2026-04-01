package com.policyconfig.example;

import com.policyconfig.api.PolicyKey;
import com.policyconfig.core.source.ConfigSource;
import com.policyconfig.spring.PolicyKeyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
public class ExamplePolicyConfiguration {

	@Bean
	@PolicyKeyBean
	public PolicyKey<Boolean> featureEnabledKey() {
		return PolicyKey.<Boolean>builder("example.feature.enabled", Boolean.class)
			.description("Enables the example feature")
			.defaultValue(false)
			.build();
	}

	@Bean
	@PolicyKeyBean
	public PolicyKey<String> apiTokenKey() {
		return PolicyKey.<String>builder("example.api.token", String.class)
			.sensitive(true)
			.defaultValue("demo-token")
			.build();
	}

	@Bean
	public MutableExampleSource exampleSource() {
		return new MutableExampleSource();
	}

	public static final class MutableExampleSource implements ConfigSource {
		private final Map<String, String> values = new LinkedHashMap<>();

		private MutableExampleSource() {
			values.put("example.feature.enabled", "true");
			values.put("example.api.token", "example-secret-token");
			values.put("example.remote.value", "from-example-source");
		}

		public void put(String key, String value) {
			values.put(key, value);
		}

		@Override
		public Map<String, String> load() {
			return new LinkedHashMap<>(values);
		}

		@Override
		public String name() {
			return "ExampleSource";
		}
	}
}
