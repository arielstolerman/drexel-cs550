# Assignment 2
# Group 1
#

VIEWER=more

.PHONY : view-part1 build run-part1 view-func1 view-func2 view-part2 run-part2 clean

### build ###

# part 1
Yylex.class parser.class sym.class Part1.class : Yylex.java parser.java sym.java Part1.java
	@javac -classpath java-cup-11a.jar Yylex.java parser.java sym.java Part1.java

# part 2
Yylex.class parser.class sym.class Heap.class Part2.class : Yylex.java parser.java sym.java Heap.java Part2.java
	@javac -classpath java-cup-11a.jar Yylex.java parser.java sym.java Heap.java Part2.java

# shared
Yylex.java : interpreterext.flex
	@java -jar ../lib/jflex.jar interpreterext.flex

parser.java sym.java : interpreterext.cup
	@java -jar java-cup-11a.jar interpreterext.cup

### list length functions ###

view-func1 : listlen_iter.p
	@$(VIEWER) listlen_iter.p

view-func2 : listlen_rec.p
	@$(VIEWER) listlen_rec.p

### part 1 ####

# view
view-part1 : Part1.java interpreterext.cup interpreterext.flex
	@$(VIEWER) Part1.java interpreterext.cup interpreterext.flex

# run
run-part1 : Yylex.class parser1.class sym1.class Part1.class
	@java -classpath ".;java-cup-11a.jar" parser1

### part 2 ###

# view
view-part2 : Part2.java Heap.java interpreterext.cup interpreterext.flex
	@$(VIEWER) Part2.java Heap.java interpreterext.cup interpreterext.flex

# run
run-part2 : Yylex.class parser2.class sym2.class Heap.class Part2.class
	@java -classpath ".;java-cup-11a.jar" parser2

### clean ###
clean :
	@rm -rf *.class
	@rm -rf sym1.java parser1.java sym2.java parser2.java Yylex.java
