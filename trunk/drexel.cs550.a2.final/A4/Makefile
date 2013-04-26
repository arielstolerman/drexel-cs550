# Assignment 2
# Group 1

VIEWER=more

.PHONY : view-part1 build run-part1 view-func1 view-func2 view-part2 run-part2 clean


######################
### build (common) ###
######################

build : build-part1 build-part2

# shared
Yylex.java : interpreterext.flex
	@java -jar jflex.jar -q interpreterext.flex

parser.java sym.java : interpreterext.cup
	@java -jar java-cup-11a.jar -nosummary interpreterext.cup


#############################
### list length functions ###
#############################

view-func1 : listlen_iter.p
	@$(VIEWER) listlen_iter.p

view-func2 : listlen_rec.p
	@$(VIEWER) listlen_rec.p


##############
### part 1 ###
##############

# view
view-part1 : part1/Part1.java interpreterext.cup interpreterext.flex
	@$(VIEWER) part1/Part1.java interpreterext.cup interpreterext.flex

# run
run-part1 : build-part1
	@java -classpath "./part1/;java-cup-11a.jar" parser

# build
build-part1 : part1/Yylex.class part1/parser.class part1/sym.class part1/Part1.class

part1/Yylex.class part1/parser.class part1/sym.class part1/Part1.class : Yylex.java parser.java sym.java part1/Part1.java
	@javac -classpath java-cup-11a.jar -d part1 Yylex.java parser.java sym.java part1/Part1.java

part1/Yylex.java part1/parser.java part1/sym.java : Yylex.java parser.java sym.java


##############
### part 2 ###
##############

# view
view-part2 : part2/Part2.java part2/Heap.java interpreterext.cup interpreterext.flex
	@$(VIEWER) part2/Part2.java part2/Heap.java interpreterext.cup interpreterext.flex

# run
run-part2 : build-part2
	@java -classpath "./part2/;java-cup-11a.jar" parser

# build
build-part2 : part2/Yylex.class part2/parser.class part2/sym.class part2/Heap.class part2/Part2.class

part2/Yylex.class part2/parser.class part2/sym.class part2/Heap.class part2/Part2.class : Yylex.java parser.java sym.java part2/Heap.java part2/Part2.java
	@javac -classpath java-cup-11a.jar -d part2 Yylex.java parser.java sym.java part2/Heap.java part2/Part2.java

part2/Yylex.java part2/parser.java part2/sym.java : Yylex.java parser.java sym.java


#############
### clean ###
#############

clean :
	@rm -rf */*.class
	@rm -rf Yylex.java sym.java parser.java
