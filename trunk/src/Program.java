/* this is the cup file for the mini language
 * at http://www.cs.drexel.edu/~jjohnson/2010-11/spring/cs550/lectures/lec2c.html *
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

	public Integer eval(HashMap<String, Integer> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
		return new Integer(0);
	}
}

class Ident extends Expr {

	private String name;

	public Ident(String s) {
		name = s;
	}

	public Integer eval(HashMap<String, Integer> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
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

	public Integer eval(HashMap<String, Integer> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
		return value;
	}
}

class Times extends Expr {

	private Expr expr1,  expr2;

	public Times(Expr op1, Expr op2) {
		expr1 = op1;
		expr2 = op2;
	}

	public Integer eval(HashMap<String, Integer> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
		return new Integer(expr1.eval(nametable, functiontable, var) * expr2.eval(nametable, functiontable, var));
	}
}

class Plus extends Expr {

	private Expr expr1,  expr2;

	public Plus(Expr op1, Expr op2) {
		expr1 = op1;
		expr2 = op2;
	}

	public Integer eval(HashMap<String, Integer> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
		return new Integer(expr1.eval(nametable, functiontable, var).intValue() + expr2.eval(nametable, functiontable, var).intValue());
	}
}

class Minus extends Expr {

	private Expr expr1,  expr2;

	public Minus(Expr op1, Expr op2) {
		expr1 = op1;
		expr2 = op2;
	}

	public Integer eval(HashMap<String, Integer> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
		return new Integer(expr1.eval(nametable, functiontable, var) - expr2.eval(nametable, functiontable, var));
	}
}

//added for 2c
class FunctionCall extends Expr {

	private String funcid;
	private ExpressionList explist;

	public FunctionCall(String id, ExpressionList el) {
		funcid = id;
		explist = el;
	}

	public Integer eval(HashMap<String, Integer> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
		return functiontable.get(funcid).apply(nametable, functiontable, var, explist);
	}
}

abstract class Statement {

	public Statement() {
	}

	public void eval(HashMap<String, Integer> nametable, HashMap<String, Proc> functiontable, LinkedList var) throws Exception {
	}
}

// added for 2c
class DefineStatement extends Statement {

	private String name;
	private Proc proc;
	private ParamList paramlist;
	private StatementList statementlist;

	public DefineStatement(String id, Proc process) {
		name = id;
		proc = process;
	}

	public void eval(HashMap<String, Integer> nametable, HashMap<String, Proc> functable, LinkedList var) {
		// get the named proc object from the function table.
		//System.out.println("Adding Process:"+name+" to Functiontable");
		functable.put(name, proc);
	}
}

class ReturnStatement extends Statement {

	private Expr expr;

	public ReturnStatement(Expr e) {
		expr = e;
	}

	public void eval(HashMap<String, Integer> nametable, HashMap<String, Proc> functiontable, LinkedList var) throws Exception {
		//Java can't throw exceptions of numbers, so we'll convert it to a string
		//and then on the other end we'll reconvert back to Integer..
		throw new Exception(Integer.toString(expr.eval(nametable, functiontable, var)));
	}
}

class AssignStatement extends Statement {

	private String name;
	private Expr expr;

	public AssignStatement(String id, Expr e) {
		name = id;
		expr = e;
	}

	public void eval(HashMap<String, Integer> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
		/* add name to the statementlist of variable names */
		if (!var.contains(name)) {
			var.add(name);
			//insert the variable with the specified name into the table with the 
			// evaluated result (which must be an integer
		}
		nametable.put(name, expr.eval(nametable, functiontable, var));
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

	public void eval(HashMap<String, Integer> nametable, HashMap<String, Proc> functiontable, LinkedList var) throws Exception {
		if (expr.eval(nametable, functiontable, var) > 0) {
			stmtlist1.eval(nametable, functiontable, var);
		} else {
			stmtlist2.eval(nametable, functiontable, var);
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

	public void eval(HashMap<String, Integer> nametable, HashMap<String, Proc> functiontable, LinkedList var) throws Exception {
		while (expr.eval(nametable, functiontable, var) > 0) {
			stmtlist.eval(nametable, functiontable, var);
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

	public void eval(HashMap<String, Integer> nametable, HashMap<String, Proc> functiontable, LinkedList var) throws Exception {
		do {
			sl.eval(nametable, functiontable, var);
		} while (expr.eval(nametable, functiontable, var) > 0);

	}
}

//added for 2c
class ParamList {

	private List<String> parameterlist;

	public ParamList(String name) {
		parameterlist = new LinkedList<String>();
		parameterlist.add(name);
	}

	public ParamList(String name, ParamList parlist) {
		parameterlist = parlist.getParamList();
		parameterlist.add(name);
	}

	public List<String> getParamList() {
		return parameterlist;
	}
}

// Added for 2c
class ExpressionList {

	private LinkedList<Expr> list;

	public ExpressionList(Expr ex) {
		list = new LinkedList<Expr>();
		list.add(ex);
	}

	public ExpressionList(Expr ex, ExpressionList el) {
		list = new LinkedList<Expr>();
		//we need ot add the expression to the front of the list
		list.add(0, ex);

	}

	public List<Expr> getExpressions() {
		return list;
	}
}

class StatementList {

	private LinkedList<Statement> statementlist;

	public StatementList(Statement statement) {
		statementlist = new LinkedList<Statement>();
		statementlist.add(statement);
	}

	public void eval(HashMap<String, Integer> nametable, HashMap<String, Proc> functiontable, LinkedList var) throws Exception {


		for (Statement stmt : statementlist) {
			stmt.eval(nametable, functiontable, var);

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

class Proc {

	private ParamList parameterlist;
	private StatementList stmtlist;

	public Proc(ParamList pl, StatementList sl) {
		parameterlist = pl;
		stmtlist = sl;
	}

	public int apply(HashMap<String, Integer> nametable, HashMap<String, Proc> functiontable, LinkedList var, ExpressionList expressionlist) {
		//System.out.println("Executing Proceedure");
		HashMap<String, Integer> newnametable = new HashMap<String, Integer>();

		// bind parameters in new name table
		// we need to get the underlying List structure that the ParamList uses...
		Iterator<String> p = parameterlist.getParamList().iterator();
		Iterator<Expr> e = expressionlist.getExpressions().iterator();

		if (parameterlist.getParamList().size() != expressionlist.getExpressions().size()) {
			System.out.println("Param count does not match");
			System.exit(1);
		}
		while (p.hasNext() && e.hasNext()) {

			// assign the evaluation of the expression to the parameter name.
			newnametable.put(p.next(), e.next().eval(nametable, functiontable, var));
			//System.out.println("Loading Nametable for procedure with: "+p+" = "+nametable.get(p));

		}
		// evaluate function body using new name table and 
		// old function table
		// eval statement list and catch return
		//System.out.println("Beginning Proceedure Execution..");
		try {
			stmtlist.eval(newnametable, functiontable, var);
		} catch (Exception result) {
			// Note, the result shold contain the proceedure's return value as a String
			//System.out.println("return value = "+result.getMessage());
			return Integer.parseInt(result.getMessage());
		}
		System.out.println("Error:  no return value");
		System.exit(1);
		// need this or the compiler will complain, but should never
		// reach this...
		return 0;
	}
}

class Program {

	private StatementList stmtlist;

	public Program(StatementList list) {
		stmtlist = list;
	}

	public void eval(HashMap<String, Integer> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
		try {
			stmtlist.eval(nametable, functiontable, var);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void dump(HashMap<String, Integer> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
		//System.out.println(hm.values());
		System.out.println("Dumping out all the variables...");
		if (nametable != null) {
			for (String name : nametable.keySet()) {
				System.out.println(name + "=" + nametable.get(name));
			}
		}
		if (functiontable != null) {
			for (String name : functiontable.keySet()) {
				System.out.println("Function: " + name + " defined...");
			}
		}
	}
}
