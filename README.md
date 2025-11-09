# JSON DTO Converter

JSON 형식의 데이터를 기반으로 **Java DTO 클래스를 자동 생성**해주는 프로그램입니다.  
API 응답(JSON)을 일일이 수동으로 파싱하지 않고, 자동으로 Java 클래스를 생성하여  
개발 효율을 높이는 것을 목표로 합니다.

## 프로젝트 소개

### 개발 배경
외부 API를 활용할 때마다 매번 JSON 구조를 분석하고, 그에 맞는 DTO 클래스를 직접 작성하는 과정은 반복적이고 번거롭습니다.  
이 프로젝트는 JSON의 구조를 자동으로 분석하여 DTO 클래스를 생성함으로써,  
**개발자가 로직 구현에 집중할 수 있도록 돕기 위해** 만들어졌습니다.

### 주요 목표
- JSON 응답을 입력하면 해당 구조에 맞는 Java DTO 파일(.java)을 자동 생성
- JSON의 key 이름을 camelCase로 변환하여 필드명에 적용
- 중첩된 JSON 객체(`{}`)는 내부 클래스로, 배열(`[]`)은 List 필드로 매핑
- 간단한 입력/출력 인터페이스 제공 (CLI 기반)

### 기능 목록

| 구분 | 기능 | 설명 |
|------|------|------|
| 입력 처리 | JSON 파일 또는 문자열 입력 | 사용자가 JSON을 입력하면 파싱 준비 |
| 파싱 | JSON 구조 파악 | key-value, 객체, 배열 구조를 계층적으로 분석 |
| 클래스 변환 | DTO 생성 | PascalCase → camelCase 변환, 내부 클래스 처리 |
| 파일 출력 | .java 파일 생성 | DTO 코드를 자동으로 파일로 저장 |
| 예외 처리 | 유효하지 않은 JSON 입력 | 잘못된 형식 입력 시 예외 메시지 출력 후 재입력 요청 |


## 요구사항 정의

### 실행 방법

```bash
java -jar json-dto-converter.jar ^
  --input <path/to/sample.json> ^
  --root-class <RootClassName> ^
  --package <com.example.dto> ^
  --out <build/generated> ^
  --inner-classes <true/false>
```

### CLI 인자 목록

| 인자 | 필수 | 기본값 | 설명 |
|------|------|--------|------|
| `--input` | O | - | 변환할 JSON 파일 경로 |
| `--root-class` | O | - | 루트 DTO 클래스명 |
| `--package` | O | - | 생성 클래스의 Java 패키지명 |
| `--out` | O | - | 생성된 DTO 파일 저장 경로 |
| `--inner-classes` | X | `false` | `true`면 이너클래스로 단일 파일 생성 |


### 입출력 형식

#### 입력
- **형식**: JSON (UTF-8)
- **예시 (`weatherapi.json`)**

```json
{
    "location": {"name": "Seoul", "country": "KR"},
    "current": {"temp_c": 21.3, "humidity": 63}
}
```

#### 출력
- **출력 위치**: `--out` 경로 기준
- **예시 (--inner-classes true 시 단일 파일)**

**WeatherApiResponse.java**
```java
package com.team606.mrdinner.entity;

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

### 실행 예시

#### 예시 명령어
```bash
java -jar json-dto-converter.jar ^
  --input weatherapi.json ^
  --root-class WeatherApiResponse ^
  --package com.team606.mrdinner.entity ^
  --out "C:\Users\hsj9433\Desktop\mrdinner\backend\src\main\java\com\team606\mrdinner\entity" ^
  --inner-classes true
