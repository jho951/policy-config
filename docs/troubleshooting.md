# 트러블슈팅

`policy-config`의 1계층 내부에서 자주 만나는 문제와 확인 순서입니다.

## 1. `Required policy key missing`가 발생한다

- `PolicyResolver` 인터페이스 기본 구현은 `PolicyResolutionException`을 던집니다.
- `policy-config-core`의 `DefaultPolicyResolver`와 `ReloadablePolicyResolver`는 `PolicyConfigException(POLICY001)`을 던집니다.
- 먼저 `PolicyKey` 이름이 실제 source에 존재하는지 확인합니다.
- 별칭을 쓰는 경우 `alias(...)`가 올바르게 등록됐는지 확인합니다.

## 2. 값이 있는데도 다른 값이 나온다

- 마지막에 등록한 `ConfigSource`가 더 높은 우선순위를 가집니다.
- 같은 key가 여러 source에 있으면 나중에 들어온 값이 덮어씁니다.
- raw value가 blank면 값이 없는 것으로 취급됩니다.
- `inspect(...)`로 `sourceName`, `matchedName`, `rawValue`를 확인합니다.

## 3. `INVALID_POLICY_VALUE`가 나온다

- 숫자, boolean, `BigDecimal`, `Duration`, enum 파싱이 실패했을 가능성이 큽니다.
- 예: `int`, `long`, `boolean`, `1h`, `30m`, `true/false`, enum name.
- raw 문자열에 공백, 따옴표, 잘못된 구분자가 섞였는지 확인합니다.
- 커스텀 converter를 쓰는 경우 변환 예외가 그대로 올라올 수 있습니다.

## 4. `UNSUPPORTED_POLICY_TYPE`가 나온다

- `String`, `Integer`, `Long`, `Boolean`, `BigDecimal`, `Duration`, `List`, `Set`, enum 외 타입은 기본 지원이 아닙니다.
- 커스텀 타입은 `PolicyConfigs.builder().converter(...)`로 직접 등록해야 합니다.
- `PolicyKey.builder(..., SomeType.class)`에 넣은 `Class`가 실제 converter와 같은지 확인합니다.

## 5. validator 때문에 조회가 실패한다

- `PolicyKey.validator(...)`에 등록한 검증기가 예외를 던지면 조회도 실패합니다.
- 기본값이 있어도 기본값이 validator를 통과해야 합니다.
- 값 자체는 맞는데도 실패하면 validator 조건을 먼저 봅니다.

## 6. `.properties` 파일이 안 읽히거나 refresh가 안 된다

- `PropertiesFileConfigSource`는 UTF-8 파일을 읽습니다.
- 파일 경로가 잘못됐거나 parent directory가 없으면 로딩 또는 watch가 실패합니다.
- 변경 감시는 `reloadable()`로 생성한 `ReloadablePolicyResolver`에서만 동작합니다.
- 파일 수정 후에도 반영되지 않으면 `inspectPrevious(...)`와 `refresh()` 호출 흐름을 확인합니다.

## 7. `SOURCE_LOAD_FAILED` 또는 `SOURCE_WATCH_FAILED`가 발생한다

- 파일 읽기 권한이 없거나 경로가 존재하지 않을 수 있습니다.
- watch는 OS의 `WatchService` 동작에 영향을 받습니다.
- 파일을 다른 방식으로 교체하는 경우 수정 이벤트가 안 잡힐 수 있습니다.
- 문제가 계속되면 `PropertiesFileConfigSource` 대신 `Map` 또는 다른 source로 검증합니다.

## 8. `displayValue`가 `***`로 보인다

- `PolicyKey.sensitive(true)`인 경우 민감 값을 마스킹합니다.
- 실제 값은 `PolicyResolution.value()`나 source 원문을 확인해야 합니다.
- 로그나 문자열 출력에서는 민감 값을 직접 노출하지 않는 것이 의도입니다.

## 9. 커스텀 converter가 적용되지 않는다

- `PolicyConfigs.builder().converter(...)`를 호출했는지 확인합니다.
- 등록한 `Class<T>`와 `PolicyKey`의 타입이 완전히 같은지 확인합니다.
- builtin converter가 먼저 처리하는 타입은 커스텀 converter로 덮이지 않습니다.

## 10. 먼저 확인할 파일

- [`policy-config-core/src/main/java/com/policyconfig/resolvercore/source/DefaultPolicyResolver.java`](../policy-config-core/src/main/java/com/policyconfig/resolvercore/source/DefaultPolicyResolver.java)
- [`policy-config-core/src/main/java/com/policyconfig/resolvercore/source/ReloadablePolicyResolver.java`](../policy-config-core/src/main/java/com/policyconfig/resolvercore/source/ReloadablePolicyResolver.java)
- [`policy-config-core/src/main/java/com/policyconfig/resolvercore/source/PropertiesFileConfigSource.java`](../policy-config-core/src/main/java/com/policyconfig/resolvercore/source/PropertiesFileConfigSource.java)
- [`policy-config-core/src/main/java/com/policyconfig/resolvercore/source/PolicyConverterRegistry.java`](../policy-config-core/src/main/java/com/policyconfig/resolvercore/source/PolicyConverterRegistry.java)
- [`policy-config-contracts/src/main/java/com/policyconfig/contracts/PolicyKey.java`](../policy-config-contracts/src/main/java/com/policyconfig/contracts/PolicyKey.java)
- [`policy-config-contracts/src/main/java/com/policyconfig/contracts/PolicyResolver.java`](../policy-config-contracts/src/main/java/com/policyconfig/contracts/PolicyResolver.java)
