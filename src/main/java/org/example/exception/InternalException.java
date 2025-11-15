package org.example.exception;

/**
 * 코드 버그, 라이브러리 오동작, 예측하지 못한 상태 등
 * "사용자가 어떻게 할 수 없는" 내부 오류를 표현하는 예외.
 *
 * <p>예시:
 * <ul>
 *     <li>JsonAnalyzer / TypeInferencer 로직 버그</li>
 *     <li>파일 시스템의 알 수 없는 I/O 오류</li>
 *     <li>원래 발생하면 안 되는 분기까지 도달한 경우</li>
 * </ul>
 *
 * <p>일반적으로는 로그를 남기고,
 * 사용자에게는 "프로그램 내부 오류가 발생했습니다" 정도의
 * 짧은 안내만 보여주는 용도로 사용한다.
 */
public class InternalException extends RuntimeException {

    /**
     * 내부 오류에 대한 간단한 설명 메시지.
     *
     * @param message 내부 오류 설명
     */
    public InternalException(String message) {
        super(message);
    }

    /**
     * 원인이 되는 예외를 함께 감싸고 싶을 때 사용하는 생성자.
     *
     * @param message 설명 메시지
     * @param cause   실제 원인이 된 예외
     */
    public InternalException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 특별한 메시지 없이 원인 예외만 감싸고 싶을 때 사용.
     *
     * @param cause 실제 원인이 된 예외
     */
    public InternalException(Throwable cause) {
        super(cause);
    }
}
