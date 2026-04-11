# Source Resolution

`policy-config`는 여러 설정 소스에서 값을 읽을 때 마지막에 추가한 source를 더 높은 우선순위로 본다.

## 우선순위

1. 가장 나중에 등록한 `ConfigSource`
2. 그 이전에 등록한 `ConfigSource`
3. 기본값

## 해석 규칙

- `PolicyKey<T>`의 이름을 먼저 찾는다.
- 별칭이 있으면 별칭도 함께 확인한다.
- 값이 있으면 타입 변환을 수행한다.
- 검증 규칙이 있으면 통과해야 한다.
- 값이 없으면 `defaultValue`를 사용한다.

## 예시

```java
PolicyResolver resolver = PolicyConfigs.builder()
    .env()
    .systemProperties()
    .map(Map.of("feature.my-feature.enabled", "true"))
    .build();
```

위 예시에서는 `map`이 `systemProperties`보다 높고, `systemProperties`가 `env`보다 높다.
