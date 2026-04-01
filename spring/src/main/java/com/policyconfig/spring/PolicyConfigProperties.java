package com.policyconfig.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "policy.config")
public class PolicyConfigProperties {

	/**
	 * 환경에서 읽을 정책 키 prefix. 비어 있으면 전체 키를 사용한다.
	 */
	private String prefix;

	/**
	 * reloadable resolver를 사용할지 여부.
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
