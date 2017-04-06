/**
 * Custom exception handling for general ftp connection related errors.
 * Created by Dalvir on 2017-01-24.
 */
public class FtpException extends Exception {
    public FtpException(String message) {
        super(message);
    }
}
