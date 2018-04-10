package hbie2.ftp;

/**
 * 描述：
 * 作者：ZP
 * 创建时间:2018/4/10
 * 最后修改时间:2018/4/10
 */
public class FTPClientException extends Throwable {
    private String msg;
    private Exception exception;

    public FTPClientException(String msg, Exception exception) {
        this.msg = msg;
        this.exception = exception;
    }

    public FTPClientException(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public Exception getException() {
        return exception;
    }
}
