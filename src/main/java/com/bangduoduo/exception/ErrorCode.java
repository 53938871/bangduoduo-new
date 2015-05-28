package com.bangduoduo.exception;

/**
 * Created by cpeng2 on 5/28/2015.
 */
public enum ErrorCode {
    EMAIL_SENDING_FAILED(30000, "Sending email address %s failed"),
    UNKNOWN_ERROR_CODE(-1, "Unknown ErrorCode");

    private final int code;
    private final String format;

    ErrorCode(final int code,final String format) {
        this.code = code;
        this.format = format;
    }

    public int getCode() {
        return code;
    }

    public String getFormat() {
        return format;
    }

    public static ErrorCode fromCode(final int code) {
        for(ErrorCode errorCode : ErrorCode.values()) {
            if(errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return UNKNOWN_ERROR_CODE;
    }
}
