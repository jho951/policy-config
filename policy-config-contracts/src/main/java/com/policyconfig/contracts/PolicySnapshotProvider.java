package com.policyconfig.contracts;

import java.util.Map;

public interface PolicySnapshotProvider {

	Map<String, String> snapshotValues();

	Map<String, String> snapshotOrigins();
}
