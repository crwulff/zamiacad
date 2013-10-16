#!/bin/sh

#This works with "share" folder unzipped from the zamiacad.jar delivered. 
#Linux users can mount the zamiacad.jar and avoid unzipping.

#Linux experts: Use of [ instead of [[ is wrong. Comparing $VAR without quotes is wrong. Use of ` is wrong. Using capitals for variable name is wrong.
#if [ x$ZAMIA_HOME = x ] ; then
if [[ -z "$ZAMIA_HOME" ]] ; then
  SCRIPT=`readlink -f $0`
  ZAMIA_HOME=`dirname $SCRIPT`
fi


export PYTHONPATH=$ZAMIA_HOME/share/python/Lib

#echo CLASSPATH=$CLASSPATH, PYTHONPATH=$PYTHONPATH

export CLASSPATH="$CLASSPATH:${ZAMIA_HOME}/bin:${ZAMIA_HOME}/share:${ZAMIA_HOME}/share/jars/*";
#echo CLASSPATH=$CLASSPATH
VMARGS="-Xmx1424m -Xms768m -Xss4m -server"

java $VMARGS org.zamia.cli.Zamia $@

