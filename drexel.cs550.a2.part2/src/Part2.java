/* this is the cup file for the mini language
 * at http://www.cs.drexel.edu/~jjohnson/2010-11/spring/cs550/lectures/lec2c.html *
 * created by Xu, 2/5/07
 * 
 * Modified by Mike Kopack for CS550, 2009 Spring Qtr.
 * Should be at the same level of completeness as the Lecture 2c
 * C++ version.
 */

import java.util.*;

abstract class Expr {

	public abstract Elem eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var);
	
	@Override
	public abstract String toString();
}

class Lst extends Expr {

	private List<Expr> list;

	public Lst() {
		list = new LinkedList<>();
	}

	public Lst(ExpressionList el) {
		list = new LinkedList<>();
		list.addAll(el.getExpressions());
	}

	public Lst(Expr ex) {
		this();
		list.add(ex);
	}

	public Lst prepend(Expr ex) {
		list.add(0, ex);
		return this;
	}

	@Override
	public Elem eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		return buildList(list, nametable, functiontable, var);
	}

	public List<Expr> getList() {
		return list;
	}
	
	@Override
	public String toString() {
		return list.toString();
	}
	
	/**
	 * Recursively builds the list of elements using dynamic memory allocation
	 * on the heap. If the list of expression is empty, returns null.
	 */
	public Elem buildList(List<Expr> list, HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		if (list.size() == 0)
			return null;
		Elem car = list.get(0).eval(nametable, functiontable, var);
		Elem cdr = list.size() == 1 ? null :
			buildList(list.subList(1, list.size()), nametable, functiontable, var);
		return Program.HEAP.cons(car, cdr, nametable);
	}
}

class Ident extends Expr {

	private String name;

	public Ident(String s) {
		name = s;
	}

	public Elem eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		return nametable.get(name);
	}
	
	@Override
	public String toString() {
		return name;
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

	public Elem eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		return new Elem(value);
	}
	
	@Override
	public String toString() {
		return value + "";
	}
}

class Times extends Expr {

	private Expr expr1, expr2;

	public Times(Expr op1, Expr op2) {
		expr1 = op1;
		expr2 = op2;
	}

	public Elem eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		return new Elem(expr1.eval(nametable, functiontable, var).getValue()
				* expr2.eval(nametable, functiontable, var).getValue());
	}
	
	@Override
	public String toString() {
		return expr1 + " TIMES " + expr2;
	}
}

class Plus extends Expr {

	private Expr expr1, expr2;

	public Plus(Expr op1, Expr op2) {
		expr1 = op1;
		expr2 = op2;
	}

	public Elem eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		return new Elem(expr1.eval(nametable, functiontable, var).getValue()
				+ expr2.eval(nametable, functiontable, var).getValue());
	}
	
	@Override
	public String toString() {
		return expr1 + " PLUS " + expr2;
	}
}

class Minus extends Expr {

	private Expr expr1, expr2;

	public Minus(Expr op1, Expr op2) {
		expr1 = op1;
		expr2 = op2;
	}

	public Elem eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		return new Elem(expr1.eval(nametable, functiontable, var).getValue()
				- expr2.eval(nametable, functiontable, var).getValue());
	}
	
	@Override
	public String toString() {
		return expr1 + " MINUS " + expr2;
	}
}

// added for 2c
class FunctionCall extends Expr {

	private String funcid;
	private ExpressionList explist;

	public FunctionCall(String id, ExpressionList el) {
		funcid = id;
		explist = el;
	}

	public Elem eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		return functiontable.get(funcid).apply(nametable, functiontable, var,
				explist);
	}
	
	@Override
	public String toString() {
		return funcid + " " + explist.getExpressions().toString()
				.replace("[","( ").replace("]"," )");
	}
}

class Concat extends Expr {

	private Expr list1;
	private Expr list2;

	public Concat(Expr list1, Expr list2) {
		this.list1 = list1;
		this.list2 = list2;
	}

	@Override
	public Elem eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		List<Elem> res = new LinkedList<>();
		res.addAll(list1.eval(nametable, functiontable, var).getList());
		res.addAll(list2.eval(nametable, functiontable, var).getList());
		return new Elem(res);
	}
	
	@Override
	public String toString() {
		return list1 + " || " + list2;
	}
}

class Cons extends Expr {
	
	private Expr exp;
	private Expr list;

	public Cons(Expr exp, Expr list) {
		this.exp = exp;
		this.list = list;
	}

	@Override
	public Elem eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		Elem expElem = exp.eval(nametable, functiontable, var);
		Elem listElem = list.eval(nametable, functiontable, var);
		LinkedList<Elem> res = new LinkedList<>();
		res.add(expElem);
		res.addAll(listElem.getList());
		return new Elem(res);
	}
	
	@Override
	public String toString() {
		return "CONS ( " + exp + ", " + list + " )";
	}
}

class Car extends Expr {

	private Expr list;

	public Car(Expr list) {
		this.list = list;
	}

	@Override
	public Elem eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		return list.eval(nametable, functiontable, var).getList().get(0);
	}
	
	@Override
	public String toString() {
		return "CAR ( " + list + " )";
	}
}

class Cdr extends Expr {

	private Expr list;

	public Cdr(Expr list) {
		this.list = list;
	}

	@Override
	public Elem eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		List<Elem> listEval = list.eval(nametable, functiontable, var)
				.getList();
		LinkedList<Elem> res = new LinkedList<>();
		res.addAll(listEval.subList(1, listEval.size()));
		return new Elem(res);
	}
	
	@Override
	public String toString() {
		return "CDR ( " + list + " )";
	}
}

class NullP extends Expr {

	private Expr list;

	public NullP(Expr list) {
		this.list = list;
	}

	@Override
	public Elem eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		return list.eval(nametable, functiontable, var).getList().isEmpty() ?
				new Elem(1) : new Elem(0);
	}
	
	@Override
	public String toString() {
		return "NULLP ( " + list + " )";
	}
}

class IntP extends Expr {

	private Expr exp;

	public IntP(Expr exp) {
		this.exp = exp;
	}

	@Override
	public Elem eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		return exp.eval(nametable, functiontable, var).isInt() ? new Elem(1)
				: new Elem(0);
	}
	
	@Override
	public String toString() {
		return "INTP ( " + exp + " )";
	}
}

class ListP extends Expr {

	private Expr exp;

	public ListP(Expr exp) {
		this.exp = exp;
	}

	@Override
	public Elem eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		return exp.eval(nametable, functiontable, var).isList() ? new Elem(1)
				: new Elem(0);
	}
	
	@Override
	public String toString() {
		return "LISTP ( " + exp + " )";
	}
}

abstract class Statement {

	public Statement() {
	}

	public void eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws ReturnValue {
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

	public void eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functable, LinkedList var) {
		// get the named proc object from the function table.
		// System.out.println("Adding Process:"+name+" to Functiontable");
		functable.put(name, proc);
	}
}

class ReturnStatement extends Statement {

	private Expr expr;

	public ReturnStatement(Expr e) {
		expr = e;
	}

	public void eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws ReturnValue {
		// Java can't throw exceptions of numbers, so we'll convert it to a
		// string
		// and then on the other end we'll reconvert back to Integer..
		throw new ReturnValue(expr.eval(nametable, functiontable, var));
	}
}

class AssignStatement extends Statement {

	private String name;
	private Expr expr;

	public AssignStatement(String id, Expr e) {
		name = id;
		expr = e;
	}

	public void eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		/* add name to the statementlist of variable names */
		if (!var.contains(name)) {
			var.add(name);
			// insert the variable with the specified name into the table with
			// the
			// evaluated result (which must be an integer
		}
		nametable.put(name, expr.eval(nametable, functiontable, var));
	}
	
	@Override
	public String toString() {
		return name + " := " + expr;
	}
}

class IfStatement extends Statement {

	private Expr expr;
	private StatementList stmtlist1, stmtlist2;

	public IfStatement(Expr e, StatementList list1, StatementList list2) {
		expr = e;
		stmtlist1 = list1;
		stmtlist2 = list2;
	}

	public IfStatement(Expr e, StatementList list) {
		expr = e;
		stmtlist1 = list;
	}

	public void eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws ReturnValue {
		if (expr.eval(nametable, functiontable, var).getInt() > 0) {
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

	public void eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws ReturnValue {
		while (expr.eval(nametable, functiontable, var).getInt() > 0) {
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

	public void eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws ReturnValue {
		do {
			sl.eval(nametable, functiontable, var);
		} while (expr.eval(nametable, functiontable, var).getInt() > 0);

	}
}

// added for 2c
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
		list.add(ex);
		list.addAll(el.getExpressions());
	}

	public List<Expr> getExpressions() {
		return list;
	}
	
	@Override
	public String toString() {
		return list.toString();
	}
}

/**
 * Wrapper for statement "return" values
 */
class ReturnValue extends Exception {

	private Elem retValue;

	public ReturnValue(Elem retValue) {
		this.retValue = retValue;
	}

	public Elem getRetValue() {
		return retValue;
	}
}

class StatementList {

	private LinkedList<Statement> statementlist;

	public StatementList(Statement statement) {
		statementlist = new LinkedList<Statement>();
		statementlist.add(statement);
	}

	public void eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws ReturnValue {

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

	public Elem apply(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var,
			ExpressionList expressionlist) {
		// System.out.println("Executing Proceedure");
		HashMap<String, Elem> newnametable = new HashMap<String, Elem>();

		// bind parameters in new name table
		// we need to get the underlying List structure that the ParamList
		// uses...
		Iterator<String> p = parameterlist.getParamList().iterator();
		Iterator<Expr> e = expressionlist.getExpressions().iterator();

		if (parameterlist.getParamList().size() != expressionlist
				.getExpressions().size()) {
			System.out.println("Param count does not match");
			System.exit(1);
		}
		while (p.hasNext() && e.hasNext()) {

			// assign the evaluation of the expression to the parameter name.
			newnametable.put(p.next(),
					e.next().eval(nametable, functiontable, var));
			// System.out.println("Loading Nametable for procedure with: "+p+" = "+nametable.get(p));

		}
		// evaluate function body using new name table and
		// old function table
		// eval statement list and catch return
		// System.out.println("Beginning Proceedure Execution..");
		try {
			stmtlist.eval(newnametable, functiontable, var);
		} catch (ReturnValue result) {
			// Note, the result shold contain the proceedure's return value as a
			// String
			// System.out.println("return value = "+result.getMessage());
			return result.getRetValue();
		}
		System.out.println("Error:  no return value");
		System.exit(1);
		// need this or the compiler will complain, but should never
		// reach this...
		return null;
	}
}

class Program {
	// dynamic memory allocation heap
	public static Heap HEAP = new Heap(64);
	
	private StatementList stmtlist;

	public Program(StatementList list) {
		stmtlist = list;
	}

	public void eval(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		try {
			stmtlist.eval(nametable, functiontable, var);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void dump(HashMap<String, Elem> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		// System.out.println(hm.values());
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

class Part2 {
}