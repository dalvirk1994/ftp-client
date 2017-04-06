all: CSftp.jar
CSftp.jar: CSftp.java
	javac CSftp.java FtpConnection.java FtpCommand.java FtpControlConnection.java FtpDataConnection.java FtpException.java FtpDataException.java FtpControlException.java FtpCommand.java 
	jar cvfe CSftp.jar CSftp *.class


run: CSftp.jar  
	java -jar CSftp.jar ftp.cs.ubc.ca  21

clean:
	rm -f *.class
	rm -f CSftp.jar
