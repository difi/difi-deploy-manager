# Difi Deployment Manager
##First time setup
Check out and build project with
<pre>mvn clean install</pre>
...and you should be set to go.

##Project settings
Use whatever tool you prefer to view or write code, import project into IntelliJ or Eclipse, or maybe Notepad or Vim is your flavor.

Anyways; some settings that are important:
- Line breaks: LF (CRLF should work just fine too)
- File encoding: UTF-8
- Property file encoding: ISO-8859-1

##Run project
First the server must be started. When project is built, 
JAR is generated in target folder. To run from project root:
<pre>java -jar ./deploy-manager/target/no.difi.deploymanager-[version].jar</pre>

##Limitations
Alpha version (PoC)

For restarting of JAR-files. Application using command:
<pre>java -jar [name-of-app]</pre>

##Compability
Working for Linux, Mac and Windows

Monitored applications are hardcoded in RestartDto.

##Automatic testing
Features are tested automatically with both unit- and integration tests. For restart, only the OS that is available is tested.