package part1;
/* this is the cup file for the mini language
 * at http://www.cs.drexel.edu/~jjohnson/2010-11/spring/cs550/lectures/lec2c.html *
 * created by Xu, 2/5/07
 * 
 * Modified by Mike Kopack for CS550, 2009 Spring Qtr.
 * Should be at the same level of completeness as the Lecture 2c
 * C++ version.
 */

import java.util.*;

/* =============================================================================
 * expressions
 * =============================================================================
 */

/**
 * Should never be constructed! 
 */
class Expr {

	public Expr() {
	}

	public Object eval(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
		return new Object(); // should never be called! 
	}
}

/**
 * Numeric expressions
 */
class NumExpr extends Expr {
	
	public NumExpr() {
	}

	public Integer eval(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
		return new Integer(0);
	}
}

/**
 * List expressions - lists of numeric or other list expressions
 */
class ListExpr extends Expr {
	
	private List<Expr> list;
	
	public ListExpr() {
		// empty list
		list = new LinkedList<>();
	}
	
	public ListExpr(ExpressionList el) {
		this.list = el.getExpressions();
	}
	
	public List<Object> eval(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var){
		List<Object> res = new LinkedList<>();
		for (Expr e: list)
			res.add(e.eval(nametable, functiontable, var));
		return res;
	}
	
	protected int size() {
		return list.size();
	}
	
	protected Expr getElem(int index) {
		if (index < list.size())
			return list.get(index);
		else
			return null;
	}
	
	protected Iterator<Expr> iterator() {
		return list.iterator();
	}
}

/**
 * modified Ident to support both number and list variables
 */
class Ident extends Expr {

	private String name;

	public Ident(String s) {
		name = s;
	}

	public Object eval(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
		return nametable.get(name);
	}
}

class Number extends NumExpr {

	private Integer value;

	public Number(int n) {
		value = new Integer(n);
	}

	public Number(Integer n) {
		value = n;
	}

	public Integer eval(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
		return value;
	}
}

class Times extends NumExpr {

	private NumExpr expr1,  expr2;

	public Times(NumExpr op1, NumExpr op2) {
		expr1 = op1;
		expr2 = op2;
	}

	public Integer eval(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
		return new Integer(expr1.eval(nametable, functiontable, var) * expr2.eval(nametable, functiontable, var));
	}
}

class Plus extends NumExpr {

	private NumExpr expr1,  expr2;

	public Plus(NumExpr op1, NumExpr op2) {
		expr1 = op1;
		expr2 = op2;
	}

	public Integer eval(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
		return new Integer(expr1.eval(nametable, functiontable, var).intValue() + expr2.eval(nametable, functiontable, var).intValue());
	}
}

class Minus extends NumExpr {

	private NumExpr expr1,  expr2;

	public Minus(NumExpr op1, NumExpr op2) {
		expr1 = op1;
		expr2 = op2;
	}

	public Integer eval(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
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

	public Object eval(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
		return functiontable.get(funcid).apply(nametable, functiontable, var, explist);
	}
}

// list functions and operators

class Cons extends ListExpr {
	
	private Expr expr;
	private ListExpr listExpr;
	
	public Cons(Expr expr, ListExpr listExpr) {
		this.expr = expr;
		this.listExpr = listExpr;
	}
	
	/**
	 * Concatenates the first evaluated expression to the second evaluated list.
	 * If the first expression is a list, concatenates the two lists; otherwise
	 * adds the first expression as the first element of the list. 
	 */
	public List<Object> eval(HashMap<String, Object> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		List<Object> rightPart = listExpr.eval(nametable, functiontable, var);
		Object leftPart = expr.eval(nametable, functiontable, var);
		if (leftPart instanceof List) {
			// cons'ing a list
			List<Object> leftPartList = (List<Object>) leftPart;
			for (Object elem: rightPart)
				leftPartList.add(elem);
			return leftPartList;
		}
		else {
			// cons'ing a number
			rightPart.add(0, leftPart);
			return rightPart;
		}
	}
}

class Car extends Expr {
	
	private ListExpr list;
	
	public Car(ListExpr list) {
		this.list = list;
	}
	
	@Override
	public Object eval(HashMap<String, Object> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		Expr firstElem = list.getElem(0);
		if (firstElem == null)
			return null; //TODO should we return null?
		else
			return firstElem.eval(nametable, functiontable, var);
	}
}

class Cdr extends ListExpr {
	
	private ListExpr list;
	
	public Cdr(ListExpr list) {
		this.list = list;
	}
	
	@Override
	public List<Object> eval(HashMap<String, Object> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		LinkedList<Object> res = new LinkedList<>();
		if (list.size() < 2)
			return null; // TODO should we return null? should we use < 2 or == 1?
		Iterator<Expr> iter = list.iterator();
		iter.next(); // skip first element
		while (iter.hasNext())
			res.add(iter.next().eval(nametable, functiontable, var));
		return res;
	}
}

class NullP extends NumExpr {
	
	private ListExpr list;
	
	public NullP(ListExpr list) {
		this.list = list;
	}
	
	@Override
	public Integer eval(HashMap<String, Object> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		// TODO currently returns whether the list is empty; should return something else?
		return list.eval(nametable, functiontable, var).size() == 0 ? 1 : 0;
	}
}

class IntP extends NumExpr {
	
	private Expr expr;
	
	public IntP(Expr expr) {
		this.expr = expr;
	}
	
	@Override
	public Integer eval(HashMap<String, Object> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		if (expr.eval(nametable, functiontable, var) instanceof Integer)
			return 1;
		else
			return 0;
	}
}

class ListP extends NumExpr {
	
	private Expr expr;
	
	public ListP(Expr expr) {
		this.expr = expr;
	}
	
	@Override
	public Integer eval(HashMap<String, Object> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		if (expr.eval(nametable, functiontable, var) instanceof List)
			return 1;
		else
			return 0;
	}
}

/* =============================================================================
 * statements
 * =============================================================================
 */

abstract class Statement {

	public Statement() {
	}

	public void eval(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var) throws ReturnValueWrapper {
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

	public void eval(HashMap<String, Object> nametable, HashMap<String, Proc> functable, LinkedList var) {
		// get the named proc object from the function table.
		//System.out.println("Adding Process:"+name+" to Functiontable");
		functable.put(name, proc);
	}
}

/**
 * Wrapper class for returning a value by ReturnStatement
 */
class ReturnValueWrapper extends Exception {
	
	private Object expEval;
	
	public ReturnValueWrapper(Object expEval){
		this.expEval = expEval;
	}
	
	@Override
	public String getMessage() {
		return expEval.toString();
	}
	
	public Object getRetVal() {
		return expEval;
	}
}

class ReturnStatement extends Statement {

	private Expr expr;

	public ReturnStatement(Expr e) {
		expr = e;
	}

	public void eval(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var) throws ReturnValueWrapper {
		//Java can't throw exceptions of numbers, so we'll convert it to a string
		//and then on the other end we'll reconvert back to Integer..
		throw new ReturnValueWrapper(expr.eval(nametable, functiontable, var));
	}
}

class AssignStatement extends Statement {

	private String name;
	private Expr expr;

	public AssignStatement(String id, Expr e) {
		name = id;
		expr = e;
	}

	public void eval(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
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

	private NumExpr expr;
	private StatementList stmtlist1,  stmtlist2;

	public IfStatement(NumExpr e, StatementList list1, StatementList list2) {
		expr = e;
		stmtlist1 = list1;
		stmtlist2 = list2;
	}

	public IfStatement(NumExpr e, StatementList list) {
		expr = e;
		stmtlist1 = list;
	}

	public void eval(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var) throws ReturnValueWrapper {
		if (expr.eval(nametable, functiontable, var) > 0) {
			stmtlist1.eval(nametable, functiontable, var);
		} else {
			if (stmtlist2 != null)
				stmtlist2.eval(nametable, functiontable, var);
		}
	}
}

class WhileStatement extends Statement {

	private NumExpr expr;
	private StatementList stmtlist;

	public WhileStatement(NumExpr e, StatementList list) {
		expr = e;
		stmtlist = list;
	}

	public void eval(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var) throws ReturnValueWrapper {
		while (expr.eval(nametable, functiontable, var) > 0) {
			stmtlist.eval(nametable, functiontable, var);
		}
	}
}

class RepeatStatement extends Statement {

	private NumExpr expr;
	private StatementList sl;

	public RepeatStatement(StatementList list, NumExpr e) {
		expr = e;
		sl = list;
	}

	public void eval(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var) throws ReturnValueWrapper {
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
		//we need to add the expression to the front of the list
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

	public void eval(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var) throws ReturnValueWrapper {


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

	public Object apply(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var, ExpressionList expressionlist) {
		//System.out.println("Executing Proceedure");
		HashMap<String, Object> newnametable = new HashMap<String, Object>();

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
		} catch (ReturnValueWrapper result) {
			// Note, the result shold contain the proceedure's return value as a String
			//System.out.println("return value = "+result.getMessage());
			return result.getRetVal();
		}
		System.out.println("Error: no return value");
		System.exit(1);
		// need this or the compiler will complain, but should never
		// reach this...
		return null;
	}
}

/* =============================================================================
 * program
 * =============================================================================
 */

class Program {

	private StatementList stmtlist;

	public Program(StatementList list) {
		stmtlist = list;
	}

	public void eval(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
		try {
			stmtlist.eval(nametable, functiontable, var);
		} catch (ReturnValueWrapper retVal) {
			System.out.println(retVal.getRetVal());
		}
	}

	public void dump(HashMap<String, Object> nametable, HashMap<String, Proc> functiontable, LinkedList var) {
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
