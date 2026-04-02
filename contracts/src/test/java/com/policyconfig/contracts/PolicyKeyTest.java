package com.policyconfig.contracts;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PolicyKeyTest {

	@Test
	void appliesNamespaceAliasesAndMetadata() {
		PolicyKey<String> key = PolicyKey.<String>builder("timeout", String.class)
			.namespace("app")
			.alias("timeout.ms")
			.description("request timeout")
			.sensitive(true)
			.validator((name, value) -> {
				if (value == null || value.isBlank()) {
					throw new IllegalArgumentException("blank");
				}
			})
			.build();

		assertEquals("app.timeout", key.getName());
		assertEquals(List.of("app.timeout.ms"), key.getAliases());
		assertEquals("request timeout", key.getDescription());
		assertTrue(key.isSensitive());
		assertEquals(1, key.getValidators().size());
	}

	@Test
	void rejectsBlankName() {
		assertThrows(IllegalArgumentException.class, () -> PolicyKey.builder(" ", String.class));
	}
}
