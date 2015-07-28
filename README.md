# Difi Deployment Manager
## First time setup
Pull project and build with 
<pre>mvn clean install </pre>

JAR is generated in target folder. To run from project root:
<pre>java -jar ./target/no.difi.deploymanager-0.9.0-SNAPSHOT.jar

##Limitations
Alpha version (PoC)

Currently only working for nix systems.

New applications is hardcoded in MonitoringApplications. Will be changed for more dynamic deploy. 