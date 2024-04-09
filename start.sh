#!/bin/bash
TOMCAT_HOME=/opt/tomcat
if [ $# -eq 0 ]
 then 
  echo "No arguments supplied"
  exit 
fi
cp $1 $TOMCAT_HOME/webapps/dpi.war
echo "cp $1 "
$TOMCAT_HOME/bin/startup.sh
tail -f $TOMCAT_HOME/logs/dpi.log
