# Assignment 6
# Group 1
#

VIEWER=more

.PHONY : view compile-static compile-dynamic run-static run-dynamic clean


# view
view : Program.java interpreterext.cup interpreterext.flex
	@$(VIEWER) Program.java interpreterext.cup interpreterext.flex


# compile

compile-static : compile

compile-dynamic : compile

compile : Yylex.class parser.class sym.class Program.class

Yylex.class parser.class sym.class Program.class : Yylex.java parser.java sym.java Program.java
	@javac -classpath java-cup-11a.jar Yylex.java parser.java sym.java Program.java

Yylex.java : interpreterext.flex
	@java -jar jflex.jar interpreterext.flex

parser.java sym.java : interpreterext.cup
	@java -jar java-cup-11a.jar interpreterext.cup


# run

run-static : compile
	@java -classpath ".;java-cup-11a.jar" parser -s

run-dynamic : compile
	@java -classpath ".;java-cup-11a.jar" parser -d


# clean

clean :
	@rm -rf *.class
	@rm -rf sym.java parser.java Yylex.java
