package dev.yukmekim.settlement.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통 에러
    INVALID_INPUT_VALUE("C001", "잘못된 입력값입니다."),
    ENTITY_NOT_FOUND("C002", "엔티티를 찾을 수 없습니다."),

    // 서버 에러
    INTERNAL_SERVER_ERROR("S001", "서버 내부 오류가 발생했습니다."),

    // 정산 에러
    SETTLEMENT_NOT_FOUND("SE001", "정산 데이터를 찾을 수 없습니다."),
    SETTLEMENT_ALREADY_EXISTS("SE002", "이미 정산된 데이터입니다.");

    private final String code;
    private final String message;
}
