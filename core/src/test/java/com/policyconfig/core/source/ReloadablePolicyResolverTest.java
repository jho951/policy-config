package com.policyconfig.core.source;

import com.policyconfig.api.PolicyKey;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReloadablePolicyResolverTest {

	@Test
	void refreshesSnapshotFromMutableSource() {
		MutableSource source = new MutableSource("memory");
		source.put("feature.toggle", "false");

		ReloadablePolicyResolver resolver = new ReloadablePolicyResolver(List.of(source), new PolicyConverterRegistry());
		PolicyKey<Boolean> key = PolicyKey.builder("feature.toggle", Boolean.class).build();

		assertEquals(false, resolver.get(key));

		source.put("feature.toggle", "true");
		assertEquals(false, resolver.get(key));

		resolver.refresh();
		assertEquals(true, resolver.get(key));
	}

	private static final class MutableSource implements ConfigSource {
		private final String name;
		private final Map<String, String> values = new LinkedHashMap<>();

		private MutableSource(String name) {
			this.name = name;
		}

		void put(String key, String value) {
			values.put(key, value);
		}

		@Override
		public Map<String, String> load() {
			return new LinkedHashMap<>(values);
		}

		@Override
		public String name() {
			return name;
		}
	}
}
