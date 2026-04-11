package com.policyconfig.contracts;

/** {@link PolicyKey} 기준 정책 값 조회 계약. */
public interface PolicyResolver {

	/** 해석 결과를 반환합니다. */
	<T> PolicyResolution<T> inspect(PolicyKey<T> key);

	/** 필수 값을 반환합니다. */
	default <T> T require(PolicyKey<T> key) {
		PolicyResolution<T> resolution = inspect(key);
		if (!resolution.present()) {
			throw new PolicyResolutionException("Required policy key missing: " + key.getName());
		}
		return resolution.value();
	}

	/** 선택 값을 반환합니다. */
	default <T> T get(PolicyKey<T> key) {
		return inspect(key).value();
	}
}
