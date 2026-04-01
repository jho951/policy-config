package com.policyconfig.spring;

import com.policyconfig.api.PolicyKey;
import com.policyconfig.api.PolicyResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import com.policyconfig.core.source.ConfigSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyConfigAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(PolicyConfigAutoConfiguration.class));

	@Test
	void createsPolicyResolverBean() {
		contextRunner
			.withPropertyValues("policy.demo.enabled=true")
			.run(context -> {
				PolicyResolver resolver = context.getBean(PolicyResolver.class);
				assertNotNull(resolver);
				assertTrue(resolver.require(PolicyKey.builder("policy.demo.enabled", Boolean.class).build()));
			});
	}

	@Test
	void appliesPrefixFilterAndCustomConverterBindings() {
		new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(PolicyConfigAutoConfiguration.class))
			.withPropertyValues(
				"policy.config.prefix=policy.demo",
				"policy.config.reloadable=true",
				"policy.demo.id=123e4567-e89b-12d3-a456-426614174000",
				"other.key=ignored"
			)
			.withBean(PolicyConverterBinding.class, () ->
				PolicyConverterBinding.of(java.util.UUID.class, (key, raw) -> java.util.UUID.fromString(raw))
			)
			.run(context -> {
				PolicyResolver resolver = context.getBean(PolicyResolver.class);
				assertTrue(resolver.get(PolicyKey.builder("policy.demo.id", java.util.UUID.class).build()) != null);
				assertTrue(context.getBean(PolicyConfigProperties.class).isReloadable());
			});
	}

	@Test
	void exposesEndpointSnapshotAndRefresh() {
		new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(PolicyConfigAutoConfiguration.class))
			.withPropertyValues(
				"policy.config.reloadable=true",
				"policy.demo.enabled=false",
				"policy.demo.secret=top-secret"
			)
			.withBean("policyDemoSecretKey", PolicyKey.class, () -> PolicyKey.<String>builder("policy.demo.secret", String.class).sensitive(true).build())
			.withBean("policyDemoEnabledKey", PolicyKey.class, () -> PolicyKey.<Boolean>builder("policy.demo.enabled", Boolean.class).build())
			.run(context -> {
				PolicyConfigEndpoint endpoint = context.getBean(PolicyConfigEndpoint.class);
				PolicyResolver resolver = context.getBean(PolicyResolver.class);

				assertNotNull(endpoint);
				assertTrue(endpoint.snapshot().containsKey("entries"));
				List<?> entries = (List<?>) endpoint.snapshot().get("entries");
				assertTrue(entries.stream().map(Map.class::cast)
					.anyMatch(entry -> "policy.demo.secret".equals(entry.get("name"))
						&& "***".equals(entry.get("value"))
						&& Boolean.TRUE.equals(entry.get("sensitive"))));
				Map<?, ?> summary = (Map<?, ?>) endpoint.snapshot().get("summary");
				assertTrue(summary.containsKey("total"));
				assertTrue(summary.containsKey("defaulted"));
				assertTrue(summary.containsKey("aliasHits"));
				assertTrue(endpoint.refresh().containsKey("refreshed"));
				assertFalse(resolver.require(PolicyKey.builder("policy.demo.enabled", Boolean.class).build()));
			});
	}

	@Test
	void marksAnnotatedPolicyKeyBeansAsDeclared() {
		new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(PolicyConfigAutoConfiguration.class))
			.withUserConfiguration(AnnotatedKeysConfig.class)
			.withPropertyValues("policy.config.reloadable=false")
			.run(context -> {
				PolicyConfigEndpoint endpoint = context.getBean(PolicyConfigEndpoint.class);
				List<?> entries = (List<?>) endpoint.snapshot().get("entries");

				assertTrue(entries.stream().map(Map.class::cast)
					.anyMatch(entry -> "policy.demo.secret".equals(entry.get("name"))
						&& Boolean.TRUE.equals(entry.get("declared"))));
			});
	}

	@Test
	void diffModeReturnsOnlyChangedEntries() {
		new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(PolicyConfigAutoConfiguration.class))
			.withUserConfiguration(AnnotatedKeysConfig.class)
			.withPropertyValues("policy.config.reloadable=true")
			.run(context -> {
				PolicyConfigEndpoint endpoint = context.getBean(PolicyConfigEndpoint.class);
				AnnotatedKeysConfig.MutableSource source = context.getBean(AnnotatedKeysConfig.MutableSource.class);

				List<?> full = (List<?>) endpoint.snapshot().get("entries");
				assertTrue(full.size() >= 1);

				source.put("policy.demo.enabled", "false");
				endpoint.refresh();

				List<?> diff = (List<?>) endpoint.snapshot("diff").get("entries");

				assertTrue(diff.stream().map(Map.class::cast)
					.anyMatch(entry -> "policy.demo.enabled".equals(entry.get("name"))
						&& "changed".equals(entry.get("deltaStatus"))));
			});
	}

	@Configuration(proxyBeanMethods = false)
	static class AnnotatedKeysConfig {

		@Bean
		MutableSource mutableSource() {
			return new MutableSource();
		}

		@Bean
		@PolicyKeyBean
		PolicyKey<String> policyDemoSecretKey() {
			return PolicyKey.<String>builder("policy.demo.secret", String.class)
				.sensitive(true)
				.defaultValue("fallback")
				.build();
		}

		@Bean
		PolicyKey<Boolean> policyDemoEnabledKey() {
			return PolicyKey.<Boolean>builder("policy.demo.enabled", Boolean.class)
				.defaultValue(true)
				.build();
		}

		static final class MutableSource implements ConfigSource {
			private final Map<String, String> values = new LinkedHashMap<>();

			private MutableSource() {
				values.put("policy.demo.enabled", "true");
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
				return "TestSource";
			}
		}
	}
}
