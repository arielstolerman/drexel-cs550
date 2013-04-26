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
%cup
%%
";" {System.out.println("SEMI"); return new Symbol(sym.SEMI); }
"+" {System.out.println("PLUS"); return new Symbol(sym.PLUS); }
"-" {System.out.println("MINUS"); return new Symbol(sym.MINUS); }
"*" {System.out.println("TIMES"); return new Symbol(sym.TIMES); }
"," {System.out.println("COMMA"); return new Symbol(sym.COMMA); }
":=" {System.out.println("ASSIGN"); return new Symbol(sym.ASSIGN); }
"define" {System.out.println("DEFINE"); return new Symbol(sym.DEFINE); }
"(" {System.out.println("LPAREN"); return new Symbol(sym.LPAREN); }
")" {System.out.println("RPAREN"); return new Symbol(sym.RPAREN); }
"[" {System.out.println("LBRACKET"); return new Symbol(sym.LBRACKET); }
"]" {System.out.println("RBRACKET"); return new Symbol(sym.RBRACKET); }
"||" {System.out.println("CONCAT"); return new Symbol(sym.CONCAT); }
"cons" {System.out.println("CONS"); return new Symbol(sym.CONS); }
"car" {System.out.println("CAR"); return new Symbol(sym.CAR); }
"cdr" {System.out.println("CDR"); return new Symbol(sym.CDR); }
"nullp" {System.out.println("NULLP"); return new Symbol(sym.NULLP); }
"intp" {System.out.println("INTP"); return new Symbol(sym.INTP); }
"listp" {System.out.println("LISTP"); return new Symbol(sym.LISTP); }
"if" {System.out.println("IF"); return new Symbol(sym.IF); }
"then" {System.out.println("THEN"); return new Symbol(sym.THEN); }
"else" {System.out.println("ELSE"); return new Symbol(sym.ELSE); }
"fi" {System.out.println("FI"); return new Symbol(sym.FI);}
"while" {System.out.println("WHILE"); return new Symbol(sym.WHILE); }
"do" {System.out.println("DO"); return new Symbol(sym.DO); }
"od" {System.out.println("OD"); return new Symbol(sym.OD); }
"proc" {System.out.println("PROC"); return new Symbol(sym.PROC); }
"end" {System.out.println("END"); return new Symbol(sym.END); }
"repeat" {System.out.println("REPEAT"); return new Symbol(sym.REPEAT); }
"until" {System.out.println("UNTIL"); return new Symbol(sym.UNTIL); }
"return" {System.out.println("RETURN"); return new Symbol(sym.RETURN); }
[0-9]+ {Integer i = new Integer(yytext()); System.out.println(i + ""); return new Symbol(sym.NUMBER, i); }
[a-zA-Z]+ {String s = new String(yytext()); System.out.println(s); return new Symbol(sym.ID, s); }
[ \t\r\n\f] {/* ignore white space */}
/*
 * error handling
 */
. {System.err.println("Illegal character: "+yytext());}
