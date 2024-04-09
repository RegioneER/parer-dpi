#!/bin/bash
TOMCAT_HOME=/opt/tomcat
USER_HOME=/home/parer
DPI_WAR_FILE=dpi_aobo.war

$TOMCAT_HOME/bin/shutdown.sh
rm -rf $TOMCAT_HOME/webapps/dpi*
rm -rf $TOMCAT_HOME/work/*
rm -rf $TOMCAT_HOME/temp/*
#cp $USER_HOME/$DPI_WAR_FILE $TOMCAT_HOME/webapps/dpi.war
