# 테스트/CI 가이드

## 로컬 테스트 실행

전체 빌드:

```bash
./gradlew clean build
```

전체 테스트:

```bash
./gradlew test
```

모듈 단위 테스트:

```bash
./gradlew :policy-config-contracts:test
./gradlew :policy-config-core:test
./gradlew :policy-config-builder:test
```

## 현재 테스트 범위

- `policy-config-contracts`
  - `PolicyKeyTest`
- `policy-config-core`
  - `DefaultPolicyResolverTest`
  - `PropertiesFileAutoRefreshTest`
  - `ReloadablePolicyResolverTest`
- `policy-config-builder`
  - `PolicyConfigsTest`

## GitHub Actions

현재 워크플로우 파일:

- `.github/workflows/build.yml`
- `.github/workflows/publish.yml`

### `build.yml`

- 트리거: `main` 대상 PR, `main` push
- 수행: `./gradlew clean test --no-daemon --stacktrace`

### `publish.yml`

- 트리거: `v*` 태그 push
- 수행:
  1. `./gradlew test --no-daemon --stacktrace`
  2. `./gradlew -Prelease_version="$VERSION" publishAggregationToCentralPortal --no-daemon --stacktrace`
  3. Maven Central에 배포

## 참고

CI와 문서는 소스 트리 기준으로 설명합니다. generated build 산출물은 문서 기준이 아닙니다.
