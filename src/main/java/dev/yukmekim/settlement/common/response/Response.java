package dev.yukmekim.settlement.common.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Response<T> {

    private final boolean success;
    private final T data;
    private final String code;
    private final String message;

    public static <T> Response<T> ok(T data) {
        return new Response<>(true, data, "200", "성공");
    }

    public static <T> Response<T> ok(T data, String message) {
        return new Response<>(true, data, "200", message);
    }

    public static <T> Response<T> fail(String code, String message) {
        return new Response<>(false, null, code, message);
    }
}
