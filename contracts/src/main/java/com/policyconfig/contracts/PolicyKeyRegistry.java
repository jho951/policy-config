package com.policyconfig.contracts;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Spring 어댑터용 {@link PolicyKey} 레지스트리. */
public final class PolicyKeyRegistry {

	private final Map<String, RegisteredPolicyKey> keys = new LinkedHashMap<>();

	public PolicyKeyRegistry register(PolicyKey<?> key) {
		return register(key, false);
	}

	public PolicyKeyRegistry register(PolicyKey<?> key, boolean explicit) {
		PolicyKey<?> resolved = Objects.requireNonNull(key, "key");
		RegisteredPolicyKey registration = new RegisteredPolicyKey(resolved, explicit);
		keys.put(resolved.getName(), registration);
		for (String alias : resolved.getAliases()) {
			keys.putIfAbsent(alias, registration);
		}
		return this;
	}

	public PolicyKeyRegistry registerAll(Collection<? extends PolicyKey<?>> keys) {
		if (keys != null) {
			for (PolicyKey<?> key : keys) {
				register(key);
			}
		}
		return this;
	}

	public Optional<PolicyKey<?>> find(String name) {
		return Optional.ofNullable(keys.get(name)).map(RegisteredPolicyKey::key);
	}

	public Map<String, PolicyKey<?>> asMap() {
		Map<String, PolicyKey<?>> out = new LinkedHashMap<>();
		for (Map.Entry<String, RegisteredPolicyKey> entry : keys.entrySet()) {
			out.put(entry.getKey(), entry.getValue().key());
		}
		return Map.copyOf(out);
	}

	public Set<PolicyKey<?>> keys() {
		Map<String, PolicyKey<?>> unique = new LinkedHashMap<>();
		for (RegisteredPolicyKey value : keys.values()) {
			unique.putIfAbsent(value.key().getName(), value.key());
		}
		return new LinkedHashSet<>(unique.values());
	}

	public Optional<RegisteredPolicyKey> findRegistration(String name) {
		return Optional.ofNullable(keys.get(name));
	}

	public int size() {
		return keys.size();
	}

	public record RegisteredPolicyKey(PolicyKey<?> key, boolean explicit) {
	}
}
