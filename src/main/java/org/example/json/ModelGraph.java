package org.example.json;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * ModelGraph
 *
 * <p>JSON 분석/타입 추론이 끝난 뒤, 실제로 생성할 Java 모델 클래스들을
 * 그래프 형태로 표현하는 불변 객체이다.</p>
 *
 * <p>노드(Node)는 Java 클래스(ModelClass)에 해당하고,
 * 엣지(Edge)는 "필드가 어떤 타입을 참조하는지" 정보로 표현된다.
 * 예를 들어, WeatherApiResponse.location 필드의 타입이 Location 이라면
 * WeatherApiResponse -> Location 이라는 관계가 생긴다.</p>
 *
 * <p>이 계층은 오직 "출력 세계(Java 타입들)의 구조"에만 관심을 가지며,
 * JSON 스키마나 타입 추론 내부 구현에는 의존하지 않는다.</p>
 */
public final class ModelGraph {

    private final ModelClass rootClass;
    private final Map<String, ModelClass> classesByQualifiedName;

    private ModelGraph(ModelClass rootClass, Map<String, ModelClass> classesByQualifiedName) {
        this.rootClass = Objects.requireNonNull(rootClass, "rootClass must not be null");
        this.classesByQualifiedName = Objects.requireNonNull(classesByQualifiedName, "classesByQualifiedName must not be null");
    }

    /**
     * 그래프의 루트 클래스(엔트리 포인트)를 반환한다.
     */
    public ModelClass getRootClass() {
        return rootClass;
    }

    /**
     * 그래프에 포함된 모든 모델 클래스를 선언 순서대로 반환한다.
     */
    public Collection<ModelClass> getDeclaredClasses() {
        return classesByQualifiedName.values();
    }

    /**
     * 그래프에 포함된 클래스 개수.
     */
    public int size() {
        return classesByQualifiedName.size();
    }

    /**
     * 디버깅/로그용 간단한 문자열 표현.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ModelGraph{\n");
        for (ModelClass modelClass : classesByQualifiedName.values()) {
            sb.append("  ").append(modelClass).append('\n');
        }
        sb.append('}');
        return sb.toString();
    }

    // =====================================================================
    //  ModelClass
    // =====================================================================

    /**
     * 실제로 생성될 Java 클래스 한 개를 표현하는 이너클래스.
     * 현재는 뼈대만 두고, 이후 커밋에서 상세 구현을 추가한다.
     */
    public static final class ModelClass {
        // 이후 커밋에서 구현
    }

    // =====================================================================
    //  Field
    // =====================================================================

    /**
     * 하나의 Java 필드를 표현하는 이너클래스.
     * 현재는 뼈대만 두고, 이후 커밋에서 상세 구현을 추가한다.
     */
    public static final class Field {
        // 이후 커밋에서 구현
    }
}
