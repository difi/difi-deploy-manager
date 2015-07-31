# Difi Deployment Manager
## First time setup
Pull project and build with 
<pre>mvn clean install </pre>

JAR is generated in target folder. To run from project root:
<pre>java -jar ./target/no.difi.deploymanager-0.9.0-SNAPSHOT.jar

##Limitations
Alpha version (PoC)

Restarting of JAR-files. Application using command:
<pre>java -jar name-of-app</pre>

##Compability
Working for Linux, Mac and Windows

Monitored applications are hard-coded in RestartDto.

##Automatic testing
Features are tested automatically with both unit- and integration tests. For restart, only the OS that is available is tested.
