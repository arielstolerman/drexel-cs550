/* this is the cup file for the mini language
 * at http://www.cs.drexel.edu/~jjohnson/2006-07/winter/cs360/lectures/lec6.html *
 * created by Xu, 2/5/07
 * 
 * Modified by Mike Kopack for CS550, 2009 Spring Qtr.
 * Should be at the same level of completeness as the Lecture 2c
 * C++ version.
 */

import java.util.*;

import java_cup.symbol;

// =============================================================================
// added classes and enums
// =============================================================================

/**
 * Enum for symbol table entry type
 */
enum SymbolType {
	TEMP,
	VAR,
	CONST,
	LABEL
}

/**
 * Class for symbol table entry values, which includes value (can be unknown
 * before compilation), type and address (unknown before compilation).
 */
class SymbolValue {
	
	// global counters
	private static int TEMP_COUNTER = 0;
	private static int LABEL_COUNTER = 0;
	
	// fields
	Integer value;
	SymbolType type;
	Integer addr;
	
	/**
	 * Private constructor from value, type and address.
	 * @param value
	 * @param type
	 * @param addr
	 */
	private SymbolValue(Integer value, SymbolType type, Integer addr) {
		this.value = value;
		this.type = type;
		this.addr = addr;
	}
	
	// mapping of constants to names
	private static String[] CONST_TO_NAME = new String[]{
		"ZERO",
		"ONE",
		"TWO",
		"THREE",
		"FOUR",
		"FIVE",
		"SIX",
		"SEVEN",
		"EIGHT",
		"NINE",
	};
	
	// factory functions
	
	/**
	 * Adds a new temporary entry to the symbol table, w.r.t the global
	 * temporary counter.
	 * @param symbolTable
	 * @param value
	 * @return
	 */
	public static String addTemp(HashMap<String,SymbolValue> symbolTable) {
		String name = "T" + TEMP_COUNTER++;
		symbolTable.put(name, new SymbolValue(null, SymbolType.TEMP, null));
		return name;
	}
	
	/**
	 * Adds a new label entry to the symbol table, w.r.t the global label
	 * counter.
	 * @param symbolTable
	 * @param label
	 * @return
	 */
	public static String getLabel(HashMap<String,SymbolValue> symbolTable) {
		String name = "L" + LABEL_COUNTER++;
		symbolTable.put(name, new SymbolValue(null, SymbolType.TEMP, null));
		return name;
	}
	
	/**
	 * Checks if the given variable name exists in the symbol table, and adds it
	 * if not. Returns the variable name.
	 * @param symbolTable
	 * @param var
	 * @return
	 */
	public static String updateVar(HashMap<String,SymbolValue> symbolTable,
			String var) {
		if (!symbolTable.containsKey(var))
			symbolTable.put(var, new SymbolValue(null, SymbolType.VAR, null));
		return var;			
	}
	
	/**
	 * Updates the given symbol table to hold the given const entry, in case it
	 * does not already contains it. Constant values are given names
	 * corresponding to their values (e.g. 23 will be named TWO_THREE).
	 * Returns the name of the symbol table entry key.
	 * @param symbolTable
	 * @param value
	 * @return
	 */
	public static String updateConst(HashMap<String,SymbolValue> symbolTable, int value) {
		// map value to corresponding name
		String name = "";
		if (value == 0)
			name = CONST_TO_NAME[0];
		else {
			String valStr = value + "";
			int len = valStr.length();
			for (int i = 0; i < len; i++)
				name += CONST_TO_NAME
				[Integer.parseInt(valStr.substring(i,i+1))] + "_";
			name = name.substring(0,name.length() - 1);
		}
		
		// if name does not exist in symbol table, create entry
		if (!symbolTable.containsKey(name))
			symbolTable.put(name, new SymbolValue(value, SymbolType.CONST, null));
		
		return name;
	}
}

abstract class Component {
	
}

// =============================================================================

class Expr {

	public Expr() {
	}

	public Integer eval(HashMap<String, Integer> nametable, LinkedList var) {
		return new Integer(0);
	}
}

class Ident extends Expr {

	private String name;

	public Ident(String s) {
		name = s;
	}

	public Integer eval(HashMap<String, Integer> nametable, LinkedList var) {
		return new Integer(nametable.get(name).toString());
	}
}

class Number extends Expr {

	private Integer value;

	public Number(int n) {
		value = new Integer(n);
	}

	public Number(Integer n) {
		value = n;
	}

	public Integer eval(HashMap<String, Integer> nametable, LinkedList var) {
		return value;
	}
}

class Times extends Expr {

	private Expr expr1,  expr2;

	public Times(Expr op1, Expr op2) {
		expr1 = op1;
		expr2 = op2;
	}

	public Integer eval(HashMap<String, Integer> nametable, LinkedList var) {
		return new Integer(expr1.eval(nametable, var) * expr2.eval(nametable, var));
	}
}

class Plus extends Expr {

	private Expr expr1,  expr2;

	public Plus(Expr op1, Expr op2) {
		expr1 = op1;
		expr2 = op2;
	}

	public Integer eval(HashMap<String, Integer> nametable, LinkedList var) {
		return new Integer(expr1.eval(nametable, var).intValue() + expr2.eval(nametable, var).intValue());
	}
}

class Minus extends Expr {

	private Expr expr1,  expr2;

	public Minus(Expr op1, Expr op2) {
		expr1 = op1;
		expr2 = op2;
	}

	public Integer eval(HashMap<String, Integer> nametable, LinkedList var) {
		return new Integer(expr1.eval(nametable, var) - expr2.eval(nametable, var));
	}
}

abstract class Statement {

	public Statement() {
	}

	public void eval(HashMap<String, Integer> nametable, LinkedList var) throws Exception {
	}
}

class AssignStatement extends Statement {

	private String name;
	private Expr expr;

	public AssignStatement(String id, Expr e) {
		name = id;
		expr = e;
	}

	public void eval(HashMap<String, Integer> nametable, LinkedList var) {
		/* add name to the statementlist of variable names */
		if (!var.contains(name)) {
			var.add(name);
			//insert the variable with the specified name into the table with the 
			// evaluated result (which must be an integer
		}
		nametable.put(name, expr.eval(nametable, var));
	}
}

class IfStatement extends Statement {

	private Expr expr;
	private StatementList stmtlist1,  stmtlist2;

	public IfStatement(Expr e, StatementList list1, StatementList list2) {
		expr = e;
		stmtlist1 = list1;
		stmtlist2 = list2;
	}

	public IfStatement(Expr e, StatementList list) {
		expr = e;
		stmtlist1 = list;
	}

	public void eval(HashMap<String, Integer> nametable, LinkedList var) throws Exception {
		if (expr.eval(nametable, var) > 0) {
			stmtlist1.eval(nametable, var);
		} else {
			stmtlist2.eval(nametable, var);
		}
	}
}

class WhileStatement extends Statement {

	private Expr expr;
	private StatementList stmtlist;

	public WhileStatement(Expr e, StatementList list) {
		expr = e;
		stmtlist = list;
	}

	public void eval(HashMap<String, Integer> nametable, LinkedList var) throws Exception {
		while (expr.eval(nametable, var) > 0) {
			stmtlist.eval(nametable, var);
		}
	}
}

class RepeatStatement extends Statement {

	private Expr expr;
	private StatementList sl;

	public RepeatStatement(StatementList list, Expr e) {
		expr = e;
		sl = list;
	}

	public void eval(HashMap<String, Integer> nametable, LinkedList var) throws Exception {
		do {
			sl.eval(nametable, var);
		} while (expr.eval(nametable, var) > 0);

	}
}

class StatementList {

	private LinkedList<Statement> statementlist;

	public StatementList(Statement statement) {
		statementlist = new LinkedList<Statement>();
		statementlist.add(statement);
	}

	public void eval(HashMap<String, Integer> nametable, LinkedList var) throws Exception {


		for (Statement stmt : statementlist) {
			stmt.eval(nametable, var);

		}
	}

	public void insert(Statement s) {
		// we need to add it to the front of the list
		statementlist.add(0, s);
	}

	public LinkedList<Statement> getStatements() {
		return statementlist;
	}
}

class Program {

	private StatementList stmtlist;

	public Program(StatementList list) {
		stmtlist = list;
	}

	public void eval(HashMap<String, Integer> nametable, LinkedList var) {
		try {
			stmtlist.eval(nametable, var);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void dump(HashMap<String, Integer> nametable, LinkedList var) {
		//System.out.println(hm.values());
		System.out.println("Dumping out all the variables...");
		if (nametable != null) {
			for (String name : nametable.keySet()) {
				System.out.println(name + "=" + nametable.get(name));
			}
		}
	}
}
