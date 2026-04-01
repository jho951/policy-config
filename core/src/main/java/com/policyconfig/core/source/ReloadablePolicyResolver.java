package com.policyconfig.core.source;

import com.policyconfig.api.PolicyKey;
import com.policyconfig.api.PolicyResolution;
import com.policyconfig.api.PolicyResolver;
import com.policyconfig.api.PolicySnapshotProvider;
import com.policyconfig.core.exception.ErrorCode;
import com.policyconfig.core.exception.PolicyConfigException;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class ReloadablePolicyResolver implements PolicyResolver, PolicySnapshotProvider, AutoCloseable {

	private final List<ConfigSource> sources;
	private final PolicyConverterRegistry converterRegistry;
	private final AtomicReference<DefaultPolicyResolver.Snapshot> snapshotRef;
	private final AtomicReference<DefaultPolicyResolver.Snapshot> previousSnapshotRef;
	private final List<AutoCloseable> closeables;

	public ReloadablePolicyResolver(List<ConfigSource> sources, PolicyConverterRegistry converterRegistry) {
		this.sources = List.copyOf(Objects.requireNonNull(sources, "sources"));
		this.converterRegistry = Objects.requireNonNull(converterRegistry, "converterRegistry");
		DefaultPolicyResolver initialResolver = new DefaultPolicyResolver(this.sources, this.converterRegistry);
		this.snapshotRef = new AtomicReference<>(initialResolver.snapshot());
		this.previousSnapshotRef = new AtomicReference<>(null);
		this.closeables = registerWatchers();
	}

	public void refresh() {
		DefaultPolicyResolver.Snapshot next = new DefaultPolicyResolver(sources, converterRegistry).snapshot();
		DefaultPolicyResolver.Snapshot current = snapshotRef.getAndSet(next);
		previousSnapshotRef.set(current);
	}

	@Override
	public void close() throws Exception {
		Exception first = null;
		for (AutoCloseable closeable : closeables) {
			try {
				closeable.close();
			} catch (Exception e) {
				if (first == null) {
					first = e;
				} else {
					first.addSuppressed(e);
				}
			}
		}
		if (first != null) {
			throw first;
		}
	}

	public DefaultPolicyResolver.Snapshot snapshot() {
		return snapshotRef.get();
	}

	public DefaultPolicyResolver.Snapshot previousSnapshot() {
		return previousSnapshotRef.get();
	}

	@Override
	public java.util.Map<String, String> snapshotValues() {
		return snapshotRef.get().values();
	}

	@Override
	public java.util.Map<String, String> snapshotOrigins() {
		return snapshotRef.get().origins();
	}

	@Override
	public <T> PolicyResolution<T> inspect(PolicyKey<T> key) {
		return DefaultPolicyResolver.resolve(snapshotRef.get(), converterRegistry, key);
	}

	public <T> PolicyResolution<T> inspectPrevious(PolicyKey<T> key) {
		DefaultPolicyResolver.Snapshot previous = previousSnapshotRef.get();
		if (previous == null) {
			return null;
		}
		return DefaultPolicyResolver.resolve(previous, converterRegistry, key);
	}

	@Override
	public <T> T require(PolicyKey<T> key) {
		PolicyResolution<T> resolution = inspect(key);
		if (!resolution.present()) {
			throw new PolicyConfigException(
				ErrorCode.REQUIRED_POLICY_MISSING,
				"Required policy key missing: " + key.getName()
			);
		}
		return resolution.value();
	}

	private List<AutoCloseable> registerWatchers() {
		List<AutoCloseable> handles = new java.util.ArrayList<>();
		for (ConfigSource source : sources) {
			if (source instanceof RefreshableConfigSource refreshableConfigSource) {
				refreshableConfigSource.watch(this::refresh);
				handles.add(refreshableConfigSource);
			}
		}
		return List.copyOf(handles);
	}
}
