FROM java:7
MAINTAINER DIFI <espen.korra@difi.no>

RUN mkdir -p /var/www/app
COPY ./deploy-manager-0.9.56-SNAPSHOT.jar /usr/src/deploymanager
WORKDIR /usr/src/deploymanager

CMD java -jar deploy-manager-0.9.56-SNAPSHOT.jar