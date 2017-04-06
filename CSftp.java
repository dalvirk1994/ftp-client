import java.io.*;
import java.lang.System;
import java.net.URLDecoder;
import java.util.Scanner;

//
// This is an implementation of a simplified version of a command 
// line ftp client. The program always takes two arguments
//


public class CSftp {
    static final int ARG_CNT = 2;
    static final int DEFAULT_PORT = 21;
    static final String NUMBER_SIGN = "#";

    static boolean isLoggedIn = false;

    public static void main(String[] args) {
        // Get command line arguments and connected to FTP
        // If the arguments are invalid or there aren't enough of them
        // then exit.

        if (args.length > ARG_CNT || args.length == 0) {
            System.out.print("Usage: cmd ServerAddress ServerPort\n");
            return;
        }

        final Scanner scanner = new Scanner(System.in);
        final FtpControlConnection ftpControlConnection;

        try {
            final String hostName = args[0];
            final int portNumber = args.length == 1 ? DEFAULT_PORT : Integer.parseInt(args[1]);

            ftpControlConnection = new FtpControlConnection();
            ftpControlConnection.createConnection(hostName, portNumber);

        } catch (FtpException | FtpControlException e) {
            System.out.println(e.getMessage());
            return;
        } catch (NumberFormatException e) {
            System.out.println("0xFFFF Processing error. Invalid port number " + args[1]);
            return;
        }

        try {
            for (int len = 1; len > 0; ) {
                System.out.print("csftp> ");

                String input = scanner.nextLine();
                String response = "";

                if (input.isEmpty() || input.startsWith(NUMBER_SIGN)) {
                    continue;
                }

                FtpCommand command = new FtpCommand(input);

                switch (command.getCommandName()) {
                    case "user":
                        if (command.getCommandValues().isEmpty() || command.getCommandValues().size() > 1) {
                            System.out.println("0x002 Incorrect number of arguments.");
                            continue;
                        }
                        ftpControlConnection.sendToServer("USER " + command.getCommandValues().get(0));
                        ftpControlConnection.readFromServer();
                        break;

                    case "pw":
                        if (command.getCommandValues().isEmpty() || command.getCommandValues().size() > 1) {
                            System.out.println("0x002 Incorrect number of arguments.");
                            continue;
                        }
                        ftpControlConnection.sendToServer("PASS " + command.getCommandValues().get(0));
                        response = ftpControlConnection.readFromServer();
                        isLoggedIn = response.startsWith("<-- 230");
                        break;

                    case "quit":
                        if (!command.getCommandValues().isEmpty()) {
                            System.out.println("0x002 Incorrect number of arguments.");
                            continue;
                        }
                        ftpControlConnection.sendToServer("QUIT");
                        ftpControlConnection.readFromServer();
                        ftpControlConnection.closeConnection();
                        return;

                    case "get":
                        if (isLoggedIn) {
                            if (command.getCommandValues().isEmpty() || command.getCommandValues().size() > 1) {
                                System.out.println("0x002 Incorrect number of arguments.");
                                continue;
                            }
                            String fileName = command.getCommandValues().get(0);
                            ftpControlConnection.sendToServer("TYPE I");
                            response = ftpControlConnection.readFromServer();
                            if (response.startsWith("<-- 200")) {
                                ftpControlConnection.sendToServer("PASV");
                                response = ftpControlConnection.readFromServer();

                                if (response.startsWith("<-- 227")) {
                                    final FtpDataConnection dataConnection = getDataConnection(response);
                                    ftpControlConnection.sendToServer("RETR " + fileName);
                                    response = ftpControlConnection.readFromServer();

                                    if (response.startsWith("<-- 150") || response.startsWith("<-- 125")) {
                                        // get the current directory filepath
                                        String outputDirectory = CSftp.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                                        // current filepath includes CSftp.jar, so we remove it.
                                        outputDirectory = outputDirectory.replaceAll("CSftp\\.jar$", "");
                                        outputDirectory = URLDecoder.decode(outputDirectory, java.nio.charset.StandardCharsets.UTF_8.toString());
                                        FileOutputStream fileOutputStream = new FileOutputStream(outputDirectory + fileName);
                                        while (true) {
                                            int nextByte;
                                            // .read() returns the next byte as an int.
                                            nextByte = dataConnection.getInputStream().read();
                                            if (nextByte == -1) {
                                                // exit loop when nextByte is -1; it represents the end of the stream.
                                                break;
                                            }
                                            // writes nextByte to the output file.
                                            fileOutputStream.write(nextByte);
                                        }
                                        fileOutputStream.flush();
                                        fileOutputStream.close();
                                        ftpControlConnection.readFromServer();
                                        dataConnection.closeConnection();
                                    } else if (response.startsWith("<-- 550")) {
                                        System.out.println("0x38E Access to local file " + fileName + " denied.");
                                        continue;
                                    }
                                }
                            }

                        } else {
                            System.out.println("0x001 Invalid command.");
                            continue;
                        }
                        break;

                    case "features":

                        if (!command.getCommandValues().isEmpty()) {
                            System.out.println("0x002 Incorrect number of arguments.");
                            continue;
                        }
                        ftpControlConnection.sendToServer("FEAT");
                        ftpControlConnection.readFromServer();

                        break;

                    case "cd":
                        if (isLoggedIn) {
                            if (command.getCommandValues().isEmpty() || command.getCommandValues().size() > 1) {
                                System.out.println("0x002 Incorrect number of arguments.");
                                continue;
                            }
                            ftpControlConnection.sendToServer("CWD " + command.getCommandValues().get(0));
                            ftpControlConnection.readFromServer();
                        } else {
                            System.out.println("0x001 Invalid command.");
                            continue;
                        }
                        break;

                    case "dir":
                        if (isLoggedIn) {
                            if (!command.getCommandValues().isEmpty()) {
                                System.out.println("0x002 Incorrect number of arguments.");
                                continue;
                            }
                            ftpControlConnection.sendToServer("LIST");
                            response = ftpControlConnection.readFromServer();

                            if (response.startsWith("<-- 227")) {

                                try {
                                    final FtpDataConnection dataConnection = getDataConnection(response);
                                    ftpControlConnection.sendToServer("LIST");
                                    response = ftpControlConnection.readFromServer();
                                    if (response.startsWith("<-- 150") || response.startsWith("<-- 125")) {
                                        dataConnection.readFromServer();
                                        ftpControlConnection.readFromServer();
                                    }
                                    dataConnection.closeConnection();
                                } catch (FtpDataException fde) {
                                    System.out.println(fde.getMessage());
                                    continue;
                                }
                            }
                        } else {
                            System.out.println("0x001 Invalid command.");
                            continue;
                        }
                        break;

                    default:
                        System.out.println("0x001 Invalid command.");
                        continue;

                }
            }
        } catch (FtpException |
                FtpControlException ftpe)

        {
            System.out.println(ftpe.getMessage());
            return;
        } catch (
                Exception exception)

        {
            System.out.println("0xFFFE Input error while reading commands, terminating.");
            return;
        }

    }

    /**
     * Create a data connection by parsing response for passive mode.
     *
     * @param response from sending PASV command.
     * @return
     * @throws FtpDataException
     * @throws FtpException
     */
    public static FtpDataConnection getDataConnection(String response) throws FtpDataException, FtpException {
        // parse out IP address
        System.out.println(response);
        response = response.replaceAll(".*\\(|\\).*", "");
        String[] responseArray = response.split("\\s*,\\s*");
        StringBuilder ipAddress = new StringBuilder();
        ipAddress.append(responseArray[0]).append(".")
                .append(responseArray[1]).append(".")
                .append(responseArray[2]).append(".")
                .append(responseArray[3]);

        int portNumber = Integer.parseInt(responseArray[4]) * 256 + Integer.parseInt(responseArray[5]);
        FtpDataConnection dataConnection = new FtpDataConnection();
        dataConnection.createConnection(ipAddress.toString(), portNumber);
        return dataConnection;
    }
}
