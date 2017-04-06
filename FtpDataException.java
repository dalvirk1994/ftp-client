/**
 * Custom exception handling for ftp data connection related errors.
 * Created by Dalvir on 2017-02-03.
 */
public class FtpDataException extends Exception {
    public FtpDataException(String message) {
        super(message);
    }
}
