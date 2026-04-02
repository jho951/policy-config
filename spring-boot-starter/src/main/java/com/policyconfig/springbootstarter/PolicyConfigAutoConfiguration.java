package com.policyconfig.springbootstarter;

import com.policyconfig.contracts.PolicyResolver;
import com.policyconfig.contracts.PolicyKey;
import com.policyconfig.contracts.PolicyKeyRegistry;
import com.policyconfig.builder.PolicyConfigs;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Spring Boot용 policy-config 자동 설정. */
@AutoConfiguration
@EnableConfigurationProperties(PolicyConfigProperties.class)
public class PolicyConfigAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public PolicyKeyRegistry policyKeyRegistry(ListableBeanFactory beanFactory) {
		PolicyKeyRegistry registry = new PolicyKeyRegistry();
		Set<String> annotatedBeans = new HashSet<>(Arrays.asList(beanFactory.getBeanNamesForAnnotation(PolicyKeyBean.class)));
		for (String beanName : beanFactory.getBeanNamesForType(PolicyKey.class, false, false)) {
			PolicyKey<?> key = beanFactory.getBean(beanName, PolicyKey.class);
			boolean explicit = annotatedBeans.contains(beanName);
			registry.register(key, explicit);
		}
		return registry;
	}

	@Bean
	@ConditionalOnMissingBean
	public PolicyResolver policyResolver(
		ConfigurableEnvironment environment,
		PolicyConfigProperties properties,
		ObjectProvider<com.policyconfig.resolvercore.source.ConfigSource> configSources,
		ObjectProvider<PolicyConverterBinding<?>> converterBindings
	) {
		PolicyConfigs.Builder builder = PolicyConfigs.builder()
			.add(new SpringEnvironmentConfigSource(environment, properties.getPrefix()));

		configSources.orderedStream().forEach(builder::add);

		converterBindings.orderedStream().forEach(binding -> registerBinding(builder, binding));

		if (properties.isReloadable()) {
			return builder.buildReloadable();
		}
		return builder.build();
	}

	@Bean
	@ConditionalOnMissingBean
	public PolicyConfigEndpoint policyConfigEndpoint(PolicyResolver policyResolver, PolicyKeyRegistry keyRegistry) {
		return new PolicyConfigEndpoint(policyResolver, keyRegistry);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void registerBinding(PolicyConfigs.Builder builder, PolicyConverterBinding<?> binding) {
		builder.converter((Class) binding.type(), (com.policyconfig.contracts.PolicyValueConverter) binding.converter());
	}
}
