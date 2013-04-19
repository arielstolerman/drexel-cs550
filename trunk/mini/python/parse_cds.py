#!/usr/bin/python
#
# To parse the cd xml file
#
# Kurt Schmidt
# 7/09
#
# EDITOR:  tabstop=2, cols=80
#

from xml.dom import minidom
import sys

if len( sys.argv ) < 2 :
	infile = sys.stdin
else :
	infile = file( sys.argv[1] )

dom = minidom.parse( infile )

print "Here's a list of (top-level) nodes:\n"
print dom.childNodes

print "\n\nOr, we know we want the catalog, at this level:\n"
cat = dom.getElementsByTagName( "catalog" )
print cat

print "\n\nAnd we can look at *its* children:\n"
#print cat[0].childNodes
for c in cat[0].childNodes :
	print c

