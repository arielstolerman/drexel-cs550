#!/usr/bin/python #
# Fig4-1.py - Demonstration of a scanner in Python
#		Adapted directly from the Louden example, Fig4-1
# 		- doesn't evaluate, just spits out tokens
#
# This is a simple example, using a single lexer, lex (the default one, that
# comes in the package).  You *can* call constructors, to have multiple
# lexers in the same program.
#
#	See http://www.dabeaz.com/ply/ply.html for explanations and a similar (but
#	cooler) example.
#
# Kurt Schmidt
# 7/07
#
# EDITOR:  cols=80, tabstop=2
#
# DEMONSTRATES:  PLY, Python lexer
#

import sys

######   LEXER   ###############################
from ply import lex

	# this takes the place of the enum, in C.  These are your token types.
tokens = (
	'ERROR',
	'PLUS',
	'TIMES',
	'LPAREN',
	'RPAREN',
	'EOL',
	'NUMBER',
	'VAR'
)

# Now, this section.  We have a mapping, REs to token types (please note
# the t_ prefix).  They simply return the type.

	# t_ignore is special, and does just what it says
t_ignore = ' \t'

	# These are the simple maps
		# These are all regular expressions.  raw string doesn't translate the \
t_PLUS		= r'\+'
t_TIMES		= r'\*'
t_LPAREN	= r'\('
t_RPAREN	= r'\)'

t_VAR     = r'[a-zA-Z_][a-zA-Z_0-9]*'

	# for tokens that require an action, we actually define functions (again,
	# note the prefixes) and put the pattern (RE) in the doc string for the
	# function.  Nearly magic

def t_NUMBER( t ) :
	r'[0-9]+'

		# t.value holds the string that matched.  Dynamic typing - no unions
	t.value = int( t.value )
	return t

	# These are standard little ditties:
def t_newline( t ):
  r'\n+'
  t.lexer.lineno += len( t.value )

  # Error handling rule
def t_error( t ):
  print "Illegal character '%s' on line %d" % ( t.value[0], t.lexer.lineno )
  #return t
  t.lexer.skip( 1 )

  # Here is where we build the lexer, after defining it (above)
lex.lex()

######   LEXER (end)   ###############################


def main( arg=sys.argv ) :

		# Now, this lexer actually takes a string; it doesn't (that I yet know)
		# read from a file.  So, you can parse the file as you like, and feed it
		# to the lexer.
	
	# we're going to read a line at a time from stdin

	line_cnt = 0

	for line in sys.stdin :

		lex.input( line )

		line_cnt += 1
		print "\nLine #", line_cnt

			# attempt to get that first token
		tok = lex.token()
		while tok :
			print tok
			tok = lex.token()

		sys.stdout.flush()

		# NOTE:  tok is an instance of LexToken.  Has these attributes:
		#   type - the type, from the 'tokens' list, assigned by magic
		#   value - the string that matched, unless you did something
		#   lineno - the line # (see t_newline())
		#   lexpos - the position of the character, from the beginning


if __name__ == '__main__' :
	main()
