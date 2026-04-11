package com.policyconfig.builder;

import com.policyconfig.contracts.PolicyKey;
import com.policyconfig.contracts.PolicyResolver;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyConfigsTest {

	@Test
	void supportsCustomConverterAndReloadableMode() {
		PolicyResolver resolver = PolicyConfigs.builder()
			.converter(UUID.class, (key, raw) -> UUID.fromString(raw))
			.map(Map.of("app.id", "123e4567-e89b-12d3-a456-426614174000"))
			.reloadable()
			.build();

		PolicyKey<UUID> key = PolicyKey.builder("app.id", UUID.class).build();
		assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), resolver.get(key));
		assertTrue(resolver.get(PolicyKey.<List>builder("missing.list", List.class).defaultValue(List.of()).build()).isEmpty());
	}
}
