#!/usr/bin/python
# Code of Figure 4.12, pages 105-107 from
# Kenneth C. Louden, Programming Languages
# Principles and Practice 2nd Edition
# Copyright (C) Brooks-Cole/ITP, 2003
# 
# Translated to Python
# Kurt Schmidt
# 7/08
# 
# Python 2.5.1
# 
# It is helpful to consider the following EBNF grammar:
# 
# <cmd>			:= <expr> '\n'
# <expr>		:= <term> { '+' <term> }
# <term>		:= <factor> { '*' <factor> }
# <factor>	:= '('<expr>')' | <number> 
# <number>	:= <digit> {<digit>}
#
# NOTE: spaces are not accepted here.  Where would you put code to account
#	 for them?
# 
# EDITOR: tabstop=2, cols=80
# 

import sys
M_LEVEL = 0

def mprint( l, s ) :
	if l >= M_LEVEL :
		sys.stderr.write( s )

### These are administrative functions

def error() :
	print "parse error:", token
	sys.exit( 1 )

def nextToken() :
	''' Read the next token, store in global token
	tokens are characters '''
	global token
	token = sys.stdin.read( 1 )
	mprint( 2, "nextToken: got token: '%s'\n" % token )

def match( t ) :
	''' if current token  is t, get next token
	else, call error() '''
	global token
	if token == t : nextToken()
	else : error()

### These are the various rec. functions that make up the parser

def command() :
	''' command -> expr '\n' '''
	global token
	result = expr()
	if token == '\n' :
			# end the parse and print the result
		print "The result is:", result
	else :
		error()

def expr() :
	''' expr -> term { '+' term } '''
	global token
	result = term()
	while token == '+' :
		match( '+' )
		result += term()
	return result

def term() :
	'''term -> factor { '*' factor } '''
	global token
	result = factor()
	while token == '*' :
		match( '*' )
		result *= factor()
	return result

def factor() :
	''' factor -> '( ' expr ')' | number '''
	global token
	if token == '(' :
		match( '(' )
		result = expr()
		match( ')' )
	else :
		result = number()
	return result

def number() :
	''' number -> digit { digit } '''
	global token
	result = digit()
	while token.isdigit() :
			# the value of a number with a new trailing digit is its previous value
			# shifted by a decimal place plus the value of the new digit
		result = 10 * result + digit()
	return result

def digit() :
	''' digit -> '0' | '1' | '2' | '3' | '4' 
			| '5' | '6' | '7' | '8' | '9' '''
	global token
	if token.isdigit() :
		result = int( token )
		match( token )
	else :
		error()
	return result

def parse() :
	nextToken()	# get the first token
	command()		# call the parsing procedure for the start symbol


def main() :
	parse()
	return 0

if __name__ == '__main__' :
	main()
