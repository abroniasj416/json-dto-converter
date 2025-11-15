package org.example.exception;

/**
 * 사용자 입력, 외부 환경 등
 * "사용자가 수정해서 다시 시도할 수 있는" 오류를 표현하는 예외.
 *
 * <p>예시:
 * <ul>
 *     <li>지원하지 않는 CLI 옵션 사용</li>
 *     <li>존재하지 않는 파일을 --input 으로 준 경우</li>
 *     <li>형식이 잘못된 JSON / 패키지명 / 클래스명 등</li>
 * </ul>
 *
 * <p>Main 진입점에서는 이 예외를 잡아서
 * 사용자에게 [ERROR] 메시지를 출력하고 종료 코드 1로 종료하는 식으로 활용한다.
 */
public class UserException extends RuntimeException {

    /**
     * 사용자에게 보여줄 오류 메시지.
     *
     * @param message 오류 설명 (이미 사용자 친화적으로 가공된 문장)
     */
    public UserException(String message) {
        super(message);
    }

    /**
     * 내부 예외를 함께 보존하고 싶을 때 사용하는 생성자.
     * (로그 등에서는 cause 까지 활용 가능)
     *
     * @param message 사용자에게 보여줄 메시지
     * @param cause   내부에서 발생한 실제 예외
     */
    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
}
