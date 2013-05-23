#!/bin/csh

set TEST=test
set GOOD=$TEST/good*
set BAD=$TEST/bad*

echo "Building..."
make compile

echo "Good tests"
echo "=========="
foreach f (`ls $GOOD`)
	echo $f ":"
	echo "---------------------------------------------------------------------"
	cat $f
	echo "---------------------------------------------------------------------"
	echo "static:"
	cat $f | make run-static
	echo "---------------------------------------------------------------------"
	echo "dynamic:"
	cat $f | make run-dynamic
	echo "#####################################################################"
	echo
end

echo

echo "Bad tests"
echo "========="
foreach f (`ls $BAD`)
	echo $f ":"
	echo "---------------------------------------------------------------------"
	cat $f
	echo "---------------------------------------------------------------------"
	echo "static:"
	cat $f | make run-static
	echo "---------------------------------------------------------------------"
	echo "dynamic:"
	cat $f | make run-dynamic
	echo "#####################################################################"
	echo
end

echo "Cleaning..."
make clean
