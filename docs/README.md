# Docs

이 디렉터리는 `policy-config`의 구현, 모듈 구조, 확장, 테스트, 트러블슈팅 문서를 모아둡니다.

## 먼저 읽기

1. [아키텍처](./architecture.md)
2. [모듈 가이드](./modules.md)
3. [확장 가이드](./extension-guide.md)
4. [소스 해석](./source-resolution.md)

### 문제를 만났을 때

1. [트러블슈팅](./troubleshooting.md)

### 모듈과 테스트

1. [테스트/CI 가이드](./test-and-ci.md)

## 읽는 순서

- 공개 설정 계약은 `policy-config-contracts` 모듈을 봅니다.
- 처음 보는 사람은 `아키텍처`와 `모듈 가이드`부터 읽는다.
- `ConfigSource`를 직접 구현하는 경우 `확장 가이드`를 먼저 본다.
- 값이 어떤 우선순위로 해석되는지 보려면 `소스 해석`을 본다.
- 테스트와 CI는 `테스트/CI 가이드`를 봅니다.
- 운영 중 문제는 `트러블슈팅`을 봅니다.
