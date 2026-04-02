package com.policyconfig.springbootstarter;

import com.policyconfig.resolvercore.source.ConfigSource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class SpringEnvironmentConfigSource implements ConfigSource {

	private final ConfigurableEnvironment environment;
	private final String prefix;

	public SpringEnvironmentConfigSource(ConfigurableEnvironment environment) {
		this(environment, null);
	}

	public SpringEnvironmentConfigSource(ConfigurableEnvironment environment, String prefix) {
		this.environment = Objects.requireNonNull(environment, "environment");
		this.prefix = normalizePrefix(prefix);
	}

	@Override
	public Map<String, String> load() {
		Set<String> names = new LinkedHashSet<>();
		for (PropertySource<?> propertySource : environment.getPropertySources()) {
			if (propertySource instanceof EnumerablePropertySource<?> enumerablePropertySource) {
				names.addAll(Arrays.asList(enumerablePropertySource.getPropertyNames()));
			}
		}

		Map<String, String> values = new LinkedHashMap<>();
		for (String name : names) {
			if (prefix != null && !name.startsWith(prefix)) {
				continue;
			}
			String value = environment.getProperty(name);
			if (value != null) {
				values.put(name, value);
			}
		}
		return values;
	}

	@Override
	public String name() {
		return "SpringEnvironment";
	}

	private static String normalizePrefix(String prefix) {
		if (prefix == null || prefix.isBlank()) {
			return null;
		}
		String normalized = prefix.trim();
		if (!normalized.endsWith(".")) {
			normalized = normalized + ".";
		}
		return normalized;
	}
}
