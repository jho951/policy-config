package com.policyconfig.core.source;

import com.policyconfig.api.PolicyKey;
import com.policyconfig.api.PolicyResolution;
import com.policyconfig.api.PolicyResolver;
import com.policyconfig.api.PolicySnapshotProvider;
import com.policyconfig.core.exception.PolicyConfigException;
import com.policyconfig.core.exception.ErrorCode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 여러 ConfigSource를 합쳐서 PolicyResolver를 구현한 기본 클래스.
 * - source 순서대로 합치되, 나중에 오는 source가 우선순위가 높다고 가정.
 */
public final class DefaultPolicyResolver implements PolicyResolver, PolicySnapshotProvider {

	private final PolicyConverterRegistry converterRegistry;
	private final Snapshot snapshot;

	public DefaultPolicyResolver(List<ConfigSource> sources) {
		this(sources, new PolicyConverterRegistry());
	}

	public DefaultPolicyResolver(List<ConfigSource> sources, PolicyConverterRegistry converterRegistry) {
		Objects.requireNonNull(sources, "sources");
		this.converterRegistry = Objects.requireNonNull(converterRegistry, "converterRegistry");
		this.snapshot = mergeSources(sources);
	}

	public Snapshot snapshot() {
		return snapshot;
	}

	@Override
	public Map<String, String> snapshotValues() {
		return snapshot.values();
	}

	@Override
	public Map<String, String> snapshotOrigins() {
		return snapshot.origins();
	}

	private Snapshot mergeSources(List<ConfigSource> sources) {
		Map<String, String> values = new LinkedHashMap<>();
		Map<String, String> origins = new LinkedHashMap<>();
		for (ConfigSource source : sources) {
			Map<String, String> loaded = source.load();
			for (Map.Entry<String, String> entry : loaded.entrySet()) {
				values.put(entry.getKey(), entry.getValue());
				origins.put(entry.getKey(), source.name());
			}
		}
		return new Snapshot(
			Collections.unmodifiableMap(new LinkedHashMap<>(values)),
			Collections.unmodifiableMap(new LinkedHashMap<>(origins))
		);
	}

	@Override
	public <T> PolicyResolution<T> inspect(PolicyKey<T> key) {
		return resolve(snapshot, converterRegistry, key);
	}

	@Override
	public <T> T require(PolicyKey<T> key) {
		PolicyResolution<T> resolution = inspect(key);
		if (!resolution.present()) {
			throw new PolicyConfigException(
				ErrorCode.REQUIRED_POLICY_MISSING,
				"Required policy key missing: " + key.getName()
			);
		}
		return resolution.value();
	}

	static <T> PolicyResolution<T> resolve(Snapshot snapshot, PolicyConverterRegistry converterRegistry, PolicyKey<T> key) {
		Objects.requireNonNull(snapshot, "snapshot");
		Objects.requireNonNull(converterRegistry, "converterRegistry");
		Objects.requireNonNull(key, "key");

		ResolvedRaw raw = findRaw(snapshot, key);
		if (raw == null) {
			T defaultValue = key.getDefaultValue();
			if (defaultValue == null) {
				return PolicyResolution.<T>builder(key)
					.present(false)
					.defaulted(false)
					.build();
			}
			validate(key, defaultValue);
			return PolicyResolution.<T>builder(key)
				.value(defaultValue)
				.present(true)
				.defaulted(true)
				.build();
		}

		T converted = converterRegistry.convert(key, raw.value());
		validate(key, converted);
		return PolicyResolution.<T>builder(key)
			.value(converted)
			.rawValue(raw.value())
			.matchedName(raw.matchedName())
			.sourceName(raw.sourceName())
			.present(true)
			.defaulted(false)
			.build();
	}

	private static <T> void validate(PolicyKey<T> key, T value) {
		for (var validator : key.getValidators()) {
			validator.validate(key.getName(), value);
		}
	}

	private static ResolvedRaw findRaw(Snapshot snapshot, PolicyKey<?> key) {
		if (hasText(snapshot.values().get(key.getName()))) {
			return new ResolvedRaw(key.getName(), snapshot.values().get(key.getName()), snapshot.origins().get(key.getName()));
		}
		for (String alias : key.getAliases()) {
			String raw = snapshot.values().get(alias);
			if (hasText(raw)) {
				return new ResolvedRaw(alias, raw, snapshot.origins().get(alias));
			}
		}
		return null;
	}

	private static boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	public record Snapshot(Map<String, String> values, Map<String, String> origins) {
	}

	private record ResolvedRaw(String matchedName, String value, String sourceName) {
	}
}
