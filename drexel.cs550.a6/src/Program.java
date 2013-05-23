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
 * Generic element that can be one of: integer, list or procedure.
 */
class Elem {
	
	// fields
	private Integer num = null;
	private List<Elem> list = null;
	private Proc proc = null;
	private ElemType type = ElemType.ERROR;
	
	// constructors
	
	private Elem(Integer num, List<Elem> list, Proc proc) {
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
	}
	
	public Elem(Integer num) {
		this(num, null, null);
	}
	
	public Elem(List<Elem> list) {
		this(null, list, null);
	}
	
	public Elem(Proc proc) {
		this(null, null, proc);
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

	@Override
	public String toString() {
		switch (type) {
		case NUM: return num.toString();
		case LIST: return list.toString();
		case PROC: return proc.toString();
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
	
	// strign representation
	
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
	public Elem eval(HashMap<String, Elem> symbolTable) {
		return symbolTable.get(name);
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
		Proc proc = symbolTable.get(funcid).getProc();
		if (proc == null) {
			throw new RuntimeException("attempted to apply a variable that " +
					"is not a procedure: " + funcid);
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

	private ParamList parameterlist;
	private StatementList stmtlist;
	private HashMap<String, Elem> procEnv;

	public Proc(ParamList pl,
			StatementList sl) {
		parameterlist = pl;
		stmtlist = sl;
	}

	public Elem apply(HashMap<String, Elem> symbolTable,
			ExpressionList expressionlist) throws RuntimeException {
		// System.out.println("Executing Procedure");
		HashMap<String, Elem> newsymbolTable = new HashMap<String, Elem>();

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
			newsymbolTable.put(p.next(),
					e.next().eval(symbolTable));
			// System.out.println("Loading symbolTable for procedure with: "+p+" = "+symbolTable.get(p));

		}
		// evaluate function body using new name table and
		// old function table
		// eval statement list and catch return
		// System.out.println("Beginning Proceedure Execution..");
		try {
			stmtlist.eval(newsymbolTable);
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

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable) throws RuntimeException {
		// TODO Auto-generated method stub
		return null;
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
		return "proc " + parameterlist + " ...";
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
	
	@Override
	public String toString() {
		return parameterlist.toString().replace("[", "(").replace("]", ")");
	}
}

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
		return list.toString().replace("[", "(").replace("]", ")");
	}
}

// =============================================================================

class StatementList extends Component {

	private LinkedList<Statement> statementlist;

	public StatementList(Statement statement) {
		statementlist = new LinkedList<Statement>();
		statementlist.add(statement);
	}

	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		for (Statement stmt : statementlist) {
			stmt.eval(symbolTable);
		}
		return null;
	}

	public void insert(Statement s) {
		// we need to add it to the front of the list
		statementlist.add(0, s);
	}

	public LinkedList<Statement> getStatements() {
		return statementlist;
	}

	@Override
	public void setStaticScope(Scope scope) {
		this.scope = scope;
		for (Statement s: statementlist)
			s.setStaticScope(scope);
	}
	
	@Override
	public String toString() {
		String res = "";
		for (Statement s: statementlist)
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
	public void setStaticScope(Scope scope) {}
	
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
