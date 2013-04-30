# Assignment 3
# Group 1

.PHONY : view compile view-trans view-link view-op run run-op clean

### view ###

view : Program.java interpreterext.cup interpreterext.flex
	@more Program.java interpreterext.cup interpreterext.flex

view-trans : trans.txt
	@cat trans.txt

view-link : link.txt
	@cat link.txt

view-op : op.txt
	@cat op.txt

view-trans view-link view-op : compile

### compile ###

compile : Yylex.class parser.class sym.class Program.class
	@java -classpath ".;java-cup-11a.jar" parser

Yylex.class parser.class sym.class Program.class : Yylex.java parser.java sym.java Program.java
	@javac -classpath java-cup-11a.jar Yylex.java parser.java sym.java Program.java

Yylex.java : interpreterext.flex
	@java -jar jflex.jar interpreterext.flex

parser.java sym.java : interpreterext.cup
	@java -jar java-cup-11a.jar interpreterext.cup


### run ###

run : build-ram link.txt
	@ram link.txt ???

run-op : build-ram op.txt
	@ram op.txt ???

build-ram : ram/inst.h ram/ram.h ram/ram.cpp ram/main.cpp
	@g++ ram/inst.h ram/ram.h ram/ram.cpp ram/main.cpp -o ram


### clean ###

clean :
	@rm -rf *.class
	@rm -rf sym.java parser.java Yylex.java
	#@rm -rf ram ram.exe *.gch
	@rm -rf trans.txt link*.txt op*.txt
