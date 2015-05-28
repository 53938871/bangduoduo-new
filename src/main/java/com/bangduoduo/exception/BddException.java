package com.bangduoduo.exception;

/**
 * Created by cpeng2 on 5/28/2015.
 */
public class BddException extends Exception {
    private final Throwable cause;
    private final int code;
    private final String formattedMsg;

    public BddException(final Throwable cause, final int code , final String msg) {
        this.cause = cause;
        this.code = code;
        this.formattedMsg = msg;
    }

    public BddException(final BddException exception) {
        this.formattedMsg = exception.getMessage();
        this.code = exception.getCode();
        this.cause = exception;
    }

   public BddException( final Throwable cause, final ErrorCode code, final Object... args) {
       final String tmp;
       tmp = String.format(code.getFormat(), args);
       this.formattedMsg = tmp;
       this.code = code.getCode();
       this.cause = cause;
   }

    public BddException(final ErrorCode code, final Object... args) {
        this(null, code, args);
    }

    @Override
     public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{cause=").append(cause);
        sb.append(", code=").append(code);
        sb.append(", formattedMsg='").append(formattedMsg).append('\'');
        sb.append('}');
        return sb.toString();
     }

    @Override
    public Throwable getCause() {
        return cause;
    }

    public int getCode() {
        return code;
    }

    public String getFormattedMsg() {
        return formattedMsg;
    }
}
