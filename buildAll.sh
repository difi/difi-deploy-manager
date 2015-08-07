#!/bin/sh

cd ./difi-deploy-manager-admin/
mvn clean install

cd ../deploy-manager-health-check/
mvn clean install

cd ..
mvn clean install