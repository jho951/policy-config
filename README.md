# policy-config

`policy-config`는 **환경변수 / System Properties / .properties / Map** 등 여러 소스에 흩어진 값을  
`PolicyKey<T>`로 **타입 안전하게 조회**하기 위한 공통 모듈입니다.

- **api**: `PolicyKey`, `PolicyResolver`
- **core**: `ConfigSource`, 기본 소스(Env/Properties/Map), `DefaultPolicyResolver`
- **config**: 조립(구성) 유틸 `PolicyConfigs` (일반적으로 이 모듈을 의존하면 편합니다)

---

## 의존성 (GitHub Packages)

예시(Gradle):

```gradle
dependencies {
    implementation("io.github.jho951:policy-config-config:1.0.0")
}
```

> `policy-config-config` 는 내부적으로 `core` / `api` 를 포함합니다.

---

## 사용 예시

### 1) 타입이 있는 정책 키 정의

```java
import com.policyconfig.api.PolicyKey;

public final class MyPolicies {
    private MyPolicies() {}

    public static final PolicyKey<Long> FILE_MAX_SIZE =
        PolicyKey.<Long>builder("filestorage.maxSizeBytes", Long.class)
            .defaultValue(50_000_000L)
            .build();

    public static final PolicyKey<Boolean> FEATURE_X =
        PolicyKey.<Boolean>builder("feature.x.enabled", Boolean.class)
            .defaultValue(false)
            .build();
}
```

### 2) Resolver 구성 후 조회

```java
import com.policyconfig.api.PolicyResolver;
import com.policyconfig.config.PolicyConfigs;

import java.util.Map;

PolicyResolver resolver = PolicyConfigs.builder()
    .env()
    .systemProperties()
    // 마지막에 추가한 소스가 우선순위가 높습니다(override)
    .map(Map.of("feature.x.enabled", "true"))
    .build();

boolean enabled = resolver.require(MyPolicies.FEATURE_X);
long max = resolver.get(MyPolicies.FILE_MAX_SIZE); // 없으면 defaultValue 사용
```

---

## 버전 정책(v1)

- v1은 **고정 로드 / 고정 파싱**을 기본으로 합니다.
- 리로드(파일 변경 감지 등)는 v2+에서 별도 확장 포인트로 추가하는 것을 권장합니다.
