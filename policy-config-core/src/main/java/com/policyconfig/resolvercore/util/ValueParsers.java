package com.policyconfig.resolvercore.util;

import com.policyconfig.resolvercore.exception.ErrorCode;
import com.policyconfig.resolvercore.exception.PolicyConfigException;

import java.util.Locale;

public final class ValueParsers {

	private ValueParsers() {
	}

	public static Integer parseInt(String key, String raw) {
		try {
			return Integer.valueOf(raw);
		} catch (NumberFormatException e) {
			throw new PolicyConfigException(
				ErrorCode.INVALID_POLICY_VALUE,"Failed to parse int for key: " + key + ", value: " + raw, e);
		}
	}

	public static Long parseLong(String key, String raw) {
		try {
			return Long.valueOf(raw);
		} catch (NumberFormatException e) {
			throw new PolicyConfigException(
				ErrorCode.INVALID_POLICY_VALUE,"Failed to parse long for key: " + key + ", value: " + raw, e);
		}
	}

	public static Boolean parseBoolean(String key, String raw) {
		if (raw == null) {
			return null;
		}
		String lower = raw.toLowerCase(Locale.ROOT);
		if ("true".equals(lower) || "1".equals(lower) || "yes".equals(lower) || "y".equals(lower)) {
			return Boolean.TRUE;
		}
		if ("false".equals(lower) || "0".equals(lower) || "no".equals(lower) || "n".equals(lower)) {
			return Boolean.FALSE;
		}
		throw new PolicyConfigException(
			ErrorCode.INVALID_POLICY_VALUE,"Failed to parse boolean for key: " + key + ", value: " + raw);
	}
}
