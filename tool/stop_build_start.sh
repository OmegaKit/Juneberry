#!/bin/bash

set -e

export JRE_HOME=/usr/lib/jvm/java-1.6.0-openjdk-1.6.0.0.x86_64

#export JAVA_OPTS=$JAVA_OPTS" -Djuneberry.local_path_prefix=/var/www/html
#export JAVA_OPTS=$JAVA_OPTS" -Dsimplelogger.level=debug"
#export JAVA_OPTS=$JAVA_OPTS" -Djuneberry.ehcache.xml.path=/mnt/sdc1/xing/juneberry-0.0.1/conf/ehcache.xml"

export JAVA_OPTS=$JAVA_OPTS" -Dtreevotee.simplelogger.level=fine"
#export JAVA_OPTS=$JAVA_OPTS" -Xms65536m -Xmx65536m"
export JAVA_OPTS=$JAVA_OPTS" -Xms4096m -Xmx4096m"

~/tool/apache-tomcat-6.0.10/bin/catalina.sh stop

ant clean
ant war

rm -vfr ~/tool/apache-tomcat-6.0.10/webapps/juneberry*
rm -vfr ~/tool/apache-tomcat-6.0.10/temp/*{data,index}
#cp -avi build/juneberry-*.war ~/tool/apache-tomcat-6.0.10/webapps/juneberry.war
cp -avi build/juneberry-*.war ~/tool/apache-tomcat-6.0.10/juneberry.war
(
cd ~/tool/apache-tomcat-6.0.10/webapps
mkdir juneberry
cd juneberry
jar xvf ../../juneberry.war
)
rm -v ~/tool/apache-tomcat-6.0.10/juneberry.war

cp -av ./sample/web.xml ~/tool/apache-tomcat-6.0.10/webapps/juneberry/WEB-INF

~/tool/apache-tomcat-6.0.10/bin/catalina.sh start
