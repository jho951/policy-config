package com.policyconfig.resolvercore.exception;

/**
 * 정책 값 조회/해석 런타임에서 사용하는 에러 코드.
 * v1 기준: 모듈/프레임워크에 종속되지 않는 공통 오류만 정의합니다.
 */
public enum ErrorCode {

	// -----------------------------
	// 정책/설정 관련
	// -----------------------------
	REQUIRED_POLICY_MISSING("POLICY001", "필수 정책 값이 없습니다."),
	INVALID_POLICY_VALUE("POLICY002", "정책 값 파싱에 실패했습니다."),
	UNSUPPORTED_POLICY_TYPE("POLICY003", "지원하지 않는 정책 타입입니다."),
	SOURCE_LOAD_FAILED("POLICY004", "정책 소스 로딩에 실패했습니다."),
	SOURCE_WATCH_FAILED("POLICY005", "정책 소스 변경 감시에 실패했습니다.");

	private final String code;
	private final String defaultMessage;

	ErrorCode(String code, String defaultMessage) {
		this.code = code;
		this.defaultMessage = defaultMessage;
	}

	public String code() {
		return code;
	}

	/** 기본 메시지. 로그용/디폴트 응답용으로 사용. */
	public String defaultMessage() {
		return defaultMessage;
	}

	@Override
	public String toString() {
		return code + " - " + defaultMessage;
	}
}
