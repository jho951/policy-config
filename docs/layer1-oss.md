# policy-config 1계층 OSS 정의

`policy-config`는 1계층에서 **정책 엔진**이 아니라 **정책 값 조회/해석 OSS**입니다.
여러 설정 소스에 흩어진 값을 `PolicyKey<T>` 기준으로 타입 안전하게 읽고,
기본값, 별칭, 검증, 변환, 출처 정보를 포함해 해석합니다.

## 한 줄 정의

> 여러 설정 소스에 흩어진 정책 값을 `PolicyKey<T>` 기준으로 타입 안전하게 읽고, 기본값·별칭·검증·변환·출처 정보를 포함해 해석해주는 범용 OSS

## 책임

- 정책 결정을 하지 않는다.
- 정책 값을 읽고 해석한다.
- 해석 결과에 값, 출처, 매칭 정보를 포함할 수 있다.
- 기본은 snapshot 기반이다.
- 갱신은 선택적이다.
- 프레임워크 종속은 핵심 책임이 아니다.

## 제공 기능

- 여러 소스(env, system properties, `.properties`, `Map`)를 하나의 `PolicyResolver`로 조회한다.
- `PolicyKey<T>` 기준으로 타입 안전하게 읽는다.
- `defaultValue`, `alias`, `validator`, `converter`를 적용한다.
- 소스 우선순위를 적용한다. 마지막에 추가한 source가 더 높다.
- `inspect()`로 `sourceName`, `matchedName`, `displayValue`를 제공한다.
- `ReloadablePolicyResolver`로 snapshot 갱신을 지원한다.
- `contracts`, `resolver-core`, `builder`만으로 재사용 가능하다.
- `spring-boot-starter`는 조립과 관찰성만 추가한다.

## 모듈

### contracts

- `PolicyKey`
- `PolicyResolver`
- `PolicyResolution`
- `PolicyKeyRegistry`
- 외부 공개 계약

### resolver-core

- `ConfigSource`
- Env / Properties / Map 기본 source
- `DefaultPolicyResolver`
- `ReloadablePolicyResolver`
- type conversion / default / alias / validation / inspect

### builder

- `PolicyConfigs`
- resolver 조립 유틸

### spring-boot-starter

- Spring Boot auto-config
- `PolicyResolver` bean 등록
- `PolicyConverterBinding` 연동
- actuator endpoint / refresh / diff

### example / docs

- 예제와 책임 경계 문서

## 하지 않는 것

- 사용자/조직/테넌트 기반 정책 결정
- rollout, A/B test, percentage split 같은 실행 정책 평가
- 특정 서비스 전용 키나 도메인 규칙 하드코딩
- Redis/DB/Kubernetes ConfigMap 같은 특정 저장소 강제
- 중앙 운영 콘솔이나 원격 제어 plane 역할
- 인증/인가 판단 자체

## 경계

- `contracts`는 계약입니다.
- `resolver-core`는 해석 핵심 런타임입니다.
- `builder`는 조립 유틸입니다.
- `spring-boot-starter`는 프레임워크 어댑터입니다.
- `policy-config` 자체는 범용 정책 값 조회 OSS로 남습니다.
- 실제 정책 의미와 서비스 표준은 상위 계층이 가집니다.
