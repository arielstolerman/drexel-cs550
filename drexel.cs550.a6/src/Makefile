# Assignment 6
# Group 1
#

VIEWER=more

.PHONY : view compile-static compile-dynamic run-static run-dynamic clean

# view
view : Program.java interpreterext.cup interpreterext.flex
	@$(VIEWER) Program.java interpreterext.cup interpreterext.flex


### part 1 ####

run-part1 : Yylex.class parser.class sym.class Program.class
	@java -classpath ".;java-cup-11a.jar" parser

Yylex.class parser.class sym.class Program.class : Yylex.java parser.java sym.java Program.java
	@javac -classpath java-cup-11a.jar Yylex.java parser.java sym.java Program.java

Yylex.java : interpreterext.flex
	@java -jar jflex.jar interpreterext.flex

parser.java sym.java : interpreterext.cup
	@java -jar java-cup-11a.jar interpreterext.cup

clean :
	@rm -rf *.class
	@rm -rf sym.java parser.java Yylex.java
