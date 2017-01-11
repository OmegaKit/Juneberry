#!/bin/bash
set -e

# Written by Lake Mossman, 4 Jan. 2017
#
# Copyright (c) 1997 - 2017, Xing, BSD 3-Clause License, ALL RIGHTS RESERVED
#

usage()
{
	echo "usage: $0 [-c] <path to juneberry directory> <path to apache servlet directory> <path to data directory> <desired port>"
	exit 1
}

# Check for compile flag
COMPILE_FLAG=false

while getopts :c opt
do
	case $opt in
	c)	COMPILE_FLAG=true;;
	\?)	echo "Invalid option: -$OPTARG" >&2
		usage;;
	esac
done
shift $((OPTIND-1))


# Check if number of arguments is correct
if [ $# -ne 4 ]
then
	echo "Invalid number of arguments"
	usage
fi


# Set up path variables
JUNEBERRY_PATH=$(readlink -f $1)
JUNEBERRY_SRC="$JUNEBERRY_PATH/src/java/jpl/mipl/"
APACHE_PATH=$(readlink -f $2)
DATA_PATH=$(readlink -f $3)
PORT=$4


# Step 1: Build the .war File
cd $JUNEBERRY_PATH
if [ $COMPILE_FLAG = true ]
then
	echo "Building war file in \"$JUNEBERRY_PATH\""
	ant clean
	ant war
fi

WAR_PATH=$(readlink -f $JUNEBERRY_PATH/build/*.war)

if [ $? -ne 0 ]
then
	echo "Error: juneberry war file does not exist"
	exit 1
fi


# Step 2: Setting up the Tomcat Servlet Engine
echo "Adding juneberry to apache servlet at \"$APACHE_PATH\""

cd "$APACHE_PATH/webapps/"
mkdir ./juneberry
cd ./juneberry
jar xf "$WAR_PATH" 


# Step 3: Configure the Tomcat Server
echo "Configuring apache server files"

cd $APACHE_PATH
sed -i '/<\/Host>/i \        <Context path=\"\/juneberry\" allowLinking=\"true\" privileged=\"true\"\/>' ./conf/server.xml
sed -i '/<\/tomcat-users>/i \  <role rolename=\"demo\"\/>' ./conf/tomcat-users.xml
sed -i '/<\/tomcat-users>/i \  <user username=\"demo\" password=\"demo\" roles=\"demo\"\/>' ./conf/tomcat-users.xml

sed -ie 's/<Connector port="8080" protocol="HTTP\/1.1"/<Connector port="'${PORT}'" protocol="HTTP\/1.1"/' ./conf/server.xml

# Step 4: Copy Icons
echo "Copying icons"

cd ./webapps/ROOT
tar xf $JUNEBERRY_PATH/icons.tar.gz


# Step 5: Setup Juneberry
echo "Configuring web.xml file"

cd $APACHE_PATH
sed -ie "/w10n of file system directory/,/<\/servlet>/  s|<param-value>\/var\/www\/html<\/param-value>|<param-value>${DATA_PATH}<\/param-value>|" ./webapps/juneberry/WEB-INF/web.xml
sed -ie "/w10n of imagery/,/<\/servlet>/  s|<param-value>\/var\/www\/html<\/param-value>|<param-value>${DATA_PATH}<\/param-value>|" ./webapps/juneberry/WEB-INF/web.xml
sed -i '/<\/web-app>/i <filter>\n  <filter-name>CorsFilter<\/filter-name>\n  <filter-class>org.apache.catalina.filters.CorsFilter<\/filter-class>\n<\/filter>\n<filter-mapping>\n  <filter-name>CorsFilter<\/filter-name>\n  <url-pattern>\/*<\/url-pattern>\n<\/filter-mapping>\n' ./webapps/juneberry/WEB-INF/web.xml

