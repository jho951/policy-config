# policy-config example

`policy-config`가 1계층 정책 값 조회/해석 OSS라는 전제를 바탕으로,
`@PolicyKeyBean`, `ConfigSource`, `PolicyConfigEndpoint`를 함께 쓰는 최소 구성을 보여줍니다.

## 30초 요약

```java
import com.policyconfig.builder.PolicyConfigs;
import com.policyconfig.contracts.PolicyKey;
import com.policyconfig.contracts.PolicyResolver;

PolicyKey<Boolean> featureKey = PolicyKey.<Boolean>builder("feature.x.enabled", Boolean.class)
    .defaultValue(false)
    .build();

PolicyResolver policyResolver = PolicyConfigs.builder()
    .env()
    .build();

boolean enabled = policyResolver.require(featureKey);
```

## 구성

- `ExampleApplication`: Spring Boot 진입점
- `ExamplePolicyConfiguration`: 정책 키와 source bean 예시
- `ExampleApplicationTest`: endpoint snapshot / diff / grouping 동작 예시

## 핵심 포인트

- `@PolicyKeyBean`을 붙이면 endpoint에서 명시적 정책 키로 표시됩니다.
- `ConfigSource` bean을 제공하면 starter가 자동으로 수집해서 resolver에 반영합니다.
- `mode=diff`는 이전 snapshot 대비 변경만 보여줍니다.
- 이 예제는 정책 결정 엔진이 아니라 정책 값 해석 계층의 사용법을 보여줍니다.

## 실행

- 예제 앱은 `./gradlew :example:run`으로 실행할 수 있습니다.
- actuator는 `GET http://localhost:8080/actuator/policy-config`로 확인할 수 있습니다.
