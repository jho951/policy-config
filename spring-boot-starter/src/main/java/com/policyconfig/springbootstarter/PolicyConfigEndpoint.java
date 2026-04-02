package com.policyconfig.springbootstarter;

import com.policyconfig.contracts.PolicyKey;
import com.policyconfig.contracts.PolicyKeyRegistry;
import com.policyconfig.contracts.PolicyKeyRegistry.RegisteredPolicyKey;
import com.policyconfig.contracts.PolicyResolution;
import com.policyconfig.contracts.PolicySnapshotProvider;
import com.policyconfig.contracts.PolicyResolver;
import com.policyconfig.resolvercore.exception.PolicyConfigException;
import com.policyconfig.resolvercore.source.ReloadablePolicyResolver;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** policy-config 관찰성 endpoint. */
@Endpoint(id = "policy-config")
public final class PolicyConfigEndpoint {

	private final PolicyResolver policyResolver;
	private final PolicyKeyRegistry keyRegistry;

	public PolicyConfigEndpoint(PolicyResolver policyResolver, PolicyKeyRegistry keyRegistry) {
		this.policyResolver = Objects.requireNonNull(policyResolver, "policyResolver");
		this.keyRegistry = Objects.requireNonNull(keyRegistry, "keyRegistry");
	}

	@ReadOperation
	public Map<String, Object> snapshot(@Nullable String mode) {
		boolean diffMode = "diff".equalsIgnoreCase(mode);
		Map<String, Object> payload = basePayload(diffMode);
		if (!(policyResolver instanceof PolicySnapshotProvider snapshotProvider)) {
			payload.put("entries", List.of());
			payload.put("sources", List.of());
			payload.put("summary", summary(0, 0, 0, 0, 0, 0));
			return payload;
		}

		Set<PolicyKey<?>> uniqueKeys = new LinkedHashSet<>(keyRegistry.keys());
		ReloadablePolicyResolver reloadable = policyResolver instanceof ReloadablePolicyResolver
			? (ReloadablePolicyResolver) policyResolver
			: null;

		List<Map<String, Object>> entries = buildEntries(uniqueKeys, reloadable);
		List<Map<String, Object>> visibleEntries = diffMode
			? entries.stream().filter(entry -> isDiffEntry(entry)).toList()
			: entries;

		payload.put("entries", visibleEntries);
		payload.put("sources", buildSourceGroups(visibleEntries));
		payload.put("count", snapshotProvider.snapshotValues().size());
		payload.put("reloadable", reloadable != null);
		payload.put("summary", buildSummary(visibleEntries, diffMode));
		return payload;
	}

	public Map<String, Object> snapshot() {
		return snapshot(null);
	}

	@WriteOperation
	public Map<String, Object> refresh() {
		Map<String, Object> payload = new LinkedHashMap<>();
		if (policyResolver instanceof ReloadablePolicyResolver reloadablePolicyResolver) {
			reloadablePolicyResolver.refresh();
			payload.put("refreshed", true);
			payload.put("count", reloadablePolicyResolver.snapshotValues().size());
			return payload;
		}
		payload.put("refreshed", false);
		payload.put("reason", "PolicyResolver is not reloadable");
		return payload;
	}

	private Map<String, Object> basePayload(boolean diffMode) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("resolverType", policyResolver.getClass().getSimpleName());
		payload.put("registryCount", keyRegistry.size());
		payload.put("mode", diffMode ? "diff" : "full");
		return payload;
	}

	private List<Map<String, Object>> buildEntries(Set<PolicyKey<?>> uniqueKeys, ReloadablePolicyResolver reloadable) {
		List<Map<String, Object>> entries = new ArrayList<>();
		for (PolicyKey<?> key : uniqueKeys) {
			entries.add(resolveEntry(key, reloadable));
		}
		return entries;
	}

	private Map<String, Object> resolveEntry(PolicyKey<?> key, ReloadablePolicyResolver reloadable) {
		Map<String, Object> item = new LinkedHashMap<>();
		item.put("name", key.getName());
		item.put("description", key.getDescription());
		item.put("sensitive", key.isSensitive());
		item.put("aliases", key.getAliases());
		item.put("declared", keyRegistry.findRegistration(key.getName()).map(RegisteredPolicyKey::explicit).orElse(false));
		try {
			PolicyResolution<?> current = policyResolver.inspect(key);
			PolicyResolution<?> previous = reloadable == null ? null : reloadable.inspectPrevious(key);
			item.put("value", maskValue(key.getName(), current.displayValue(), key));
			item.put("source", current.sourceName());
			item.put("defaulted", current.defaulted());
			item.put("matched", current.matchedName() == null ? "missing" : current.matchedName().equals(key.getName()) ? "direct" : "alias");
			item.put("aliasHit", current.matchedName() != null && !current.matchedName().equals(key.getName()));
			item.put("present", current.present());
			item.put("status", current.present() ? "ok" : "missing");
			item.put("previousValue", previous == null ? null : maskValue(key.getName(), previous.displayValue(), key));
			item.put("previousSource", previous == null ? null : previous.sourceName());
			item.put("previousPresent", previous != null && previous.present());
			item.put("previousDefaulted", previous != null && previous.defaulted());
			item.put("changedFromPrevious", changedFromPrevious(current, previous));
			item.put("deltaStatus", deltaStatus(current, previous));
		} catch (PolicyConfigException e) {
			item.put("value", null);
			item.put("source", null);
			item.put("defaulted", false);
			item.put("matched", null);
			item.put("aliasHit", false);
			item.put("present", false);
			item.put("status", "error");
			item.put("previousValue", null);
			item.put("previousSource", null);
			item.put("previousPresent", false);
			item.put("previousDefaulted", false);
			item.put("changedFromPrevious", false);
			item.put("deltaStatus", "error");
			item.put("error", e.getMessage());
			item.put("errorCode", e.getErrorCode() == null ? null : e.getErrorCode().code());
		}
		return item;
	}

	private static boolean changedFromPrevious(PolicyResolution<?> current, @Nullable PolicyResolution<?> previous) {
		if (previous == null) {
			return false;
		}
		return current.present() != previous.present()
			|| current.defaulted() != previous.defaulted()
			|| !Objects.equals(current.value(), previous.value())
			|| !Objects.equals(current.sourceName(), previous.sourceName())
			|| !Objects.equals(current.matchedName(), previous.matchedName());
	}

	private static String deltaStatus(PolicyResolution<?> current, @Nullable PolicyResolution<?> previous) {
		if (previous == null) {
			return "baseline";
		}
		if (!Objects.equals(current.value(), previous.value())
			|| current.present() != previous.present()
			|| current.defaulted() != previous.defaulted()
			|| !Objects.equals(current.sourceName(), previous.sourceName())
			|| !Objects.equals(current.matchedName(), previous.matchedName())) {
			return "changed";
		}
		return "unchanged";
	}

	private static boolean isDiffEntry(Map<String, Object> entry) {
		return "changed".equals(entry.get("deltaStatus"))
			|| "error".equals(entry.get("deltaStatus"));
	}

	private List<Map<String, Object>> buildSourceGroups(List<Map<String, Object>> entries) {
		Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
		for (Map<String, Object> entry : entries) {
			String source = entry.get("source") == null ? "missing" : String.valueOf(entry.get("source"));
			grouped.computeIfAbsent(source, ignored -> new ArrayList<>()).add(entry);
		}

		List<Map<String, Object>> sources = new ArrayList<>();
		for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
			Map<String, Object> source = new LinkedHashMap<>();
			source.put("name", entry.getKey());
			source.put("count", entry.getValue().size());
			source.put("entries", entry.getValue());
			source.put("summary", buildSourceSummary(entry.getValue()));
			sources.add(source);
		}
		return sources;
	}

	private static Map<String, Object> buildSourceSummary(List<Map<String, Object>> entries) {
		int added = 0;
		int removed = 0;
		int changed = 0;
		int errors = 0;
		for (Map<String, Object> entry : entries) {
			String status = String.valueOf(entry.get("status"));
			if ("error".equals(status)) {
				errors++;
				continue;
			}
			boolean previousPresent = Boolean.TRUE.equals(entry.get("previousPresent"));
			boolean present = Boolean.TRUE.equals(entry.get("present"));
			boolean changedFromPrevious = Boolean.TRUE.equals(entry.get("changedFromPrevious"));
			if (changedFromPrevious && !previousPresent && present) {
				added++;
			} else if (changedFromPrevious && previousPresent && !present) {
				removed++;
			} else if (changedFromPrevious) {
				changed++;
			}
		}
		Map<String, Object> summary = new LinkedHashMap<>();
		summary.put("total", entries.size());
		summary.put("added", added);
		summary.put("removed", removed);
		summary.put("changed", changed);
		summary.put("errors", errors);
		return summary;
	}

	private Map<String, Object> buildSummary(List<Map<String, Object>> entries, boolean diffMode) {
		int defaulted = 0;
		int aliasHits = 0;
		int missing = 0;
		int errors = 0;
		int changed = 0;
		for (Map<String, Object> entry : entries) {
			if (Boolean.TRUE.equals(entry.get("defaulted"))) {
				defaulted++;
			}
			if (Boolean.TRUE.equals(entry.get("aliasHit"))) {
				aliasHits++;
			}
			if ("missing".equals(entry.get("status"))) {
				missing++;
			}
			if ("error".equals(entry.get("status"))) {
				errors++;
			}
			if (Boolean.TRUE.equals(entry.get("changedFromPrevious"))) {
				changed++;
			}
		}
		int total = diffMode ? entries.size() : keyRegistry.keys().size();
		return summary(total, defaulted, aliasHits, errors + missing, changed, entries.size());
	}

	private static Map<String, Object> summary(int total, int defaulted, int aliasHits, int errorOrMissing, int changedFromPrevious, int returned) {
		Map<String, Object> summary = new LinkedHashMap<>();
		summary.put("total", total);
		summary.put("returned", returned);
		summary.put("defaulted", defaulted);
		summary.put("aliasHits", aliasHits);
		summary.put("errorOrMissing", errorOrMissing);
		summary.put("changedFromPrevious", changedFromPrevious);
		return summary;
	}

	private static String maskValue(String key, String value, PolicyKey<?> policyKey) {
		if (policyKey != null && policyKey.isSensitive()) {
			return "***";
		}
		if (value == null) {
			return null;
		}
		return shouldMask(key) ? "***" : value;
	}

	private static boolean shouldMask(String key) {
		String lower = key.toLowerCase();
		return lower.contains("password")
			|| lower.contains("secret")
			|| lower.contains("token")
			|| lower.contains("credential")
			|| lower.contains("private")
			|| lower.contains("api.key")
			|| lower.contains("apikey");
	}
}
