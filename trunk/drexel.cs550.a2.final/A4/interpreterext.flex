/* this is the lex file for the sample mini language at
 * for CS550, Assignment 2
 * Modified by Mike Kopack for CS550, 2009 Spring Qtr.
 * Should be at the same level of completeness as the Lecture 2c
 * C++ version.
 *
 * --------------------------------------------------------
 *
 * Group 1 Extension:
 * added support for lists of integers and other such lists
 */

import java_cup.runtime.Symbol;
%%
%class Yylex[PART_NUM]
%cup
%eofval{ 
	return new Symbol( sym[PART_NUM].EOF );
%eofval}
%%
";" {return new Symbol(sym[PART_NUM].SEMI); }
"+" {return new Symbol(sym[PART_NUM].PLUS); }
"-" {return new Symbol(sym[PART_NUM].MINUS); }
"*" {return new Symbol(sym[PART_NUM].TIMES); }
"," {return new Symbol(sym[PART_NUM].COMMA); }
":=" {return new Symbol(sym[PART_NUM].ASSIGN); }
"define" {return new Symbol(sym[PART_NUM].DEFINE); }
"(" {return new Symbol(sym[PART_NUM].LPAREN); }
")" {return new Symbol(sym[PART_NUM].RPAREN); }
"[" {return new Symbol(sym[PART_NUM].LBRACKET); }
"]" {return new Symbol(sym[PART_NUM].RBRACKET); }
"||" {return new Symbol(sym[PART_NUM].CONCAT); }
"cons" {return new Symbol(sym[PART_NUM].CONS); }
"car" {return new Symbol(sym[PART_NUM].CAR); }
"cdr" {return new Symbol(sym[PART_NUM].CDR); }
"nullp" {return new Symbol(sym[PART_NUM].NULLP); }
"intp" {return new Symbol(sym[PART_NUM].INTP); }
"listp" {return new Symbol(sym[PART_NUM].LISTP); }
"if" {return new Symbol(sym[PART_NUM].IF); }
"then" {return new Symbol(sym[PART_NUM].THEN); }
"else" {return new Symbol(sym[PART_NUM].ELSE); }
"fi" {return new Symbol(sym[PART_NUM].FI);}
"while" {return new Symbol(sym[PART_NUM].WHILE); }
"do" {return new Symbol(sym[PART_NUM].DO); }
"od" {return new Symbol(sym[PART_NUM].OD); }
"proc" {return new Symbol(sym[PART_NUM].PROC); }
"end" {return new Symbol(sym[PART_NUM].END); }
"repeat" {return new Symbol(sym[PART_NUM].REPEAT); }
"until" {return new Symbol(sym[PART_NUM].UNTIL); }
"return" {return new Symbol(sym[PART_NUM].RETURN); }
[0-9]+ {return new Symbol(sym[PART_NUM].NUMBER, new Integer(yytext())); }
[a-zA-Z]+ {return new Symbol(sym[PART_NUM].ID, new String(yytext())); }
\/\/.*$ {/* ignore comments */}
[ \t\r\n\f] {/* ignore white space */}
/*
 * error handling
 */
. {System.err.println("Illegal character: "+yytext());}
