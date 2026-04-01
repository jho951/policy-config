package com.policyconfig.api;

import java.util.Map;

public interface PolicySnapshotProvider {

	Map<String, String> snapshotValues();

	Map<String, String> snapshotOrigins();
}
