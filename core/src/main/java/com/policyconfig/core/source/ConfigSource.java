package com.policyconfig.core.source;

import java.util.Map;

/**
 * 설정 값을 key-value(Map) 형태로 제공하는 소스.
 * 예: 환경변수, Properties 파일, 시스템 프로퍼티 등.
 */
public interface ConfigSource {

	/**
	 * 이 소스에서 사용할 수 있는 모든 key-value를 반환.
	 * key 충돌이 있을 경우, 나중에 합치는 쪽에서 우선순위를 정한다.
	 */
	Map<String, String> load();
}
