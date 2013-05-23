#!/bin/csh

set TEST=test
set GOOD=$TEST/good*
set BAD=$TEST/bad*

echo "Building..."
make

echo "Good tests"
echo "=========="
foreach f (`ls $GOOD`)
	echo $f ":"
	echo "---------------------------------------------------------------------"
	cat $f
	echo "---------------------------------------------------------------------"
	echo "part1:"
	cat $f | make run-part1
	echo "---------------------------------------------------------------------"
	echo "part2:"
	cat $f | make run-part2
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
	echo "part1:"
	cat $f | make run-part1
	echo "---------------------------------------------------------------------"
	echo "part2:"
	cat $f | make run-part2
	echo "#####################################################################"
	echo
end

echo "Cleaning..."
make clean
