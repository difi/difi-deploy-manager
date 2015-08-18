# Difi Deployment Manager
## First time setup
For later builds, it is sufficient to run
<pre>mvn clean install</pre>

##Run project
First the server must be started. When project is built, 
JAR is generated in target folder. To run from project root:
<pre>java -jar ./target/no.difi.deploymanager-<version>.jar</pre>

##Limitations
Alpha version (PoC)

Restarting of JAR-files. Application using command:
<pre>java -jar [name-of-app]</pre>

##Compability
Working for Linux, Mac and Windows

Monitored applications are hardcoded in RestartDto.

##Automatic testing
Features are tested automatically with both unit- and integration tests. For restart, only the OS that is available is tested.
