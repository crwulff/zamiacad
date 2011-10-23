#!/bin/sh

if [ x$ZAMIA_HOME == x ] ; then
  SCRIPT=`readlink -f $0`
  ZAMIA_HOME=`dirname $SCRIPT`
fi

#echo "ZAMIA_HOME is ${ZAMIA_HOME}"

CP="${ZAMIA_HOME}/bin";
for i in ${ZAMIA_HOME}/share/jars/*.jar ; do
  CP="$CP:$i"
done
CP="$CP:${ZAMIA_HOME}/share"

#echo "CLASSPATH is ${CP}"

VMARGS="-Xmx1424m -Xms768m -Xss4m -server"

java $VMARGS -cp $CP org.zamia.cli.Zamia $@

