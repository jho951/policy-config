# Modules

`policy-config`는 멀티모듈 Gradle 프로젝트다. 단, 각 모듈은 별도 아키텍처 계층이 아니라 같은 1계층 라이브러리 안의 배포/책임 경계다.

## 모듈 목록

| Module | Responsibility | Artifact |
| --- | --- | --- |
| `policy-config-contracts` | `PolicyKey`, `PolicyResolver`, `PolicyResolution` 같은 공개 계약 | `io.github.jho951:policy-config-contracts` |
| `policy-config-core` | 설정 소스 조회, 타입 변환, 기본값/별칭/검증/갱신 처리 | `io.github.jho951:policy-config-core` |
| `policy-config-builder` | 여러 `ConfigSource`를 조립해 `PolicyResolver`를 만드는 빌더 | `io.github.jho951:policy-config-builder` |

## 의존 관계

- `policy-config-core` -> `policy-config-contracts`
- `policy-config-builder` -> `policy-config-contracts`, `policy-config-core`

의존 방향은 API 안정성과 배포 단위를 지키기 위한 규칙이다. 서비스 계층, 인프라 계층, 프레임워크 어댑터 계층을 만들기 위한 분리가 아니다.

## 공개 API 원칙

- `policy-config-contracts`는 외부에 노출할 계약만 유지한다.
- `policy-config-core`는 외부 프레임워크에 의존하지 않는다.
- `policy-config-builder`는 조립 책임만 가진다.
- 서비스 전용 규칙은 어떤 모듈에도 넣지 않는다.
- Spring, DB, HTTP, 캐시 등 운영 환경 연동은 이 저장소의 모듈로 추가하지 않는다.
