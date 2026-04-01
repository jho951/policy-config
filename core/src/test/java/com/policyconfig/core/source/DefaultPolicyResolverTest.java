package com.policyconfig.core.source;

import com.policyconfig.api.PolicyKey;
import com.policyconfig.api.PolicyResolution;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPolicyResolverTest {

	@Test
	void usesLaterSourceForOverrides() {
		ConfigSource first = namedSource("first", Map.of("feature.enabled", "false"));
		ConfigSource second = namedSource("second", Map.of("feature.enabled", "true"));

		DefaultPolicyResolver resolver = new DefaultPolicyResolver(List.of(first, second));
		PolicyKey<Boolean> key = PolicyKey.builder("feature.enabled", Boolean.class).build();

		PolicyResolution<Boolean> resolution = resolver.inspect(key);

		assertTrue(resolution.present());
		assertTrue(resolution.value());
		assertEquals("second", resolution.sourceName());
	}

	@Test
	void resolvesAliasesAndDefaults() {
		ConfigSource source = namedSource("file", Map.of("app.timeout.ms", "42"));
		DefaultPolicyResolver resolver = new DefaultPolicyResolver(List.of(source));

		PolicyKey<Integer> key = PolicyKey.<Integer>builder("timeout", Integer.class)
			.alias("app.timeout.ms")
			.defaultValue(10)
			.build();

		assertEquals(42, resolver.get(key));
	}

	@Test
	void validatesDefaultValueWhenMissing() {
		DefaultPolicyResolver resolver = new DefaultPolicyResolver(List.of());
		PolicyKey<Integer> key = PolicyKey.<Integer>builder("limit", Integer.class)
			.defaultValue(0)
			.validator((name, value) -> {
				if (value <= 0) {
					throw new IllegalArgumentException("invalid");
				}
			})
			.build();

		assertThrows(IllegalArgumentException.class, () -> resolver.get(key));
	}

	@Test
	void requireThrowsWhenMissingAndNoDefault() {
		DefaultPolicyResolver resolver = new DefaultPolicyResolver(List.of());
		PolicyKey<String> key = PolicyKey.builder("missing", String.class).build();

		assertThrows(com.policyconfig.core.exception.PolicyConfigException.class, () -> resolver.require(key));
	}

	private static ConfigSource namedSource(String name, Map<String, String> entries) {
		return new ConfigSource() {
			@Override
			public Map<String, String> load() {
				return new LinkedHashMap<>(entries);
			}

			@Override
			public String name() {
				return name;
			}
		};
	}
}
