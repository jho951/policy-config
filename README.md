# policy-config

`policy-config`는 Java 17 기반의 정책 값 조회 OSS 모듈입니다.

[![Build](https://github.com/jho951/policy-config/actions/workflows/build.yml/badge.svg)](https://github.com/jho951/policy-config/actions/workflows/build.yml)
[![Publish](https://github.com/jho951/policy-config/actions/workflows/publish.yml/badge.svg)](https://github.com/jho951/policy-config/actions/workflows/publish.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.jho951/policy-config-spring-boot-starter?label=maven%20central)](https://central.sonatype.com/artifact/io.github.jho951/policy-config-spring-boot-starter)

## 공개 좌표

- `io.github.jho951:policy-config-contracts`
- `io.github.jho951:policy-config-resolver-core`
- `io.github.jho951:policy-config-builder`
- `io.github.jho951:policy-config-spring-boot-starter`

## SCM 메타데이터

- `github_org`: `jho951`
- `github_repo`: `policy-config`
- `pom.url`, `scm.connection`, `scm.developerConnection`은 위 두 키를 조합한다.

## 포함 범위

- 정책 값 조회 및 해석
- `PolicyKey<T>` 기반 타입 안전 조회
- Spring Boot 연동

## 빠른 시작

```gradle
dependencies {
    implementation("io.github.jho951:policy-config-spring-boot-starter:<version>")
}
```

## 버전 정책

- 기본 버전 SOT는 `gradle.properties`의 `version`입니다.
- 릴리스 시에만 `releaseVersion`으로 임시 override합니다.
- 태그 push 시 `releaseVersion`이 주입되어 Maven Central에 publish합니다.

Apache License 2.0. 자세한 내용은 [LICENSE](./LICENSE)를 보세요.
