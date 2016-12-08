#!/bin/bash
set -e

# Written by Lake Mossman, 14 Oct. 2016
#
# Copyright (c) 1997 - 2016, Xing, BSD 3-Clause License, ALL RIGHTS RESERVED
#


# Check if argument was given
if [ $# -eq 0 ]
then
	echo "usage: $0 <path to junami jar> <path to juneberry directory>"
	exit
fi

# Set up variables and temporary directory for source modification
OLD_NAMESPACE="one.junami"
NEW_NAMESPACE="jpl.mipl.w10n.junami"
BASE_PATH=$(readlink -f $0)
BASE_DIR=$(dirname "$BASE_PATH")
JAR_PATH=$(readlink -f $1)
JUNEBERRY_PATH=$(readlink -f $2)
JUNEBERRY_SRC="$JUNEBERRY_PATH/src/java/jpl/mipl/"
CALL_DIR=$PWD
TEMP_DIR=`mktemp -d`

echo "Extracing source code from \"$JAR_PATH\""

cd $TEMP_DIR
jar -xf $JAR_PATH

FILE_NAME=$(basename $JAR_PATH)

COMMENT_MESSAGE="/*\nThis file is auto converted from open source package "$FILE_NAME"\nonly for name space change from "$OLD_NAMESPACE" to "$NEW_NAMESPACE"\nusing tool incorporate_junami_src.sh\nby "$USER" on "`date`"\n*/\n\n"

# Update the tree structure
mkdir -p ./jpl/mipl/
mv ./one ./jpl/mipl/w10n
find ./jpl/ -name "*.class" -type f -delete

echo "Updating namespace from "$OLD_NAMESPACE" to "$NEW_NAMESPACE""

# Replace old namespace with new namespace
find ./ -type f -exec sed -i -e 's/'$OLD_NAMESPACE'/'$NEW_NAMESPACE'/g' {} \;

# Add the comment to the top of each source file
find ./jpl/mipl/w10n/junami -type f -exec sed -i -e '1s;^;'"$COMMENT_MESSAGE"';' {} \;

# Copy new files into juneberry source
echo "Copying source files to juneberry src"
echo $JUNEBERRY_SRC
cp -r ./jpl/mipl/w10n/ $JUNEBERRY_SRC

rm -r $TEMP_DIR
