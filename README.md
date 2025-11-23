# JSON DTO Converter

JSON 형식의 데이터를 기반으로 **Java DTO 클래스를 자동 생성**해주는 CLI 프로그램입니다.  
외부 API 응답(JSON)을 일일이 손으로 DTO로 옮겨 적지 않고,  
**JSON 구조를 분석 → Java 클래스 코드 생성 → .java 파일로 저장**하는 과정을 자동화합니다.

---

## 1. 프로젝트 소개

### 1-1. 개발 배경

외부 REST API를 사용할 때마다 다음과 같은 작업을 반복하게 됩니다.

1. JSON 응답 예시를 확인한다  
2. 각 필드의 타입과 중첩 구조를 파악한다  
3. Java에서 사용할 DTO 클래스를 직접 작성한다  

이 과정은 **반복적이고 실수가 발생하기 쉬운 작업**입니다.  
이 프로젝트는 JSON을 한 번만 제공하면, 그 구조를 분석하여 **DTO 클래스를 자동 생성**함으로써  
개발자가 **비즈니스 로직 구현에 더 집중**할 수 있도록 돕기 위해 만들어졌습니다.

### 1-2. 주요 목표

- JSON 응답을 입력하면 해당 구조에 맞는 **Java DTO 파일(.java)을 자동 생성**
- JSON key 이름을 camelCase로 변환하여 필드명에 적용
- 중첩된 JSON 객체(`{}`)는 별도 클래스 또는 이너 클래스(`--inner-classes` 옵션)로 생성
- JSON 배열(`[]`)은 `List<T>` 필드로 매핑
- 단순하고 명시적인 **CLI 기반 인터페이스** 제공

---

## 2. 기능 목록


| 구분       | 기능                      | 설명 |
|-----------|---------------------------|------|
| 입력 처리 | **JSON 파일 입력**        | `--input`으로 지정한 JSON 파일을 UTF-8로 읽어들임 |
| 검증 & 파싱 | JSON 유효성 검사 및 파싱  | 파일 존재 여부, 크기(5MB 이하), JSON 문법, 루트 타입 등을 검증 후 파싱 |
| 구조 분석 | JSON 구조 분석            | 객체/배열/기본 타입을 계층적으로 분석하여 내부 모델(Schema)로 변환 |
| 타입 추론 | Java 타입 추론            | `double`, `boolean`, `String`, `List<T>`, 사용자 정의 클래스 등으로 매핑 |
| 클래스 생성 | DTO 클래스 코드 생성      | PascalCase 클래스명, camelCase 필드명, optional 여부 반영 |
| 파일 출력 | `.java` 파일 생성         | 지정한 출력 디렉터리(`--out`) 아래에 Java 소스 파일 생성 (필요 시 디렉터리 자동 생성) |
| 예외 처리 | 사용자 입력 오류 처리     | 잘못된 CLI 옵션, 경로 오류, JSON 포맷 오류 등 발생 시 **명확한 에러 메시지** 출력 |

> 🔎 **입력은 현재 "파일 입력"만 지원**합니다.  
> JSON 문자열을 직접 CLI 인자로 넘기는 방식은 지원하지 않습니다.

---

## 3. 실행 방법 (Windows 기준 예시)

### 3-1. 사전 준비물

- **Java 21** 설치 (JDK)
- 인터넷 연결 (처음 `gradlew` 실행 시 Gradle Wrapper가 필요한 의존성을 내려받습니다)
- 변환할 JSON 파일 (예: `weatherapi.json`)

### 3-2. 빠른 실행 흐름

1. **변환할 JSON 파일 준비**

   예시로, 바탕화면에 JSON 파일을 저장했다고 가정합니다.

   - 예시 경로:  
     `C:\Users\user\Desktop\weatherapi.json`

2. **레포지토리 클론**

   ```bash
   git clone https://github.com/abroniasj416/json-dto-converter.git
   cd json-dto-converter
   ```

3. **빌드 (Gradle Wrapper 사용)**

   - Windows CMD / PowerShell:

     ```bash
     .\gradlew clean build
     ```

   - macOS / Linux:


4. **빌드 결과(.jar) 위치 확인**

   ```bash
   cd build\libs
   dir
   ```

   예시:

   - `json-dto-converter-1.0-SNAPSHOT.jar` 파일이 생성되어 있어야 합니다.

5. **실행 명령어 (예시)**

   ```bash
   java -jar json-dto-converter-1.0-SNAPSHOT.jar ^
     --input C:\Users\user\Desktop\weatherapi.json ^
     --root-class WeatherApiResponse ^
     --package com.org.weather.dto ^
     --out C:\Users\user\Desktop\weather-alarm\backend\src\main\java\com\org\weather\dto ^
     --inner-classes false
   ```

   - 위 예시는 **Windows CMD** 기준이며, `^`는 줄바꿈 이어쓰기용입니다.
   - 한 줄로 작성해도 동작합니다.

   ```bash
   java -jar json-dto-converter-1.0-SNAPSHOT.jar --input C:\Users\user\Desktop\weatherapi.json --root-class WeatherApiResponse --package com.org.weather.dto --out C:\Users\user\Desktop\weather-alarm\backend\src\main\java\com\org\weather\dto --inner-classes false
   ```

6. **결과 확인**

   명령어가 정상적으로 완료되면, 아래 경로에 `.java` 파일들이 생성됩니다.

   - 예시 경로:  
     `C:\Users\user\Desktop\weather-alarm\backend\src\main\java\com\org\weather\dto`

---

## 4. CLI 인자 상세 설명

> 📌 **중요:**  
> 모든 옵션은 `--option value` 형식이어야 합니다.  
> `--inner-classes=true` 같은 **`=` 형식은 지원하지 않습니다.**  
> (예: ✅ `--inner-classes true`, ❌ `--inner-classes=true`)

### 4-1. 옵션 목록

| 인자             | 필수 | 기본값  | 예시                                | 설명 |
|------------------|------|---------|-------------------------------------|------|
| `--input`        | O    | -       | `C:\Users\user\Desktop\weatherapi.json` | 변환할 JSON 파일 경로 (상대경로도 허용되지만 가능하면 **절대경로** 사용 권장) |
| `--root-class`   | O    | -       | `WeatherApiResponse`               | 루트 DTO 클래스명 (Java 클래스 이름 규칙을 따르는 PascalCase 권장) |
| `--package`      | O    | -       | `com.org.weather.dto`              | 생성될 클래스들의 Java 패키지명 (`package` 구문에 그대로 사용) |
| `--out`          | O    | -       | `C:\Users\user\Desktop\weather-alarm\backend\src\main\java\com\org\weather\dto` | 생성된 `.java` 파일을 저장할 디렉터리 경로. 없으면 **자동 생성** |
| `--inner-classes`| X    | `false` | `true` / `false`                   | `true`면 루트 클래스 내부에 **static 이너 클래스**로 중첩 생성. `false`면 각 클래스를 **별도 파일**로 생성 |

### 4-2. 옵션 별 동작 정리

- `--input`
  - JSON 파일을 읽어서 검증합니다.
  - 파일이 없거나 권한이 없거나 JSON 형식이 잘못된 경우, **명확한 에러 메시지**를 출력하고 종료합니다.
- `--root-class`
  - JSON 루트 객체를 표현하는 **최상위 클래스 이름**입니다.
  - 예: `WeatherApiResponse`, `NewsResponse`, `UserProfile` 등.
- `--package`
  - 생성되는 모든 클래스의 `package` 구문에 사용됩니다.
  - 예: `com.team606.mrdinner.entity`, `com.org.weather.dto`
- `--out`
  - `.java` 파일이 생성될 디렉터리입니다.
  - 존재하지 않으면 디렉터리를 자동으로 생성합니다.
  - 파일이 아닌 경로거나, 쓰기 권한이 없으면 에러를 발생시킵니다.
- `--inner-classes`
  - `true`:
    - 루트 클래스 하나만 `.java` 파일로 생성되고,
    - 중첩된 JSON 객체들은 **루트 클래스 안의 static inner class**로 생성됩니다.
  - `false`:
    - 각 JSON 객체 구조마다 **별도의 top-level 클래스**로 분리되어,
    - 여러 개의 `.java` 파일이 `--out` 디렉터리에 생성됩니다.

> ✅ **팁:**  
> 되도록 모든 경로는 **절대경로**로 작성하는 것을 권장합니다.  
> 상대경로를 사용할 경우, **현재 명령어를 실행한 디렉터리**를 기준으로 해석되므로  
> 사용자가 작업 디렉터리를 헷갈리면 경로 오류가 발생할 수 있습니다.

---

## 5. 입출력 예시

### 5-1. 입력(JSON) 예시

- 파일명: `weatherapi.json`  
- 경로 예시: `C:\Users\user\Desktop\weatherapi.json`

```json
{
  "location": { "name": "Seoul", "country": "KR" },
  "current": { "temp_c": 21.3, "humidity": 63 }
}
```

### 5-2. 출력(Java 클래스) 예시

- 옵션: `--inner-classes true` 인 경우  
- 출력 파일: `WeatherApiResponse.java` (단일 파일)

```java
package com.org.weather.dto;

public class WeatherApiResponse {
    private Location location;
    private Current current;

    public static class Location {
        private String name;
        private String country;
    }

    public static class Current {
        private double tempC;
        private int humidity;
    }
}
```

---

## 6. 내부 동작 개요 (간단 버전)

> 우테코 코치/리뷰어 분들을 위해, 내부 구조를 간략히 정리했습니다.

1. **JsonValidator**
   - 파일 존재 여부, 크기(5MB 이하), 읽기 가능 여부를 확인합니다.
   - BOM(Byte Order Mark) 제거, JSON 문법 검증, 루트 타입 검사까지 수행합니다.

2. **JsonAnalyzer**
   - Jackson `JsonNode`를 순회하며 내부 도메인 모델인 `SchemaNode` 트리를 생성합니다.
   - 객체, 배열, 기본 타입, union 타입 등을 추상화합니다.

3. **TypeInferencer**
   - `SchemaNode` 트리를 순회하면서 각 필드의 Java 타입(`TypeRef`)을 결정합니다.
   - 숫자/문자열/불리언/배열/객체/nullable 등의 케이스를 분기 처리합니다.

4. **ClassGenerator**
   - 타입 정보에 기반해 `ClassSpec`, `FieldSpec`과 같은 중간 모델을 만들고,
   - Java 소스 코드 문자열로 렌더링합니다.

5. **FileWriter**
   - 최종 Java 소스 문자열을 `.java` 파일로 저장합니다.
   - 출력 디렉터리는 `--out` 기준으로 생성/검증합니다.

---

## 7. 제한 사항

- 입력 JSON 파일 크기는 **5MB 이하**를 가정합니다.
- 매우 복잡한 union 타입이나, 특수한 JSON 패턴에 대해선 생성 결과가 기대와 다를 수 있습니다.
- 현재는 **CLI 실행 + 파일 입력**만 지원하고,  
  IDE 플러그인이나 웹 UI는 제공하지 않습니다.

---
