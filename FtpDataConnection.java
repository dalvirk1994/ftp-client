import java.io.BufferedInputStream;

/**
 * Creates a data connection.
 * Created by Dalvir on 2017-01-30.
 */
public class FtpDataConnection extends FtpConnection {
    private final static int TIME_OUT = 10000;

    private BufferedInputStream inputStream;

    public FtpDataConnection() {
        super(TIME_OUT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createConnection(String hostName, int portNumber) throws FtpDataException, FtpException {
        try {
            super.createConnection(hostName, portNumber);
            // we use a BufferedInputStream for data connections in order to properly read byte data.
            inputStream = new BufferedInputStream(getServerSocket().getInputStream());
        } catch (FtpException ftpe) {
            throw new FtpDataException("0x3A2 Data transfer connection to " + getHostName() + " on port " + getPortNumber() + " failed to open.");

        } catch (Exception e) {
            if (getServerSocket().isConnected() && !getServerSocket().isClosed()) {
                closeConnection();
            }
            throw new FtpDataException("0x3A7 Data transfer connection I/O error, closing data connection.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendToServer(String command) throws FtpDataException, FtpException {
        try {
            super.sendToServer(command);
        } catch (Exception e) {
            if (getServerSocket().isConnected() && !getServerSocket().isClosed()) {
                closeConnection();
            }
            throw new FtpDataException("0x3A7 Data transfer connection I/O error, closing data connection.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String readFromServer() throws FtpDataException, FtpException {
        String response;

        try {
            StringBuilder builder = new StringBuilder();
            String currentLine = getInBufferedReader().readLine();
            builder.append(currentLine);

            while ((currentLine = getInBufferedReader().readLine()) != null) {
                builder.append("\r\n");
                builder.append(currentLine);
            }

            response = builder.toString();
            System.out.println(response);
        } catch (Exception e) {
            if (getServerSocket().isConnected() && !getServerSocket().isClosed()) {
                closeConnection();
            }
            throw new FtpDataException("0x3A7 Data transfer connection I/O error, closing data connection.");
        }
        return response;
    }

    public BufferedInputStream getInputStream() {
        return inputStream;
    }
}
