package com.policyconfig.resolvercore.source;

import com.policyconfig.contracts.PolicyKey;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class PropertiesFileAutoRefreshTest {

	@Test
	void reloadsAutomaticallyWhenFileChanges() throws Exception {
		Path file = Files.createTempFile("policy-config", ".properties");
		Files.writeString(file, "feature.enabled=false\n", StandardCharsets.UTF_8);

		PolicyKey<Boolean> key = PolicyKey.builder("feature.enabled", Boolean.class).build();

		try (ReloadablePolicyResolver resolver = new ReloadablePolicyResolver(
			List.of(new PropertiesFileConfigSource(file)),
			new PolicyConverterRegistry()
		)) {
			assertEquals(false, resolver.get(key));

			Files.writeString(file, "feature.enabled=true\n", StandardCharsets.UTF_8);

			waitFor(() -> assertEquals(true, resolver.get(key)), 5, TimeUnit.SECONDS);
		} finally {
			Files.deleteIfExists(file);
		}
	}

	private static void waitFor(CheckedRunnable assertion, long timeout, TimeUnit unit) throws Exception {
		long deadline = System.nanoTime() + unit.toNanos(timeout);
		AssertionError last = null;
		while (System.nanoTime() < deadline) {
			try {
				assertion.run();
				return;
			} catch (AssertionError e) {
				last = e;
				Thread.sleep(100);
			}
		}
		if (last != null) {
			throw last;
		}
		fail("timeout waiting for file refresh");
	}

	@FunctionalInterface
	private interface CheckedRunnable {
		void run() throws Exception;
	}
}
