import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Class to create, close, maintain connections.
 * Created by Dalvir on 2017-01-24.
 */
public class FtpConnection {
    private String hostName;
    private int portNumber;
    private final int timeOut;
    private Socket serverSocket;
    private PrintWriter out;
    private BufferedReader in;

    public FtpConnection(int timeOut) {
        this.timeOut = timeOut;
    }

    /**
     * Attempt to create a connection to server.
     */
    protected void createConnection(String hostName, int portNumber) throws Exception {
        SocketAddress serverAddress = new InetSocketAddress(hostName, portNumber);
        this.hostName = hostName;
        this.portNumber = portNumber;
        serverSocket = new Socket();
        try {
            serverSocket.connect(serverAddress, timeOut);
        } catch (Exception e) {
            throw new FtpException("Connection error");
        }
        out = new PrintWriter(serverSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
    }

    /**
     * Send command to server and receive a response.
     */
    public void sendToServer(String command) throws Exception {
        System.out.println("--> " + command);
        out.write(command + "\r\n");
        out.flush();
    }

    /**
     * Goes through BufferedReader, builds a string, then prints it out and returns it
     * @throws Exception
     */
    public String readFromServer() throws Exception {
        String response;
        StringBuilder builder = new StringBuilder().append("<-- ");
        // reads the next line from the server output
        String currentLine = in.readLine();
        builder.append(currentLine);

        // block to deal with multi-line responses
        if (currentLine.startsWith("-", 3)) {
            builder.append("\r\n");
            String terminationLine = currentLine.substring(0, 3) + " ";
            while (!(currentLine = in.readLine()).contains(terminationLine)) {
                builder.append("<-- " + currentLine + "\r\n");
            }
            builder.append("<-- " + currentLine);
        }

        response = builder.toString();
        System.out.println(response);
        return response;
    }

    /**
     * Close the connection.
     */
    public void closeConnection() throws FtpException {
        try {
            out.close();
            in.close();
            serverSocket.close();
        } catch (IOException e) {
            throw new FtpException("0xFFFF Processing error. Problem encountered during closing connection.");
        }
    }

    public String getHostName() {
        return hostName;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public Socket getServerSocket() {
        return serverSocket;
    }

    public BufferedReader getInBufferedReader() {
        return in;
    }
}
