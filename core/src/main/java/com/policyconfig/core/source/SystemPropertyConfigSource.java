package com.policyconfig.core.source;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * System.getProperties() 기반 ConfigSource.
 * - key/value는 String 프로퍼티만 취급합니다.
 */
public final class SystemPropertyConfigSource implements ConfigSource {

	@Override
	public Map<String, String> load() {
		Properties props = System.getProperties();
		Map<String, String> out = new HashMap<>();
		for (String name : props.stringPropertyNames()) {
			out.put(name, props.getProperty(name));
		}
		return out;
	}
}
