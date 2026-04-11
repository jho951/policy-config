package com.policyconfig.resolvercore.source;

import java.util.HashMap;
import java.util.Map;

/**
 * System.getenv() 기반 ConfigSource.
 */
public final class EnvConfigSource implements ConfigSource {

	@Override
	public Map<String, String> load() {
		// 그대로 반환하면 수정 가능하니까 defensive copy
		return new HashMap<>(System.getenv());
	}
}
