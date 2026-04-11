# Architecture

`policy-config`는 다양한 설정 소스에서 정책 값을 읽어오는 1계층 OSS다.

## 원칙

- 정책 설정 조회는 라이브러리 책임이다.
- 서비스는 조회 결과를 소비한다.
- 구현은 공개 계약을 재정의하지 않는다.
- 핵심 조회 로직은 프레임워크에 의존하지 않는다.

## 구성

- `policy-config-contracts`
- `policy-config-core`
- `policy-config-builder`

## 동작 흐름

1. `ConfigSource`가 원시 값을 제공한다.
2. `PolicyResolver`가 소스 우선순위를 적용해 값을 찾는다.
3. `PolicyKey<T>` 기준으로 타입 변환을 수행한다.
4. 기본값, 별칭, 검증 규칙을 적용한다.
5. 필요하면 `inspect()`로 소스 정보와 매칭 정보를 노출한다.

## 1계층 OSS 기준

- 특정 서비스 도메인에 종속되지 않는다.
- 정책 실행 엔진이 아니라 조회/해석 엔진이다.
- Spring은 포함하지 않는다.
- 조립은 `policy-config-builder`가 맡고, 계약은 `policy-config-contracts`가 맡는다.
