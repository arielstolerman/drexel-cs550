#!/usr/bin/python
#
# if.py - explores the if/if-else ambiguity
#
# Kurt Schmidt
# 2/11
#
# EDITOR:  cols=80, tabstop=2
#
# DEMONSTRATES:  PLY, Python lex, yacc
#

import sys

######   LEXER   ###############################
# Note:  This is precisely the same lexer that exp2 uses.  Could've pulled
# it out to a different file.

from ply import lex

	# this takes the place of the enum, in C.  These are your token types.
tokens = (
	'IF',
	'THEN',
	'ELSE',
	'EXP',
	'BS'
)

# Now, this section.  We have a mapping, REs to token types (please note
# the t_ prefix).  They simply return the type.

	# t_ignore is special, and does just what it says.  Spaces and tabs
t_ignore = ' \t'

	# These are the simple maps
t_IF		= r'if'
t_THEN	= r'then'
t_ELSE	= r'else'
t_EXP		= r'exp'
t_BS		= r'S'

	# These are standard little ditties:
def t_newline( t ):
  r'\n+'
  t.lexer.lineno += len( t.value )

  # Error handling rule
def t_error( t ):
  print "Illegal character '%s' on line %d" % ( t.value[0], t.lexer.lineno )
  return t
  #t.lexer.skip( 1 )

  # Here is where we build the lexer, after defining it (above)
lex.lex()

######   LEXER (end)   ###############################


######   YACC   #####################################

import ply.yacc as yacc

	# create a function for each production (note the prefix)
	# The rule is given in the doc string

precedence = (
	( 'nonassoc', 'IF' ),
	( 'nonassoc', 'ELSE' )
)

def p_stmt_if( p ) :
	'stmt : ifstmt'
	print 'stmt <- ifstmt'

def p_stmt_BS( p ) :
	'stmt : BS'
	print 'stmt <- BS'

def p_ifstmt( p ) :
	'ifstmt : IF EXP THEN stmt'
	print 'ifstmt <- IF EXP THEN stmt'

def p_if_else_stmt( p ) :
	'ifstmt : IF EXP THEN stmt ELSE stmt'
	print 'ifstmt <- IF EXP THEN stmt ELSE stmt'

# Error rule for syntax errors
def p_error( p ):
	print "Syntax error in input!"

	# now, build the parser
yacc.yacc()

def main( arg=sys.argv ) :

		# Now, this lexer actually takes a string; it doesn't (that I yet know)
		# read from a file.  So, you can parse the file as you like, and feed it
		# to the lexer.
	
		# we'll read from stdin
	if sys.stdin.isatty() :
		prompt = '> '
	else :
		prompt = ''
	s = raw_input( prompt )
	t = s.strip()

	while s and t not in ( 'quit', 'q', '.', 'bye' ) :
		try :
			result = yacc.parse( s )
			print result
		except Exception :
			pass
		try :
			s = raw_input( prompt )
		except EOFError :
			print ""
			break
		t = s.strip()


if __name__ == '__main__' :
	main()
