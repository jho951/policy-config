package com.policyconfig.api;

public class PolicyResolutionException extends RuntimeException {

	public PolicyResolutionException(String message) {
		super(message);
	}

	public PolicyResolutionException(String message, Throwable cause) {
		super(message, cause);
	}
}
