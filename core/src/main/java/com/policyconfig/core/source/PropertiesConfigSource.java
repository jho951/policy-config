package com.policyconfig.core.source;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * java.util.Properties 기반 ConfigSource.
 */
public final class PropertiesConfigSource implements ConfigSource {

	private final Map<String, String> entries;

	public PropertiesConfigSource(Properties properties) {
		Map<String, String> map = new HashMap<>();
		for (String name : properties.stringPropertyNames()) {
			map.put(name, properties.getProperty(name));
		}
		this.entries = Collections.unmodifiableMap(map);
	}

	@Override
	public Map<String, String> load() {
		return entries;
	}
}
