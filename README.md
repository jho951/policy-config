# policy-config

`policy-config`는 **환경변수 / System Properties / .properties / Map** 등 여러 소스에 흩어진 값을  
`PolicyKey<T>`로 **타입 안전하게 조회**하기 위한 공통 모듈입니다.

- **api**: `PolicyKey`, `PolicyResolver`
- **core**: `ConfigSource`, 기본 소스(Env/Properties/Map), `DefaultPolicyResolver`, `ReloadablePolicyResolver`
- **config**: 조립(구성) 유틸 `PolicyConfigs` (일반적으로 이 모듈을 의존하면 편합니다)
- **spring**: Spring Boot 자동 설정 + Actuator endpoint 제공
- **example**: Spring 예제 모듈

---

## 의존성 (Maven Central)

예시(Gradle):

```gradle
dependencies {
    implementation("io.github.jho951:policy-config-config:1.0.0")
    implementation("io.github.jho951:policy-config-spring-boot-starter:1.0.0")
}
```

> `policy-config-config` 는 내부적으로 `core` / `api` 를 포함합니다.
> `policy-config-spring-boot-starter` 는 Spring Boot 애플리케이션에서 `PolicyResolver` 빈을 자동 등록합니다.
> `example` 모듈은 Maven Central에 배포하지 않습니다.

## Maven Central 배포

이 저장소는 Maven Central 배포용 메타데이터와 서명 설정을 포함합니다.

- `api`, `core`, `config`, `spring` 모듈만 배포 대상입니다.
- `example` 모듈은 실행 예제이므로 배포되지 않습니다.
- `semantic-release` 워크플로는 `main`에 push되면 릴리즈 PR을 엽니다.
- `publish-release` 워크플로는 릴리즈 PR이 머지되면 Maven Central 배포와 태그 생성, GitHub Release 생성을 수행합니다.
- 필요한 시크릿은 `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD`, `MAVEN_CENTRAL_GPG_PRIVATE_KEY`, `MAVEN_CENTRAL_GPG_PASSPHRASE` 입니다.
- 로컬 릴리즈는 `./gradlew publishToMavenCentral -PVERSION_NAME=1.0.0` 로 수행할 수 있습니다.
- `ci.yml`은 PR과 `main` 푸시에서 테스트를 돌립니다.
- `commit-lint.yml`은 PR 커밋 메시지가 conventional commit 규칙을 따르는지 검사합니다.
- 릴리즈 PR 커밋은 `feat`, `fix`, `perf`, `docs`, `refactor`, `test`, `build`, `ci`, `chore`, `style`, `revert` 형식이어야 합니다.
- 릴리즈 커밋은 `chore(release): v1.2.3` 형식만 허용합니다.

배포 전에 Maven Central namespace와 GPG 서명이 준비되어 있어야 합니다.

---

## 사용 예시

### 1) 타입이 있는 정책 키 정의

```java
import com.policyconfig.api.PolicyKey;

public final class MyPolicies {
    private MyPolicies() {}

    public static final PolicyKey<Long> FILE_MAX_SIZE =
        PolicyKey.<Long>builder("filestorage.maxSizeBytes", Long.class)
            .namespace("app")
            .description("파일 스토리지 최대 업로드 크기")
            .defaultValue(50_000_000L)
            .validator((key, value) -> {
                if (value < 1_000_000L) {
                    throw new IllegalArgumentException("too small");
                }
            })
            .build();

    public static final PolicyKey<Boolean> FEATURE_X =
        PolicyKey.<Boolean>builder("feature.x.enabled", Boolean.class)
            .alias("feature_x_enabled")
            .sensitive(false)
            .defaultValue(false)
            .build();
}
```

### 2) Resolver 구성 후 조회

```java
import com.policyconfig.api.PolicyResolver;
import com.policyconfig.config.PolicyConfigs;

import java.util.Map;
import java.util.UUID;

PolicyResolver resolver = PolicyConfigs.builder()
    .env()
    .systemProperties()
    // 마지막에 추가한 소스가 우선순위가 높습니다(override)
    .map(Map.of("feature.x.enabled", "true"))
    .converter(UUID.class, (key, raw) -> UUID.fromString(raw))
    .build();

boolean enabled = resolver.require(MyPolicies.FEATURE_X);
long max = resolver.get(MyPolicies.FILE_MAX_SIZE); // 없으면 defaultValue 사용
```

### 3) 값 해석 결과 확인

```java
var resolution = resolver.inspect(MyPolicies.FILE_MAX_SIZE);
resolution.value();
resolution.sourceName();  // 어떤 source에서 왔는지
resolution.matchedName(); // 실제로 매칭된 key
resolution.displayValue(); // sensitive key는 마스킹
```

### 4) reload 가능한 resolver

```java
var reloadable = PolicyConfigs.builder()
    .map(Map.of("feature.x.enabled", "false"))
    .buildReloadable();

// source 쪽 값이 바뀐 뒤 refresh() 호출
// (reloadable 구현체를 직접 보관하면 refresh 가능)
```

### 5) Spring Boot에서 사용

```java
import com.policyconfig.api.PolicyKey;
import com.policyconfig.api.PolicyResolver;
import org.springframework.stereotype.Service;

@Service
public class UploadService {
    private static final PolicyKey<Long> MAX_SIZE =
        PolicyKey.<Long>builder("policy.upload.max-size-bytes", Long.class)
            .defaultValue(50_000_000L)
            .build();

    private final PolicyResolver policyResolver;

    public UploadService(PolicyResolver policyResolver) {
        this.policyResolver = policyResolver;
    }

    public long maxSize() {
        return policyResolver.require(MAX_SIZE);
    }
}
```

### 6) Spring Boot 설정

```properties
policy.config.prefix=policy.demo
policy.config.reloadable=true
```

커스텀 변환기 빈은 다음처럼 등록할 수 있습니다.

```java
import com.policyconfig.spring.PolicyConverterBinding;
import org.springframework.context.annotation.Bean;

@Bean
PolicyConverterBinding<java.util.UUID> uuidConverter() {
    return PolicyConverterBinding.of(java.util.UUID.class, (key, raw) -> java.util.UUID.fromString(raw));
}
```

### 7) Actuator endpoint

- endpoint id: `policy-config`
- `GET /actuator/policy-config`
- `POST /actuator/policy-config` 또는 `@WriteOperation`에 대응되는 액션으로 refresh
- `GET /actuator/policy-config?mode=diff`로 이전 스냅샷 대비 변경된 항목만 볼 수 있습니다.
- `PolicyKey`를 Spring bean으로 등록하면 endpoint가 `sensitive` 메타데이터를 반영합니다.
- `PolicyKey` bean은 자동으로 수집됩니다. 별도 레지스트리 코드는 필요 없습니다.
- `@PolicyKeyBean`을 붙이면 endpoint에서 `declared: true`로 표시됩니다.
- `sources` 섹션은 source별로 그룹화된 정책 항목을 보여줍니다.

응답 예시:

```json
{
  "resolverType": "ReloadablePolicyResolver",
  "registryCount": 4,
  "count": 12,
  "reloadable": true,
  "mode": "full",
  "summary": {
    "total": 4,
    "returned": 4,
    "defaulted": 1,
    "aliasHits": 1,
    "errorOrMissing": 0,
    "changedFromPrevious": 0
  },
  "entries": [
    {
      "name": "policy.demo.enabled",
      "description": null,
      "value": "true",
      "source": "SpringEnvironment",
      "sensitive": false,
      "defaulted": false,
      "matched": "direct",
      "aliasHit": false,
      "present": true,
      "status": "ok",
      "changedFromPrevious": false,
      "deltaStatus": "baseline"
    }
  ],
  "sources": [
    {
      "name": "SpringEnvironment",
      "count": 1,
      "entries": [
        {
          "name": "policy.demo.enabled",
          "value": "true"
        }
      ]
    }
  ]
}
```

---

## 버전 정책(v1)

- v1은 기본적으로 **고정 스냅샷**으로 동작합니다.
- 필요하면 `ReloadablePolicyResolver` 또는 `PolicyConfigs.builder().reloadable()`로 스냅샷 갱신을 사용할 수 있습니다.
- 커스텀 변환기는 `PolicyConfigs.builder().converter(type, converter)`로 등록할 수 있습니다.
- Spring Boot에서는 `policy-config-spring-boot-starter`를 의존성에 추가하면 `PolicyResolver` 빈이 자동 등록됩니다.
- Spring Boot 설정은 `policy.config.prefix`와 `policy.config.reloadable`로 제어할 수 있습니다.
- Actuator endpoint `policy-config`에서 현재 스냅샷과 origin을 확인할 수 있습니다.
- `PolicyKey`를 bean으로 등록하면 endpoint가 정의 메타데이터까지 함께 보여줍니다.
- `@PolicyKeyBean`이 붙은 bean은 endpoint에서 명시적 정책 키로 표시됩니다.
- `mode=diff`는 이전 스냅샷과 비교한 변경분만 반환합니다.
- `propertiesFile(...)`을 `reloadable` 모드로 쓰면 파일 변경 시 자동 refresh 됩니다.
