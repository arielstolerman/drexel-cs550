# Assignment 4
# Group 1

# RAM simulator path
RAM=ram.exe
# RAM memory argument
ifndef RAM_MEM
	M=
else
	M=-m $$RAM_MEM
endif

.PHONY : view compile view-trans view-link run clean

### view ###

view : Program.java interpreterext.cup interpreterext.flex
	@more Program.java interpreterext.cup interpreterext.flex

view-trans : trans.txt
	@echo === trans.txt ===
	@cat trans.txt
	@echo

view-link : linked.txt linked_mem.txt
	@echo === linked_mem.txt ===
	@cat linked_mem.txt
	@echo
	@echo
	@echo === linked.txt ===
	@cat linked.txt
	@echo


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

run : linked.txt linked_mem.txt
	@$(RAM) $(M) linked.txt linked_mem.txt


### clean ###

clean :
	@rm -rf *.class
	@rm -rf sym.java parser.java Yylex.java
	@rm -rf trans.txt linked*.txt
