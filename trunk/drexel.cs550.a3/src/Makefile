# Assignment 3
# Group 1

.PHONY : view compile view-trans view-link view-op run run-op clean

### view ###

view : Program.java interpreterext.cup interpreterext.flex
	@more Program.java interpreterext.cup interpreterext.flex

view-trans : trans.txt
	@cat trans.txt

view-link : linked.txt
	@cat linked.txt

view-op : opt_linked.txt
	@cat opt_linked.txt

trans.txt linked.txt linked_mem.txt opt.txt opt_linked.txt opt_linked_mem.txt : compile

### compile ###

compile : build
	@java -classpath ".;java-cup-11a.jar" parser

build : Yylex.class parser.class sym.class Program.class

Yylex.class parser.class sym.class Program.class : Yylex.java parser.java sym.java Program.java
	@javac -classpath java-cup-11a.jar Yylex.java parser.java sym.java Program.java

Yylex.java : interpreterext.flex
	@java -jar jflex.jar -q interpreterext.flex

parser.java sym.java : interpreterext.cup
	@java -jar java-cup-11a.jar -nosummary -nowarn interpreterext.cup


### run ###

run : ram.exe linked.txt linked_mem.txt
	@ram.exe linked.txt linked_mem.txt

run-op : build-ram opt_linked.txt opt_linked_mem.txt
	@ram.exe opt_linked.txt opt_linked_mem.txt

ram.exe : ram_src/inst.h ram_src/ram.h ram_src/ram.cpp ram_src/main.cpp
	@g++ ram_src/inst.h ram_src/ram.h ram_src/ram.cpp ram_src/main.cpp -o ram
	@chmod +x ram.exe


### clean ###

clean :
	@rm -rf *.class
	@rm -rf sym.java parser.java Yylex.java
	#@rm -rf ram.exe *.gch
	@rm -rf trans.txt linked*.txt opt*.txt
