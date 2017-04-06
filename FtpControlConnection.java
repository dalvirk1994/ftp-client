/**
 * Creates a control connection.
 * Created by Dalvir on 2017-01-30.
 */
public class FtpControlConnection extends FtpConnection {
    private final static int TIME_OUT = 20000;

    public FtpControlConnection() {
        super(TIME_OUT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createConnection(String hostName, int portNumber) throws FtpControlException, FtpException {
        try {
            super.createConnection(hostName, portNumber);
            String connectionResponse = super.readFromServer();
            if (connectionResponse.contains("421")) {
                throw new FtpException("0xFFFC Control connection to " + getHostName() + " on port " + getPortNumber() + " failed to open.");
            }
        } catch (FtpException ftpe) {
            throw new FtpControlException("0xFFFC Control connection to " + getHostName() + " on port " + getPortNumber() + " failed to open.");
        } catch (Exception e) {
            if (getServerSocket().isConnected() && !getServerSocket().isClosed()) {
                closeConnection();
            }
            throw new FtpControlException("0xFFFD Control connection I/O error, closing control connection.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendToServer(String command) throws FtpControlException, FtpException {
        try {
            super.sendToServer(command);
        } catch (Exception e) {
            closeConnection();
            throw new FtpControlException("0xFFFD Control connection I/O error, closing control connection.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String readFromServer() throws FtpControlException, FtpException {
        try {
            return super.readFromServer();
        } catch (Exception e) {
            if (getServerSocket().isConnected() && !getServerSocket().isClosed()) {
                closeConnection();
            }
            throw new FtpControlException("0xFFFD Control connection I/O error, closing control connection.");
        }
    }
}
