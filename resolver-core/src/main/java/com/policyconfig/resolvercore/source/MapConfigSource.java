package com.policyconfig.resolvercore.source;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 단순 Map 기반 ConfigSource (테스트/임시 오버라이드용).
 */
public final class MapConfigSource implements ConfigSource {

	private final Map<String, String> entries;

	public MapConfigSource(Map<String, String> entries) {
		this.entries = new HashMap<>(entries);
	}

	@Override
	public Map<String, String> load() {
		return Collections.unmodifiableMap(entries);
	}
}
