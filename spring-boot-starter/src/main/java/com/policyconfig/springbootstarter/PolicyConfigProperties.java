package com.policyconfig.springbootstarter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "policy.config")
public class PolicyConfigProperties {

	/**
	 * 정책 키 prefix.
	 */
	private String prefix;

	/**
	 * reloadable 사용 여부.
	 */
	private boolean reloadable;

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public boolean isReloadable() {
		return reloadable;
	}

	public void setReloadable(boolean reloadable) {
		this.reloadable = reloadable;
	}
}
