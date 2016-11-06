export JRE_HOME=/usr/lib/jvm/java-1.6.0-openjdk-1.6.0.0.x86_64

export JAVA_OPTS=$JAVA_OPTS" -Xmx1000m"
export JAVA_OPTS=$JAVA_OPTS" -Djava.awt.headless=true"
export JAVA_OPTS=$JAVA_OPTS" -Dtreevotee.simplelogger.level=debug"
#export JAVA_OPTS=$JAVA_OPTS" -Dwiio.output.array2json.max=1048576" # = 2^20
export JAVA_OPTS=$JAVA_OPTS" -Dwiio.output.array2json.max=104857600" # = 2^20

~/tool/apache-tomcat-6.0.10/bin/catalina.sh stop
~/tool/apache-tomcat-6.0.10/bin/catalina.sh start
