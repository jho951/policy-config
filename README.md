# policy-config

`policy-config`는 로그인 제한 횟수, 토큰 TTL, rate limit 같은 정책성 설정값을 타입 안전하게 조회하고 검증하는 Java 17 기반 순수 1계층 OSS 라이브러리입니다.
핵심 개념, 입력/출력 모델, 인터페이스 계약, 해석 규칙을 한 묶음으로 제공합니다.

[![Build](https://github.com/jho951/policy-config/actions/workflows/build.yml/badge.svg)](https://github.com/jho951/policy-config/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.jho951/policy-config-core?label=maven%20central)](https://central.sonatype.com/search?q=jho951)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](./LICENSE)
[![Tag](https://img.shields.io/github/v/tag/jho951/policy-config)](https://github.com/jho951/policy-config/tags)

## 공개 좌표

- `io.github.jho951:policy-config-core`
- `io.github.jho951:policy-config-contracts`
- `io.github.jho951:policy-config-builder`

## 책임 경계

- `policy-config-contracts`는 외부에 노출되는 계약만 정의합니다. `PolicyKey`, `PolicyResolver`, `PolicyResolution`, converter/validator 인터페이스처럼 구현과 분리되어야 하는 타입을 둡니다.
- `policy-config-core`는 정책 값을 읽고 해석하는 순수 로직만 담당합니다. `ConfigSource`, 기본 resolver, 타입 변환, 기본값, 별칭, 검증, refresh 처리를 포함하며 Spring, DB, HTTP 같은 운영 어댑터에 의존하지 않습니다.
- `policy-config-builder`는 여러 설정 소스와 converter를 조립해 `PolicyResolver`를 만들기 위한 편의 API만 제공합니다. 해석 규칙을 새로 정의하거나 서비스 전용 정책을 넣지 않습니다.
- 서비스별 key 설계, 외부 저장소 연동, fallback 전략, 캐시/관측성/프레임워크 통합은 이 저장소의 책임이 아닙니다.

## 빠른 시작

```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.jho951:policy-config-core:<version>")
    implementation("io.github.jho951:policy-config-contracts:<version>")
    implementation("io.github.jho951:policy-config-builder:<version>")
}
```

## 문서

- [docs/README.md](docs/README.md)
