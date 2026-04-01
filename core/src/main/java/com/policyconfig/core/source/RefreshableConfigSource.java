package com.policyconfig.core.source;

public interface RefreshableConfigSource extends ConfigSource, AutoCloseable {

	void watch(Runnable onChange);

	@Override
	default void close() throws Exception {
		// optional
	}
}
