/* this is the cup file for the mini language
 * at http://www.cs.drexel.edu/~jjohnson/2006-07/winter/cs360/lectures/lec6.html *
 * created by Xu, 2/5/07
 * 
 * Modified by Mike Kopack for CS550, 2009 Spring Qtr.
 * Should be at the same level of completeness as the Lecture 2c
 * C++ version.
 */

import java.util.*;

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
