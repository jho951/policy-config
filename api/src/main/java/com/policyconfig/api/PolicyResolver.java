package com.policyconfig.api;

/**
 * PolicyKey 기반으로 설정값을 조회하는 인터페이스.
 * - 실제 값이 없으면 defaultValue 를 사용하거나 예외를 던질 수 있다.
 */
public interface PolicyResolver {

	/**
	 * 필수 값 조회. 값이 없거나 파싱에 실패하면 PolicyValueException.
	 */
	<T> T require(PolicyKey<T> key);

	/**
	 * 선택 값 조회. 값이 없으면 key 에 정의된 defaultValue 를 사용.
	 * defaultValue 가 없고 값도 없으면 null 반환.
	 */
	<T> T get(PolicyKey<T> key);
}
