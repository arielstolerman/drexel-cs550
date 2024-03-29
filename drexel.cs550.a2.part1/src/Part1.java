/* this is the cup file for the mini language
 * at http://www.cs.drexel.edu/~jjohnson/2010-11/spring/cs550/lectures/lec2c.html *
 * created by Xu, 2/5/07
 * 
 * Modified by Mike Kopack for CS550, 2009 Spring Qtr.
 * Should be at the same level of completeness as the Lecture 2c
 * C++ version.
 */

import java.util.*;

/**
 * Generic element that can be either an integer or a list of elements.
 */
class Element {

	private Integer value;
	private List<Element> list;
	private boolean isList;

	public Element(Integer value) {
		this.value = value;
		isList = false;
	}

	public Element(List<Element> list) {
		this.list = list;
		isList = true;
	}

	public boolean isInt() {
		return !isList;
	}

	public boolean isList() {
		return isList;
	}

	public Integer getInt() {
		if (isList)
			return null;
		return value;
	}

	public List<Element> getList() {
		if (!isList)
			return null;
		return list;
	}

	@Override
	public String toString() {
		if (isList)
			return list.toString();
		else
			return value.toString();
	}
}

abstract class Expr {

	public abstract Element eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws RuntimeException;

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
	public Element eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws RuntimeException {
		LinkedList<Element> res = new LinkedList<>();
		for (Expr e : list)
			res.add(e.eval(nametable, functiontable, var));
		return new Element(res);
	}

	public List<Expr> getList() {
		return list;
	}

	@Override
	public String toString() {
		return list.toString();
	}
}

class Ident extends Expr {

	private String name;

	public Ident(String s) {
		name = s;
	}

	public Element eval(HashMap<String, Element> nametable,
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

	public Element eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {
		return new Element(value);
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

	public Element eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws RuntimeException {
		Element e1 = expr1.eval(nametable, functiontable, var);
		Element e2 = expr2.eval(nametable, functiontable, var);
		if (e1.isList() || e2.isList()) {
			throw new RuntimeException("TIMES called on a list: " + e1
					+ " TIMES " + e2 + " invalid");
		}

		return new Element(e1.getInt() * e2.getInt());
	}

	@Override
	public String toString() {
		return expr1.toString() + " TIMES " + expr2.toString();
	}
}

class Plus extends Expr {

	private Expr expr1, expr2;

	public Plus(Expr op1, Expr op2) {
		expr1 = op1;
		expr2 = op2;
	}

	public Element eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws RuntimeException {
		Element e1 = expr1.eval(nametable, functiontable, var);
		Element e2 = expr2.eval(nametable, functiontable, var);
		if (e1.isList() || e2.isList()) {
			throw new RuntimeException("PLUS called on a list: " + e1
					+ " PLUS " + e2 + " invalid");
		}

		return new Element(e1.getInt() + e2.getInt());
	}

	@Override
	public String toString() {
		return expr1.toString() + " PLUS " + expr2.toString();
	}
}

class Minus extends Expr {

	private Expr expr1, expr2;

	public Minus(Expr op1, Expr op2) {
		expr1 = op1;
		expr2 = op2;
	}

	public Element eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws RuntimeException {
		Element e1 = expr1.eval(nametable, functiontable, var);
		Element e2 = expr2.eval(nametable, functiontable, var);
		if (e1.isList() || e2.isList()) {
			throw new RuntimeException("MINUS called on a list: " + e1
					+ " MINUS " + e2 + " invalid");
		}
		return new Element(e1.getInt() - e2.getInt());
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

	public Element eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws RuntimeException {
		return functiontable.get(funcid).apply(nametable, functiontable, var,
				explist);
	}

	@Override
	public String toString() {
		return funcid
				+ " "
				+ explist.getExpressions().toString().replace("[", "( ")
						.replace("]", " )");
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
	public Element eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws RuntimeException {
		Element e1 = list1.eval(nametable, functiontable, var);
		Element e2 = list2.eval(nametable, functiontable, var);

		if (!e1.isList() || !e2.isList()) {
			throw new RuntimeException("Parameter to CONCAT not a list: " + e1
					+ " || " + e2 + " invalid");
		}

		List<Element> res = new LinkedList<>();
		res.addAll(e1.getList());
		res.addAll(e2.getList());
		return new Element(res);
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
	public Element eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws RuntimeException {
		Element expElem = exp.eval(nametable, functiontable, var);
		Element listElem = list.eval(nametable, functiontable, var);

		if (!listElem.isList()) {
			throw new RuntimeException("Second parameter to CONS not a list: "
					+ "CONS ( " + expElem + ", " + listElem + " )"
					+ " invalid");
		}

		LinkedList<Element> res = new LinkedList<>();
		res.add(expElem);
		res.addAll(listElem.getList());
		return new Element(res);
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
	public Element eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws RuntimeException {

		Element e = list.eval(nametable, functiontable, var);
		if (!e.isList()) {
			throw new RuntimeException("Parameter to CAR not a list: "
					+ "CAR ( " + e + " )" + " invalid");
		}
		return e.getList().get(0);
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
	public Element eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws RuntimeException {

		Element e = list.eval(nametable, functiontable, var);
		if (!(e.isList())) {
			throw new RuntimeException("Parameter to CDR not a list: "
					+ "CDR ( " + e + " )" + " invalid");
		}

		List<Element> listEval = e.getList();
		LinkedList<Element> res = new LinkedList<>();
		res.addAll(listEval.subList(1, listEval.size()));
		return new Element(res);
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
	public Element eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws RuntimeException {

		Element e = list.eval(nametable, functiontable, var);

		if (!(e.isList())) {
			throw new RuntimeException("Parameter to NULLP not a list: "
					+ "NULLP ( " + e + " )" + " invalid");
		}
		return e.getList().isEmpty() ? new Element(1) : new Element(0);
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
	public Element eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws RuntimeException {
		return exp.eval(nametable, functiontable, var).isInt() ? new Element(1)
				: new Element(0);
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
	public Element eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws RuntimeException {
		return exp.eval(nametable, functiontable, var).isList() ? new Element(1)
				: new Element(0);
	}

	@Override
	public String toString() {
		return "LISTP ( " + exp + " )";
	}
}

abstract class Statement {

	public Statement() {
	}

	public abstract void eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws ReturnValue, RuntimeException;

	@Override
	public abstract String toString();
}

// added for 2c
class DefineStatement extends Statement {

	private String name;
	private Proc proc;

	// private ParamList paramlist;
	// private StatementList statementlist;

	public DefineStatement(String id, Proc process) {
		name = id;
		proc = process;
	}

	public void eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functable, LinkedList var) {
		// get the named proc object from the function table.
		// System.out.println("Adding Process:"+name+" to Functiontable");
		functable.put(name, proc);
	}

	@Override
	public String toString() {
		return "DEFINE " + name + " " + proc;
	}
}

class ReturnStatement extends Statement {

	private Expr expr;

	public ReturnStatement(Expr e) {
		expr = e;
	}

	public void eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws ReturnValue, RuntimeException {
		throw new ReturnValue(expr.eval(nametable, functiontable, var));
	}

	@Override
	public String toString() {
		return "RETURN " + expr.toString();
	}

}

class AssignStatement extends Statement {

	private String name;
	private Expr expr;

	public AssignStatement(String id, Expr e) {
		name = id;
		expr = e;
	}

	public void eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws RuntimeException {
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

	public void eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws RuntimeException {
		Element cond = expr.eval(nametable, functiontable, var);
		if (!cond.isInt())
			throw new RuntimeException("IF condition must be an integer: "
					+ "IF " + cond + " THEN ... invalid");
		if (cond.getInt() > 0) {
			stmtlist1.eval(nametable, functiontable, var);
		} else {
			stmtlist2.eval(nametable, functiontable, var);
		}
	}

	@Override
	public String toString() {
		return "IF" + expr.toString();
	}
}

class WhileStatement extends Statement {

	private Expr expr;
	private StatementList stmtlist;

	public WhileStatement(Expr e, StatementList list) {
		expr = e;
		stmtlist = list;
	}

	public void eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws RuntimeException {
		Element cond = null;
		try {
			while ((cond = expr.eval(nametable, functiontable, var)).getInt() > 0) {
				stmtlist.eval(nametable, functiontable, var);
			}
		} catch (Exception e) {
			throw new RuntimeException("WHILE condition must be an integer: "
					+ "WHILE " + cond + " DO ... invalid");
		}
	}

	@Override
	public String toString() {
		return "WHILE " + expr.toString() + " DO " + stmtlist.toString();
	}
}

class RepeatStatement extends Statement {

	private Expr expr;
	private StatementList sl;

	public RepeatStatement(StatementList list, Expr e) {
		expr = e;
		sl = list;
	}

	public void eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws RuntimeException {
		Element cond = null;
		try {
			do {
				sl.eval(nametable, functiontable, var);
			} while ((cond = expr.eval(nametable, functiontable, var)).getInt() > 0);
		} catch (Exception e) {
			throw new RuntimeException("REPEAT condition must be an integer: "
					+ "REPEAT ... UNTIL " + cond + " invalid");
		}
	}

	@Override
	public String toString() {
		return "REPEAT " + expr.toString() + " UNTIL" + sl.toString();
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
class ReturnValue extends RuntimeException {

	private Element retValue;

	public ReturnValue(Element retValue) {
		this.retValue = retValue;
	}

	public Element getRetValue() {
		return retValue;
	}
}

class StatementList {

	private LinkedList<Statement> statementlist;

	public StatementList(Statement statement) {
		statementlist = new LinkedList<Statement>();
		statementlist.add(statement);
	}

	public void eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var)
			throws RuntimeException {

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

	@Override
	public String toString() {
		String res = "";
		for (Statement s: statementlist)
			res += s + "\n";
		return res;
	}

}

class Proc {

	private ParamList parameterlist;
	private StatementList stmtlist;

	public Proc(ParamList pl, StatementList sl) {
		parameterlist = pl;
		stmtlist = sl;
	}

	public Element apply(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var,
			ExpressionList expressionlist) throws RuntimeException {
		// System.out.println("Executing Proceedure");
		HashMap<String, Element> newnametable = new HashMap<String, Element>();

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

	private StatementList stmtlist;

	public Program(StatementList list) {
		stmtlist = list;
	}

	public void eval(HashMap<String, Element> nametable,
			HashMap<String, Proc> functiontable, LinkedList var) {

		stmtlist.eval(nametable, functiontable, var);

	}

	public void dump(HashMap<String, Element> nametable,
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

class Part1 {
}