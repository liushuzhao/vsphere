package cnitsec.common.connection;

public class ConnectionException extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public ConnectionException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
