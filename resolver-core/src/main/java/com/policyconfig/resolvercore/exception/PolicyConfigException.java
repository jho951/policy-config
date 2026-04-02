package com.policyconfig.resolvercore.exception;

public class PolicyConfigException extends RuntimeException {

	private final ErrorCode errorCode;

	public PolicyConfigException(ErrorCode errorCode) {
		super(errorCode.defaultMessage());
		this.errorCode = errorCode;
	}

	public PolicyConfigException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public PolicyConfigException(ErrorCode errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public PolicyConfigException(ErrorCode errorCode, Throwable cause) {
		super(errorCode.defaultMessage(), cause);
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	@Override
	public String toString() {
		return "PolicyConfigException{" + "errorCode=" + errorCode.code() + ", message=" + getMessage() + '}';
	}
}
