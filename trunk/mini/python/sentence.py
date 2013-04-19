#!/usr/bin/python
#
# sentence.py - Just breaks input into tokens (as strings), over white space
#
# This is a simple example, using a single lexer, lex (the default one, that
# comes in the package).  You *can* call constructors, to have multiple
# lexers in the same program.
#
#	See http://www.dabeaz.com/ply/ply.html for explanations and a similar (but
#	cooler) example.
#
# Kurt Schmidt
# 11/08
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
	'TOK'
)

# Now, this section.  We have a mapping, REs to token types (please note
# the t_ prefix).  They simply return the type.

	# t_ignore is special, and does just what it says
t_ignore = ' \t,'


def t_TOK( t ) :
	r'\w+'

	t.value = t.value.lower()
	return t

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
	
		# we'll read from stdin
	if sys.stdin.isatty() :
		prompt = '> '
	else :
		prompt = ''
	line = raw_input( prompt )
	line = line.strip()

	while line and line not in ( 'quit', 'q', '.', 'bye' ) :
		try :
			lex.input( line )
			tok = lex.token()
			while tok :
				print tok
				tok = lex.token()
		except Exception :
			pass
		try :
			line = raw_input( prompt )
			line = line.strip()
		except EOFError :
			print ""
			break


if __name__ == '__main__' :
	main()
