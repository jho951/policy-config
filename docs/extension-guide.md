# Extension Guide

## 새 ConfigSource 추가

새 설정 소스를 추가할 때는 원시 문자열 맵을 제공하는 구현을 만든다.

```java
public final class DbConfigSource implements ConfigSource {
    @Override
    public Map<String, String> load() {
        return Map.of("feature.my-feature.enabled", "true");
    }
}
```

권장 사항:

- 파싱은 하지 않는다.
- 실패 시에는 빈 맵을 반환하거나 명시적으로 예외를 던진다.
- 서비스 전용 키 설계를 넣지 않는다.

## 새 변환기 추가

새 타입 변환이 필요하면 `PolicyValueConverter`를 구현한다.

```java
public final class DurationConverter implements PolicyValueConverter<Duration> {
    @Override
    public Duration convert(String value) {
        return Duration.parse(value);
    }
}
```

## 새 조립 방식 추가

조립 방식을 추가할 때는 `builder`가 담당하는 범위 안에서 구성만 확장한다.

- 핵심 해석 로직은 `policy-config-core`에 남긴다.
- 계약 타입은 `policy-config-contracts`에 둔다.
- 조립 편의 메서드만 `policy-config-builder`에 넣는다.

## 검증

새 확장 추가 후에는 최소한 아래를 확인한다.

- 모듈 간 컴파일이 깨지지 않는지
- `PolicyResolver`의 기본 해석이 바뀌지 않는지
- 서비스 전용 규약에 묶이지 않는지
