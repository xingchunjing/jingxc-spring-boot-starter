package top.jingxc.server.exception;

public class ReturnCode200Exception extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReturnCode200Exception() {
    }

    public ReturnCode200Exception(String msg) {
        super(msg);
    }

}