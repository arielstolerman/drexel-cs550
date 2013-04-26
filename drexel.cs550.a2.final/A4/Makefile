# Assignment 2
# Group 1

VIEWER=more

.PHONY : view-part1 build run-part1 view-func1 view-func2 view-part2 run-part2 clean


#############
### build ###
#############

build : build-part1 build-part2

# part 1

build-part1 : Yylex1.class parser1.class sym1.class Part1.class

Yylex1.class parser1.class sym1.class Part1.class : Yylex1.java parser1.java sym1.java Part1.java
	@javac -classpath java-cup-11a.jar Yylex1.java parser1.java sym1.java Part1.java

parser1.java sym1.java : interpreterext1.cup
	@java -jar java-cup-11a.jar -parser parser1 -symbols sym1 interpreterext1.cup

interpreterext1.cup : interpreterext.cup
	@sed 's/\[PART_NUM\]/1/g' interpreterext.cup > interpreterext1.cup
	
Yylex1.java : interpreterext1.flex
	@java -jar jflex.jar interpreterext1.flex

interpreterext1.flex : interpreterext.flex
	@sed 's/\[PART_NUM\]/1/g' interpreterext.flex > interpreterext1.flex

# part 2

build-part1 : Yylex2.class parser2.class sym2.class Part2.class

Yylex2.class parser2.class sym2.class Heap.class Part2.class : Yylex2.java parser2.java sym2.java Part2.java
	@javac -classpath java-cup-11a.jar Yylex2.java parser2.java sym2.java Heap.java Part2.java

parser2.java sym2.java : interpreterext2.cup
	@java -jar java-cup-11a.jar -parser parser2 -symbols sym2 interpreterext2.cup

interpreterext2.cup : interpreterext.cup
	@sed 's/\[PART_NUM\]/2/g' interpreterext.cup > interpreterext2.cup

Yylex2.java : interpreterext2.flex
	@java -jar jflex.jar interpreterext2.flex

interpreterext2.flex : interpreterext.flex
	@sed 's/\[PART_NUM\]/2/g' interpreterext.flex > interpreterext2.flex


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
view-part1 : Part1.java interpreterext1.cup interpreterext1.flex
	@$(VIEWER) Part1.java interpreterext1.cup interpreterext1.flex

# run
run-part1 : Yylex1.class parser1.class sym1.class Part1.class
	@java -classpath ".;java-cup-11a.jar" parser1


##############
### part 2 ###
##############

# view
view-part2 : Part2.java Heap.java interpreterext2.cup interpreterext2.flex
	@$(VIEWER) Part2.java Heap.java interpreterext2.cup interpreterext2.flex

# run
run-part2 : Yylex2.class parser2.class sym2.class Heap.class Part2.class
	@java -classpath ".;java-cup-11a.jar" parser2


#############
### clean ###
#############

clean :
	@rm -rf *.class
	@rm -rf interpreterext1.flex Yylex1.java interpreterext1.cup sym1.java parser1.java
	@rm -rf interpreterext2.flex Yylex2.java interpreterext2.cup sym2.java parser2.java
