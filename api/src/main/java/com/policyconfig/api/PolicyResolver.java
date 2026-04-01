package com.policyconfig.api;

/**
 * PolicyKey 기반으로 설정값을 조회하는 인터페이스.
 * - 실제 값이 없으면 defaultValue 를 사용하거나 예외를 던질 수 있다.
 */
public interface PolicyResolver {

	/**
	 * 정책 값을 해석한 상세 결과를 반환한다.
	 */
	<T> PolicyResolution<T> inspect(PolicyKey<T> key);

	/**
	 * 필수 값 조회. 값이 없거나 파싱에 실패하면 PolicyResolutionException 또는 구현체 예외.
	 */
	default <T> T require(PolicyKey<T> key) {
		PolicyResolution<T> resolution = inspect(key);
		if (!resolution.present()) {
			throw new PolicyResolutionException("Required policy key missing: " + key.getName());
		}
		return resolution.value();
	}

	/**
	 * 선택 값 조회. 값이 없으면 key 에 정의된 defaultValue 를 사용.
	 * defaultValue 가 없고 값도 없으면 null 반환.
	 */
	default <T> T get(PolicyKey<T> key) {
		return inspect(key).value();
	}
}
