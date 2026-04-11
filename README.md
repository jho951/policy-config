# policy-config

`policy-config`는 Java 17 기반의 정책 키와 설정 소스 조회 OSS 모듈입니다.
핵심 개념, 입력/출력 모델, 인터페이스 계약, 해석 규칙을 한 묶음으로 제공합니다.

[![Build](https://github.com/jho951/policy-config/actions/workflows/build.yml/badge.svg)](https://github.com/jho951/policy-config/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.jho951/policy-config-core?label=maven%20central)](https://central.sonatype.com/search?q=jho951)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](./LICENSE)
[![Tag](https://img.shields.io/github/v/tag/jho951/policy-config)](https://github.com/jho951/policy-config/tags)

## 공개 좌표

- `io.github.jho951:policy-config-core`
- `io.github.jho951:policy-config-contracts`

## 무엇을 제공하나

- `policy-config-contracts`: 공개 계약과 타입 안전한 정책 키 모델
- `policy-config-core`: 설정 소스 조회, 타입 변환, 기본값/별칭/검증/갱신 처리
- `policy-config-builder`: 여러 `ConfigSource`를 조립해 `PolicyResolver`를 만드는 빌더

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
