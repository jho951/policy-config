package com.policyconfig.core.source;

import com.policyconfig.api.PolicyKey;
import com.policyconfig.api.PolicyResolver;
import com.policyconfig.core.exception.ErrorCode;
import com.policyconfig.core.exception.PolicyConfigException;
import com.policyconfig.core.util.ValueParsers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 여러 ConfigSource를 합쳐서 PolicyResolver를 구현한 기본 클래스.
 * - source 순서대로 합치되, 나중에 오는 source가 우선순위가 높다고 가정.
 */
public final class DefaultPolicyResolver implements PolicyResolver {

	private final Map<String, String> merged;

	public DefaultPolicyResolver(List<ConfigSource> sources) {
		this.merged = mergeSources(sources);
	}

	private Map<String, String> mergeSources(List<ConfigSource> sources) {
		Map<String, String> result = new HashMap<>();
		for (ConfigSource source : sources) {
			Map<String, String> loaded = source.load();
			// 나중에 오는 source가 기존 값을 덮어쓴다 (우선순위 ↑)
			for (Map.Entry<String, String> e : loaded.entrySet()) {
				result.put(e.getKey(), e.getValue());
			}
		}
		return result;
	}

	@Override
	public <T> T require(PolicyKey<T> key) {
		T value = get(key);
		if (value == null) {
			throw new PolicyConfigException(ErrorCode.REQUIRED_POLICY_MISSING,"Required policy key missing: " + key.getName());
		}
		return value;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(PolicyKey<T> key) {
		String raw = merged.get(key.getName());
		if (raw == null || raw.isBlank()) {
			return key.getDefaultValue();
		}

		Class<T> type = key.getType();

		if (type == String.class) {
			return (T) raw;
		} else if (type == Integer.class) {
			return (T) ValueParsers.parseInt(key.getName(), raw);
		} else if (type == Long.class) {
			return (T) ValueParsers.parseLong(key.getName(), raw);
		} else if (type == Boolean.class) {
			return (T) ValueParsers.parseBoolean(key.getName(), raw);
		}

		throw new PolicyConfigException(
			ErrorCode.UNSUPPORTED_POLICY_TYPE,"Unsupported type for key: " + key.getName() + ", type: " + type);
	}
}
