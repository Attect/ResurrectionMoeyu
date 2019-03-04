package jp.co.a_tm.moeyu.api;

public class MoeyuAPIException extends Exception {
    private static final long serialVersionUID = -6049282589543171179L;
    private int statusCode;

    public MoeyuAPIException(int statusCode) {
        this.statusCode = statusCode;
    }

    public MoeyuAPIException(String detailMessage) {
        super(detailMessage);
    }

    public MoeyuAPIException(Throwable throwable) {
        super(throwable);
    }

    public MoeyuAPIException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public MoeyuAPIException(Throwable throwable, int statusCode) {
        super(throwable);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
