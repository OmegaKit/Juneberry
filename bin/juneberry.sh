#!/bin/bash

set -e

if [ $# = 0 ]; then
  echo "Usage: juneberry COMMAND"
  echo "where COMMAND is one of:"
  echo "  CommandLineTool"
  echo "  Reader"
  echo "    VicarIO"
  echo "    Fits"
  echo "    EdrIsis"
  echo "  Factory"
  echo "    StoreReaderClassFactory"
  echo "    OutputWriterClassFactory"
  echo "    ProcessorClassFactory"
  echo "  Raster"
  echo "    RasterExposer"
  echo "  Cache"
  echo "    LocalEhcache"
  echo "    RemoteEhcache"
  echo "  VicarIO:"
  echo "    SimpleConvert"
  echo "    jConvertIIO"
  echo "  SoftLinkManager"
  exit 1
fi

# get arguments
COMMAND=$1
shift

# jar dependencies and classes
# ref: http://stackoverflow.com/questions/219585/setting-multiple-jars-in-java-classpath
#CLASSPATH=$(for x in ../juneberry-lib.dir-0.9.0/*.jar; do echo -n $x:; done)./build/juneberry-0.9.0.jar
CLASSPATH=$(for x in ../juneberry-lib.dir.full-0.10.0/*.jar; do echo -n $x:; done)./build/juneberry-0.10.0.jar
#echo $CLASSPATH

#JAVA=$JAVA_HOME/bin/java
JAVA=`which java`

JAVA_OPTS=""
JAVA_OPTS=$JAVA_OPTS" -Xms4096m -Xmx4096m"
# only use headless mode
JAVA_OPTS=$JAVA_OPTS" -Djava.awt.headless=true"
JAVA_OPTS=$JAVA_OPTS" -Dtreevotee.simplelogger.level=debug"
#JAVA_OPTS=$JAVA_OPTS" -Dwiio.output.array2json.max=1048576" # = 2^20
JAVA_OPTS=$JAVA_OPTS" -Dwiio.output.array2json.max=1073741824" # = 2^30

if [ "$COMMAND" = "SimpleConvert" ] ; then
  CLASS=jpl.mipl.io.SimpleConvert
elif [ "$COMMAND" = "jConvertIIO" ] ; then
  CLASS=jpl.mipl.io.jConvertIIO
elif [ "$COMMAND" = "StoreReaderClassFactory" ] ; then
  CLASS=jpl.mipl.juneberry.store.read.ReaderClassFactory
elif [ "$COMMAND" = "OutputWriterClassFactory" ] ; then
  CLASS=jpl.mipl.juneberry.output.WriterClassFactory
elif [ "$COMMAND" = "ProcessorClassFactory" ] ; then
  CLASS=jpl.mipl.juneberry.proc.ProcessorClassFactory
elif [ "$COMMAND" = "CommandLineTool" ] ; then
  CLASS=jpl.mipl.juneberry.cli.CommandLineTool
  #OPT_D="-Dcom.sun.media.jai.disableMediaLib=true"
  #OPT_D=$OPT_D" -Dfoo.bar"
elif [ "$COMMAND" = "RasterExposer" ] ; then
  CLASS=jpl.mipl.juneberry.store.read.RasterExposer
elif [ "$COMMAND" = "LocalEhcache" ] ; then
  CLASS=jpl.mipl.juneberry.cache.LocalEhcache
elif [ "$COMMAND" = "RemoteEhcache" ] ; then
  CLASS=jpl.mipl.juneberry.cache.RemoteEhcache
elif [ "$COMMAND" = "VicarIO" ] ; then
  CLASS=jpl.mipl.juneberry.store.read.VicarIO
elif [ "$COMMAND" = "Fits" ] ; then
  CLASS=jpl.mipl.juneberry.store.read.Fits
elif [ "$COMMAND" = "EdrIsis" ] ; then
  CLASS=jpl.mipl.juneberry.store.read.EdrIsis
elif [ "$COMMAND" = "SoftLinkManager" ] ; then
  CLASS=jpl.mipl.juneberry.util.SoftLinkManager
else
  CLASS=$COMMAND
fi

#exec $JAVA $JAVA_OPTS $OPT_D -classpath "$CLASSPATH" $CLASS "$@"
exec $JAVA $JAVA_OPTS -classpath "$CLASSPATH" $CLASS "$@"
