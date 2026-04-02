package com.policyconfig.resolvercore.source;

import com.policyconfig.contracts.PolicyKey;
import com.policyconfig.contracts.PolicyValueConverter;
import com.policyconfig.resolvercore.exception.ErrorCode;
import com.policyconfig.resolvercore.exception.PolicyConfigException;
import com.policyconfig.resolvercore.util.ValueParsers;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class PolicyConverterRegistry {

	private final Map<Class<?>, PolicyValueConverter<?>> converters = new LinkedHashMap<>();

	public PolicyConverterRegistry() {
		registerBuiltinConverters();
	}

	public <T> PolicyConverterRegistry register(Class<T> type, PolicyValueConverter<T> converter) {
		Objects.requireNonNull(type, "type");
		Objects.requireNonNull(converter, "converter");
		converters.put(type, converter);
		return this;
	}

	public PolicyConverterRegistry registerAll(Iterable<? extends RegisteredConverter<?>> converters) {
		if (converters != null) {
			for (RegisteredConverter<?> converter : converters) {
				registerRaw(converter);
			}
		}
		return this;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void registerRaw(RegisteredConverter<?> converter) {
		register((Class) converter.type(), (PolicyValueConverter) converter.converter());
	}

	@SuppressWarnings("unchecked")
	public <T> T convert(PolicyKey<T> key, String raw) {
		Class<T> type = key.getType();

		if (type == String.class) {
			return (T) raw;
		}
		if (type == Integer.class) {
			return (T) ValueParsers.parseInt(key.getName(), raw);
		}
		if (type == Long.class) {
			return (T) ValueParsers.parseLong(key.getName(), raw);
		}
		if (type == Boolean.class) {
			return (T) ValueParsers.parseBoolean(key.getName(), raw);
		}
		if (type == BigDecimal.class) {
			return (T) parseBigDecimal(key.getName(), raw);
		}
		if (type == Duration.class) {
			return (T) parseDuration(key.getName(), raw);
		}
		if (type == List.class) {
			return (T) parseStringList(raw);
		}
		if (type == Set.class) {
			return (T) parseStringSet(raw);
		}
		if (type.isEnum()) {
			return (T) parseEnum((Class<? extends Enum>) type, key.getName(), raw);
		}

		PolicyValueConverter<T> converter = (PolicyValueConverter<T>) converters.get(type);
		if (converter != null) {
			return converter.convert(key.getName(), raw);
		}

		throw new PolicyConfigException(
			ErrorCode.UNSUPPORTED_POLICY_TYPE,
			"Unsupported type for key: " + key.getName() + ", type: " + type
		);
	}

	private void registerBuiltinConverters() {
		register(String.class, (key, raw) -> raw);
		register(Integer.class, ValueParsers::parseInt);
		register(Long.class, ValueParsers::parseLong);
		register(Boolean.class, ValueParsers::parseBoolean);
		register(BigDecimal.class, PolicyConverterRegistry::parseBigDecimal);
		register(Duration.class, PolicyConverterRegistry::parseDuration);
	}

	private static BigDecimal parseBigDecimal(String key, String raw) {
		try {
			return new BigDecimal(raw);
		} catch (NumberFormatException e) {
			throw new PolicyConfigException(
				ErrorCode.INVALID_POLICY_VALUE,
				"Failed to parse decimal for key: " + key + ", value: " + raw,
				e
			);
		}
	}

	private static Duration parseDuration(String key, String raw) {
		try {
			String normalized = raw.trim().toLowerCase(Locale.ROOT);
			if (normalized.matches("^\\d+$")) {
				return Duration.ofSeconds(Long.parseLong(normalized));
			}
			if (normalized.endsWith("ms")) {
				return Duration.ofMillis(Long.parseLong(normalized.substring(0, normalized.length() - 2)));
			}
			if (normalized.endsWith("s")) {
				return Duration.ofSeconds(Long.parseLong(normalized.substring(0, normalized.length() - 1)));
			}
			if (normalized.endsWith("m")) {
				return Duration.ofMinutes(Long.parseLong(normalized.substring(0, normalized.length() - 1)));
			}
			if (normalized.endsWith("h")) {
				return Duration.ofHours(Long.parseLong(normalized.substring(0, normalized.length() - 1)));
			}
			if (normalized.endsWith("d")) {
				return Duration.ofDays(Long.parseLong(normalized.substring(0, normalized.length() - 1)));
			}
			return Duration.parse(raw);
		} catch (Exception e) {
			throw new PolicyConfigException(
				ErrorCode.INVALID_POLICY_VALUE,
				"Failed to parse duration for key: " + key + ", value: " + raw,
				e
			);
		}
	}

	private static List<String> parseStringList(String raw) {
		if (raw == null || raw.isBlank()) {
			return List.of();
		}
		List<String> values = new ArrayList<>();
		for (String part : raw.split(",")) {
			String trimmed = part.trim();
			if (!trimmed.isEmpty()) {
				values.add(trimmed);
			}
		}
		return Collections.unmodifiableList(values);
	}

	private static Set<String> parseStringSet(String raw) {
		return Collections.unmodifiableSet(new LinkedHashSet<>(parseStringList(raw)));
	}

	private static Enum<?> parseEnum(Class<? extends Enum> type, String key, String raw) {
		for (Object constant : type.getEnumConstants()) {
			Enum<?> enumValue = (Enum<?>) constant;
			if (enumValue.name().equals(raw) || enumValue.name().equalsIgnoreCase(raw)) {
				return enumValue;
			}
		}
		throw new PolicyConfigException(
			ErrorCode.INVALID_POLICY_VALUE,
			"Failed to parse enum for key: " + key + ", value: " + raw + ", enum: " + type.getName()
		);
	}

	public record RegisteredConverter<T>(Class<T> type, PolicyValueConverter<T> converter) {
	}
}
