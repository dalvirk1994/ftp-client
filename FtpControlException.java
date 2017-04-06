/**
 * Custom exception handling for ftp control connection related errors.
 * Created by Dalvir on 2017-02-03.
 */
public class FtpControlException extends Exception {
    public FtpControlException(String message) {
        super(message);
    }
}
