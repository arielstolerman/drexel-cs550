/* this is the cup file for the mini language
 * at http://www.cs.drexel.edu/~jjohnson/2010-11/spring/cs550/lectures/lec2c.html *
 * created by Xu, 2/5/07
 * 
 * Modified by Mike Kopack for CS550, 2009 Spring Qtr.
 * Should be at the same level of completeness as the Lecture 2c
 * C++ version.
 */

import java.util.*;

// =============================================================================

// added code

/**
 * Generic element that can be one of: integer, list, procedure or class
 * instance.
 */
class Elem {
	
	// fields
	private Integer num = null;
	private List<Elem> list = null;
	private Proc proc = null;
	private Class cls = null;
	private ElemType type = ElemType.ERROR;
	
	// constructors
	
	private Elem(Integer num, List<Elem> list, Proc proc, Class cls) {
		if (num != null) {
			this.num = num;
			type = ElemType.NUM;
		}
		else if (list != null) {
			this.list = list;
			type = ElemType.LIST;
		}
		else if (proc != null) {
			this.proc = proc;
			type = ElemType.PROC;
		}
		else if (cls != null) {
			this.cls = cls;
			type = ElemType.CLASS;
		}
	}
	
	public Elem(Integer num) {
		this(num, null, null, null);
	}
	
	public Elem(List<Elem> list) {
		this(null, list, null, null);
	}
	
	public Elem(Proc proc) {
		this(null, null, proc, null);
	}
	
	public Elem(Class cls) {
		this(null, null, null, cls);
	}
	
	// getters and queries
	
	public boolean isNum() {
		return type == ElemType.NUM;
	}
	
	public boolean isList() {
		return type == ElemType.LIST;
	}
	
	public boolean isProc() {
		return type == ElemType.PROC;
	}
	
	public boolean isClass() {
		return type == ElemType.CLASS;
	}
	
	public ElemType type() {
		return type;
	}

	public Integer getNum() {
		return num;
	}
	
	public List<Elem> getList() {
		return list;
	}
	
	public Proc getProc() {
		return proc;
	}
	
	public Class getCls() {
		return cls;
	}

	@Override
	public String toString() {
		switch (type) {
		case NUM: return num.toString();
		case LIST: return list.toString();
		case PROC: return proc.toString();
		case CLASS: return cls.toString();
		default: return "Elem error";
		}
	}
}

/**
 * Enumerator for element type.
 */
enum ElemType {
	NUM("integer"),
	LIST("list"),
	PROC("procedure"),
	CLASS("class"),
	ERROR("error");
	
	private ElemType(String desc) {
		this.desc = desc;
	}
	
	private String desc;
	
	@Override
	public String toString() {
		return desc;
	}
}

/**
 * Abstract class for everything to extend, to enforce implementation of eval.
 */
abstract class Component {

	// keep record of symbol table of the scope in which the component is
	// defined

	/**
	 * To hold the scope in which the object is defined.
	 */
	protected Scope scope;

	/**
	 * @return the static symbol table of the environment in which this object
	 * is evaluated.
	 */
	public Scope staticScope() {
		return scope;
	}
	
	public abstract void setStaticScope(Scope scope);

	// evaluate method

	/**
	 * Evaluate method
	 */
	public abstract Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException;
	
	// string representation
	
	@Override
	public abstract String toString();
}

/**
 * Interface for components that are also scopes - Program and Proc.
 */
interface Scope {
	/**
	 * @return the environment (symbol table) of the scope.
	 */
	public HashMap<String, Elem> env();
}

/**
 * Wrapper for statement "return" values
 */
class ReturnValue extends RuntimeException {

	private static final long serialVersionUID = -7380156579538751703L;
	private Elem retValue;

	public ReturnValue(Elem retValue) {
		this.retValue = retValue;
	}

	public Elem getRetValue() {
		return retValue;
	}
}

//=============================================================================

// expressions

abstract class Expr extends Component {}

class Lst extends Expr {

	private List<Expr> list;

	public Lst() {
		list = new LinkedList<>();
	}

	public Lst(ExpressionList el) {
		this();
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
	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		LinkedList<Elem> res = new LinkedList<>();
		for (Expr e : list)
			res.add(e.eval(symbolTable));
		return new Elem(res);
	}
	
	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		for (Expr e: list)
			e.setStaticScope(scope);
	}

	public List<Expr> getList() {
		return list;
	}
	
	@Override
	public String toString() {
		String res = "(";
		for (Expr e: list)
			res += e + ", ";
		return res.substring(0, res.length() - 2) + ")";
	}
}

class Ident extends Expr {

	private String name;

	public Ident(String s) {
		name = s;
	}

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable) throws RuntimeException {
		Elem e = symbolTable.get(name);
		if (e == null)
			throw new RuntimeException("undefined variable: " + name);
		return e;
	}
	
	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
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

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable) {
		return new Elem(value);
	}
	
	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
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

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		Elem e1 = expr1.eval(symbolTable);
		Elem e2 = expr2.eval(symbolTable);
		if (e1.isList() || e2.isList()) {
			throw new RuntimeException("TIMES called on a list: " + e1
					+ " TIMES " + e2 + " invalid");
		}

		return new Elem(e1.getNum() * e2.getNum());
	}
	
	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		expr1.setStaticScope(scope);
		expr2.setStaticScope(scope);
	}
	
	@Override
	public String toString() {
		return expr1 + " * " + expr2;
	}
}

class Plus extends Expr {

	private Expr expr1, expr2;

	public Plus(Expr op1, Expr op2) {
		expr1 = op1;
		expr2 = op2;
	}
	
	@Override
	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		Elem e1 = expr1.eval(symbolTable);
		Elem e2 = expr2.eval(symbolTable);
		if (e1.isList() || e2.isList()) {
			throw new RuntimeException("PLUS called on a list: " + e1
					+ " PLUS " + e2 + " invalid");
		}

		return new Elem(e1.getNum() + e2.getNum());
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		expr1.setStaticScope(scope);
		expr2.setStaticScope(scope);
	}
	
	@Override
	public String toString() {
		return expr1 + " + " + expr2;
	}
}

class Minus extends Expr {

	private Expr expr1, expr2;

	public Minus(Expr op1, Expr op2) {
		expr1 = op1;
		expr2 = op2;
	}

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		Elem e1 = expr1.eval(symbolTable);
		Elem e2 = expr2.eval(symbolTable);
		if (e1.isList() || e2.isList()) {
			throw new RuntimeException("MINUS called on a list: " + e1
					+ " MINUS " + e2 + " invalid");
		}
		return new Elem(e1.getNum() - e2.getNum());
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		expr1.setStaticScope(scope);
		expr2.setStaticScope(scope);
	}
	
	@Override
	public String toString() {
		return expr1 + " - " + expr2;
	}
}

class FunctionCall extends Expr {

	private String funcid;
	private ExpressionList explist;

	public FunctionCall(String id,
			ExpressionList el) {
		funcid = id;
		explist = el;
	}

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		Elem procElem = symbolTable.get(funcid);
		if (procElem == null) {
			throw new RuntimeException("attempted to apply an undefined " +
					"procedure: " + funcid);
		}
		Proc proc = procElem.getProc();
		if (proc == null) {
			throw new RuntimeException("attempted to apply a non-procedure " +
					"variable: " + funcid);
		}
		return proc.apply(symbolTable, explist);
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		for (Expr e: explist.getExpressions())
			e.setStaticScope(scope);
	}
	
	@Override
	public String toString() {
		return "" + funcid + explist;
	}
	
	public String funcId() {
		return funcid;
	}
	
	public ExpressionList exprList() {
		return explist;
	}
}

class Concat extends Expr {

	private Expr list1;
	private Expr list2;

	public Concat(Expr list1,
			Expr list2) {
		this.list1 = list1;
		this.list2 = list2;
	}

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		Elem e1 = list1.eval(symbolTable);
		Elem e2 = list2.eval(symbolTable);

		if (!e1.isList() || !e2.isList()) {
			throw new RuntimeException("Parameter to CONCAT not a list: " + e1
					+ " || " + e2 + " invalid");
		}

		List<Elem> res = new LinkedList<>();
		res.addAll(e1.getList());
		res.addAll(e2.getList());
		return new Elem(res);
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		list1.setStaticScope(scope);
		list2.setStaticScope(scope);
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
	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		Elem expElem = exp.eval(symbolTable);
		Elem listElem = list.eval(symbolTable);

		if (!listElem.isList()) {
			throw new RuntimeException("Second parameter to CONS not a list: "
					+ "CONS ( " + expElem + ", " + listElem + " )"
					+ " invalid");
		}

		LinkedList<Elem> res = new LinkedList<>();
		res.add(expElem);
		res.addAll(listElem.getList());
		return new Elem(res);
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		exp.setStaticScope(scope);
		list.setStaticScope(scope);
	}
	
	@Override
	public String toString() {
		return "cons(" + exp + ", " + list + ")";
	}
}

class Car extends Expr {

	private Expr list;

	public Car(Expr list) {
		this.list = list;
	}

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {

		Elem e = list.eval(symbolTable);
		if (!e.isList()) {
			throw new RuntimeException("Parameter to CAR not a list: "
					+ "CAR ( " + e + " )" + " invalid");
		}
		return e.getList().get(0);
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		list.setStaticScope(scope);
	}
	
	@Override
	public String toString() {
		return "car(" + list + ")";
	}
}

class Cdr extends Expr {

	private Expr list;

	public Cdr(Expr list) {
		this.list = list;
	}

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		Elem e = list.eval(symbolTable);
		if (!(e.isList())) {
			throw new RuntimeException("Parameter to CDR not a list: "
					+ "CDR ( " + e + " )" + " invalid");
		}

		List<Elem> listEval = e.getList();
		LinkedList<Elem> res = new LinkedList<>();
		res.addAll(listEval.subList(1, listEval.size()));
		return new Elem(res);
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		list.setStaticScope(scope);
	}
	
	@Override
	public String toString() {
		return "cdr(" + list + ")";
	}
}

class NullP extends Expr {

	private Expr list;

	public NullP(Expr list) {
		this.list = list;
	}

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		Elem e = list.eval(symbolTable);
		if (!(e.isList())) {
			throw new RuntimeException("Parameter to NULLP not a list: "
					+ "NULLP ( " + e + " )" + " invalid");
		}
		return e.getList().isEmpty() ? new Elem(1) : new Elem(0);
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		list.setStaticScope(scope);
	}
	
	@Override
	public String toString() {
		return "nullp(" + list + ")";
	}
}

class IntP extends Expr {

	private Expr exp;

	public IntP(Expr exp) {
		this.exp = exp;
	}

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		return exp.eval(symbolTable).isNum() ? new Elem(1) : new Elem(0);
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		exp.setStaticScope(scope);
	}
	
	@Override
	public String toString() {
		return "intp(" + exp + ")";
	}
}

class ListP extends Expr {

	private Expr exp;

	public ListP(Expr exp) {
		this.exp = exp;
	}

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		return exp.eval(symbolTable).isList() ? new Elem(1)
				: new Elem(0);
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		exp.setStaticScope(scope);
	}
	
	@Override
	public String toString() {
		return "listp(" + exp + ")";
	}
}

class Proc extends Expr implements Scope {

	private ParamList paramlist;
	private StatementList stmtlist;
	private HashMap<String, Elem> procEnv;

	public Proc(ParamList pl,
			StatementList sl) {
		paramlist = pl;
		stmtlist = sl;
	}
	
	/**
	 * Applies the procedure on the given list of expressions as parameters and
	 * returns the evaluated element.
	 * If the scoping rule is set to dynamic, will use the given calling scope
	 * symbol table.
	 */
	public Elem apply(HashMap<String, Elem> symbolTable, ExpressionList explist)
			throws RuntimeException {
		LinkedList<Elem> elems = new LinkedList<>();
		for (Expr expr: explist.getExpressions())
			elems.add(expr.eval(symbolTable));
		return apply(symbolTable, elems);
	}
	
	/**
	 * Applies the procedure on the given list of evaluated expressions as
	 * parameters and returns the evaluated element.
	 * If the scoping rule is set to dynamic, will use the given calling scope
	 * symbol table.
	 */
	public Elem apply(HashMap<String, Elem> symbolTable, List<Elem> elems)
			throws RuntimeException {
		// initialize proc environment - its symbol table
		procEnv = new HashMap<>();
		
		// set symbol table to use according to scope rule
		if (Program.STATIC_SCOPING) {
			// use scope in which proc was defined
			procEnv.putAll(scope.env());
		}
		else {
			// use scope in which proc was called
			procEnv.putAll(symbolTable);
		}
		
		// add parameter bindings
		int numP = paramlist.numParams(), numE = elems.size();
		if (numP != numE)
			throw new RuntimeException("Param count does not match, expected " +
					numP + ", received " + numE);
		HashMap<String, Elem> paramBindings = new HashMap<>();
		Iterator<String> p = paramlist.getParamList().iterator();
		Iterator<Elem> e = elems.iterator();
		while (p.hasNext() && e.hasNext())
			paramBindings.put(p.next(), e.next());
		procEnv.putAll(paramBindings);
		
		// finally, evaluate body
		try {
			stmtlist.eval(procEnv);
		} catch (ReturnValue result) {
			return result.getRetValue();
		}
		throw new RuntimeException("Error: no return value");
	}

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable) throws RuntimeException {
		return new Elem(this);
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		for (Statement s: stmtlist.getStatements())
			s.setStaticScope(this);
	}

	@Override
	public HashMap<String, Elem> env() {
		return procEnv;
	}
	
	@Override
	public String toString() {
		return "proc " + paramlist + " " + stmtlist + " end";
	}
	
	public int numParams() {
		return paramlist.numParams();
	}
}

class ProcP extends Expr {

	private Expr exp;

	public ProcP(Expr exp) {
		this.exp = exp;
	}

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		return exp.eval(symbolTable).isProc() ? new Elem(1)	: new Elem(0);
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		exp.setStaticScope(scope);
	}
	
	@Override
	public String toString() {
		return "procp(" + exp + ")";
	}
}

class Map extends Expr {

	private Expr proc;
	private Expr list;
	
	public Map(Expr proc, Expr list) {
		this.proc = proc;
		this.list = list;
	}

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable) throws RuntimeException {
		// evaluate proc expression
		Elem procElem = proc.eval(symbolTable);
		if (!procElem.isProc()) {
			throw new RuntimeException("map expected a proc as first argument," +
					" given: " + procElem.type());
		}
		Proc proc = procElem.getProc();
		if (proc.numParams() != 1) {
			throw new RuntimeException("cannot call map with proc that does " +
					"not expect exactly 1 parameter; received: " + proc);
		}
		
		// evaluate list expression and construct expressions
		Elem listElem = list.eval(symbolTable);
		if (!listElem.isList()) {
			throw new RuntimeException("map expected a list as second " +
					"argument, given: " + listElem.type());
		}
		List<Elem> elems = listElem.getList();
		
		// map
		LinkedList<Elem> res = new LinkedList<>();
		LinkedList<Elem> singleton;
		for (Elem e: elems) {
			singleton = new LinkedList<>();
			singleton.add(e);
			res.add(proc.apply(symbolTable, singleton));
		}
		return new Elem(res);
	}
	
	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		proc.setStaticScope(scope);
		list.setStaticScope(scope);
	}

	@Override
	public String toString() {
		return "map(" + proc + ", " + list + ")";
	}
}

class DotCall extends Expr {
	
	private Expr expr;
	private String attrib = null;
	private FunctionCall funCall = null;
	
	public DotCall(Expr e, String a) {
		expr = e;
		attrib = a;
	}
	
	public DotCall(Expr e, FunctionCall f) {
		expr = e;
		funCall = f;
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		if (funCall != null)
			funCall.setStaticScope(scope);
	}

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable) throws RuntimeException {
		// get instance
		Elem clsElem = expr.eval(symbolTable);
		if (!clsElem.isClass())
			throw new RuntimeException("Not a class instance: " + expr);
		Class cls = clsElem.getCls();
		
		// get attribute / call function
		Elem res = null;
		if (attrib != null) {
			res = cls.env().get(attrib);
			if (res == null) {
				throw new RuntimeException(expr + " does not contain attribute "
						+ attrib);
			}
		}
		else if (funCall != null) {
			String funcid = funCall.funcId();
			Elem proc = cls.env().get(funcid);
			if (proc == null)
				throw new RuntimeException(expr + " does not contain function "
						+ funcid);
			res = funCall.eval(symbolTable); // should work in static scoping
		}
		
		// handle return
		if (res == null)
			throw new RuntimeException("unexpected return value from " +
					toString());
		return res;
	}

	@Override
	public String toString() {
		return expr + "." + (attrib != null ? attrib : funCall);
	}
	
}

// =============================================================================

// statements

abstract class Statement extends Component {}

class ReturnStatement extends Statement {

	private Expr expr;

	public ReturnStatement(Expr e) {
		expr = e;
	}

	public Elem eval(HashMap<String, Elem> symbolTable)
			throws ReturnValue, RuntimeException {
		throw new ReturnValue(expr.eval(symbolTable));
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		expr.setStaticScope(scope);
	}
	
	@Override
	public String toString() {
		return "return " + expr;
	}
}

class AssignStatement extends Statement {

	private String name;
	private Expr expr;

	public AssignStatement(String id,
			Expr e) {
		name = id;
		expr = e;
	}

	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		symbolTable.put(name, expr.eval(symbolTable));
		return null;
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		expr.setStaticScope(scope);
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

	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		Elem cond = expr.eval(symbolTable);
		if (!cond.isNum())
			throw new RuntimeException("IF condition must be an integer, " +
					"received: " + cond.type());
		if (cond.getNum() > 0) {
			stmtlist1.eval(symbolTable);
		} else {
			stmtlist2.eval(symbolTable);
		}
		return null;
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		expr.setStaticScope(scope);
		stmtlist1.setStaticScope(scope);
		stmtlist2.setStaticScope(scope);
	}
	
	@Override
	public String toString() {
		return "if " + expr + " then " +
				stmtlist1 + " else " + stmtlist2 + "fi";
	}
}

class WhileStatement extends Statement {

	private Expr expr;
	private StatementList stmtlist;

	public WhileStatement(Expr e, StatementList list) {
		expr = e;
		stmtlist = list;
	}

	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		Elem cond = null;
		try {
			while ((cond = expr.eval(symbolTable)).getNum() > 0) {
				stmtlist.eval(symbolTable);
			}
		} catch (Exception e) {
			throw new RuntimeException("WHILE condition must be an integer, " +
					"received: " + cond.type());
		}
		return null;
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		expr.setStaticScope(scope);
		stmtlist.setStaticScope(scope);
	}
	
	@Override
	public String toString() {
		return "while " + expr + " do " + stmtlist + " od";
	}
}

class RepeatStatement extends Statement {

	private Expr expr;
	private StatementList stmtlist;

	public RepeatStatement(StatementList list, Expr e) {
		expr = e;
		stmtlist = list;
	}

	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		Elem cond = null;
		try {
			do {
				stmtlist.eval(symbolTable);
			} while ((cond = expr.eval(symbolTable)).getNum() > 0);
		} catch (Exception e) {
			throw new RuntimeException("REPEAT condition must be an integer, " +
					"received: " + cond.type());
		}
		return null;
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		expr.setStaticScope(scope);
		stmtlist.setStaticScope(scope);
	}
	
	@Override
	public String toString() {
		return "repeat " + stmtlist + " until " + expr;
	}
}

class Class extends Statement implements Scope {
	
	private String name;
	private String superName;
	private ParamList paramList;
	private StatementList stmtList;
	private HashMap<String, Elem> clsEnv = new HashMap<>();
	
	public Class(String n, String s, ParamList pl, StatementList sl) {
		name = n;
		superName = s;
		paramList = pl;
		stmtList = sl;
	}
	
	public Class(String n, ParamList pl, StatementList sl) {
		this(n, null, pl, sl);
	}
	
	public Class(String n, String s, StatementList sl) {
		this(n, s, new ParamList(), sl);
	}
	
	public Class(String n, StatementList sl) {
		this(n, null, new ParamList(), sl);
	}
	
	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		stmtList.setStaticScope(this);
	}

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable) throws RuntimeException {
		
		
		
		return null;
	}

	@Override
	public String toString() {
		return "class " + name + " " + paramList + " " +
				(superName != null ? ": " + superName + " " : "") +
				stmtList + " end";
	}

	@Override
	public HashMap<String, Elem> env() {
		return clsEnv;
	}	
}

class ParamList {

	private List<String> paramlist;
	
	public ParamList() {
		paramlist = new LinkedList<>();
	}

	public ParamList(String name) {
		this();
		paramlist.add(name);
	}

	public ParamList(String name, ParamList parlist) {
		this();
		paramlist.add(0,name);
	}

	public List<String> getParamList() {
		return paramlist;
	}
	
	public int numParams() {
		return paramlist.size();
	}
	
	@Override
	public String toString() {
		return paramlist.toString().replace("[", "(").replace("]", ")");
	}
}

class ExpressionList {

	private List<Expr> list;

	public ExpressionList(Expr ex) {
		list = new LinkedList<Expr>();
		list.add(ex);
	}

	public ExpressionList(Expr ex, ExpressionList el) {
		list = new LinkedList<Expr>();
		list.add(ex);
		list.addAll(el.getExpressions());
	}
	
	public ExpressionList(List<Expr> list) {
		this.list = list;
	}

	public List<Expr> getExpressions() {
		return list;
	}
	
	public int numExpressions() {
		return list.size();
	}

	@Override
	public String toString() {
		return list.toString().replace("[", "(").replace("]", ")");
	}
}

// =============================================================================

class StatementList extends Component {

	private LinkedList<Statement> stmtlist;

	public StatementList(Statement statement) {
		stmtlist = new LinkedList<Statement>();
		stmtlist.add(statement);
	}

	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		for (Statement stmt : stmtlist) {
			stmt.eval(symbolTable);
		}
		return null;
	}

	public void insert(Statement s) {
		// we need to add it to the front of the list
		stmtlist.add(0, s);
	}

	public LinkedList<Statement> getStatements() {
		return stmtlist;
	}
	
	public int numStatements() {
		return stmtlist.size();
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		for (Statement s: stmtlist)
			s.setStaticScope(scope);
	}
	
	@Override
	public String toString() {
		String res = "";
		for (Statement s: stmtlist)
			res += s + "; ";
		return res;
	}
}

class Program extends Component implements Scope {

	/**
	 * Flag to indicate static / dynamic scoping.
	 */
	public static boolean STATIC_SCOPING = true;
	
	// fields
	private StatementList stmtlist;
	private HashMap<String, Elem> progEnv = new HashMap<>();

	public Program(StatementList list) {
		stmtlist = list;
		setStaticScopeRecursively();
	}

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable) {
		stmtlist.eval(symbolTable);
		return null;
	}
	
	public void eval() {
		stmtlist.eval(progEnv);
	}
	
	@Override
	// should never be called
	public void setStaticScope(Scope scope) {
		this.scope = this;
	}
	
	public void setStaticScopeRecursively() {
		for (Statement s: stmtlist.getStatements())
			s.setStaticScope(this);
	}
	
	@Override
	public HashMap<String, Elem> env() {
		return progEnv;
	}
	
	public void dump() {
		System.out.println("Dumping global symbol table:");
		for (String key: progEnv.keySet())
			System.out.println(key + " = " + progEnv.get(key));
	}
	
	/**
	 * Sets whether to use static or dynamic scoping.
	 */
	public static void setStaticScoping(boolean staticScoping) {
		STATIC_SCOPING = staticScoping;
	}
	
	@Override
	public String toString() {
		return stmtlist.toString();
	}
}
