package com.policyconfig.example;

import com.policyconfig.api.PolicyKey;
import com.policyconfig.api.PolicyResolver;
import com.policyconfig.spring.PolicyConfigEndpoint;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExampleApplicationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(
			com.policyconfig.spring.PolicyConfigAutoConfiguration.class
		))
		.withPropertyValues("policy.config.reloadable=true")
		.withUserConfiguration(ExamplePolicyConfiguration.class);

	@Test
	void exposesDeclaredKeysGroupedSourcesAndDiff() {
		contextRunner.run(context -> {
			PolicyResolver resolver = context.getBean(PolicyResolver.class);
			PolicyConfigEndpoint endpoint = context.getBean(PolicyConfigEndpoint.class);
			ExamplePolicyConfiguration.MutableExampleSource source = context.getBean(ExamplePolicyConfiguration.MutableExampleSource.class);

			assertTrue(Boolean.TRUE.equals(resolver.require(context.getBean("featureEnabledKey", PolicyKey.class))));
			Map<String, Object> snapshot = endpoint.snapshot();
			assertTrue(snapshot.containsKey("sources"));
			assertEquals("full", snapshot.get("mode"));

			List<?> sources = (List<?>) snapshot.get("sources");
			assertTrue(sources.stream().map(Map.class::cast)
				.anyMatch(group -> "ExampleSource".equals(group.get("name"))));

			source.put("example.feature.enabled", "false");
			endpoint.refresh();

			Map<String, Object> diff = endpoint.snapshot("diff");
			List<?> diffEntries = (List<?>) diff.get("entries");
			assertTrue(diffEntries.stream().map(Map.class::cast)
				.anyMatch(entry -> "example.feature.enabled".equals(entry.get("name"))
					&& "changed".equals(entry.get("deltaStatus"))));
		});
	}
}
