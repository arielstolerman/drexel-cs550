# Assignment 6
# Group 1
#

VIEWER=more

.PHONY : view-part1 build run-part1 view-func1 view-func2 view-part2 run-part2 clean

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
