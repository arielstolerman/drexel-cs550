# Assignment 2
# Group 1
#

VIEWER=more

.PHONY : view-part1 build run-part1 view-func1 view-func2 view-part2 run-part2 clean

### part 2 ####

run-part2 : Yylex.class parser.class sym.class Heap.class Part2.class
	@java -classpath ".;../lib/java-cup-11a.jar" parser

Yylex.class parser.class sym.class Heap.class Part2.class : Yylex.java parser.java sym.java Heap.java Part2.java
	@javac -classpath ../lib/java-cup-11a.jar Yylex.java parser.java sym.java Heap.java Part2.java

Yylex.java : interpreterext.flex
	@java -jar ../lib/jflex.jar interpreterext.flex

parser.java sym.java : interpreterext.cup
	@java -jar ../lib/java-cup-11a.jar interpreterext.cup

clean :
	@rm -rf *.class
	@rm -rf sym.java parser.java Yylex.java
