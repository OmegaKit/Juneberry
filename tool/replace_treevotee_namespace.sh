#!/bin/bash
set -e

# Written by Lake Mossman, 13 Oct. 2016
#
# Copyright (c) 1997 - 2016, Xing, BSD 3-Clause License, ALL RIGHTS RESERVED
#


if [ $# -eq 0 ]
then
	echo "usage: $0 <path to directory>"
	exit
fi

OLD_NAMESPACE="the.treevotee"
NEW_NAMESPACE="jpl.mipl.treevotee"
DIR_PATH=$(readlink -f $1)

echo "Replacing all \"$OLD_NAMESPACE\" references with \"$NEW_NAMESPACE\" in directory $DIR_PATH"

find $DIR_PATH -type f -exec sed -i -e 's/'$OLD_NAMESPACE'/'$NEW_NAMESPACE'/g' {} \;
