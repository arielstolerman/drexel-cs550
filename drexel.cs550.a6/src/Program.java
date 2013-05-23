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
	
	/**
	 * To hold the symbol table of the environment in which the object is
	 * defined. To be used for procedures in static scoping mode.
	 */
	protected HashMap<String, Elem> staticSymbolTable;
	
	/**
	 * Constructor that binds the object to the symbol table of the environment
	 * in which it was defined.
	 * @param staticSymbolTable
	 */
	public Component(HashMap<String, Elem> staticSymbolTable) {
		this.staticSymbolTable = staticSymbolTable;
	}
	
	/**
	 * Evaluate method
	 */
	public abstract Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException;
	
	@Override
	public abstract String toString();
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

abstract class Expr extends Component {

	public Expr(HashMap<String, Elem> staticSymbolTable) {
		super(staticSymbolTable);
	}

}

class Lst extends Expr {

	private List<Expr> list;

	public Lst(HashMap<String, Elem> staticSymbolTable) {
		super(staticSymbolTable);
		list = new LinkedList<>();
	}

	public Lst(HashMap<String, Elem> staticSymbolTable, ExpressionList el) {
		this(staticSymbolTable);
		list.addAll(el.getExpressions());
	}

	public Lst(HashMap<String, Elem> staticSymbolTable, Expr ex) {
		this(staticSymbolTable);
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

	public Ident(HashMap<String, Elem> staticSymbolTable, String s) {
		super(staticSymbolTable);
		name = s;
	}

	public Elem eval(HashMap<String, Elem> symbolTable) {
		return symbolTable.get(name);
	}

	@Override
	public String toString() {
		return name;
	}
}

class Number extends Expr {

	private Integer value;

	public Number(HashMap<String, Elem> staticSymbolTable, int n) {
		super(staticSymbolTable);
		value = new Integer(n);
	}

	public Number(HashMap<String, Elem> staticSymbolTable, Integer n) {
		super(staticSymbolTable);
		value = n;
	}

	public Elem eval(HashMap<String, Elem> symbolTable) {
		return new Elem(value);
	}

	@Override
	public String toString() {
		return value + "";
	}
}

class Times extends Expr {

	private Expr expr1, expr2;

	public Times(HashMap<String, Elem> staticSymbolTable, Expr op1, Expr op2) {
		super(staticSymbolTable);
		expr1 = op1;
		expr2 = op2;
	}

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
	public String toString() {
		return expr1.toString() + " TIMES " + expr2.toString();
	}
}

class Plus extends Expr {

	private Expr expr1, expr2;

	public Plus(HashMap<String, Elem> staticSymbolTable, Expr op1, Expr op2) {
		super(staticSymbolTable);
		expr1 = op1;
		expr2 = op2;
	}

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
	public String toString() {
		return expr1.toString() + " PLUS " + expr2.toString();
	}
}

class Minus extends Expr {

	private Expr expr1, expr2;

	public Minus(HashMap<String, Elem> staticSymbolTable, Expr op1, Expr op2) {
		super(staticSymbolTable);
		expr1 = op1;
		expr2 = op2;
	}

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
	public String toString() {
		return expr1 + " MINUS " + expr2;
	}
}

class FunctionCall extends Expr {

	private String funcid;
	private ExpressionList explist;

	public FunctionCall(HashMap<String, Elem> staticSymbolTable, String id,
			ExpressionList el) {
		super(staticSymbolTable);
		funcid = id;
		explist = el;
	}

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

	public Concat(HashMap<String, Elem> staticSymbolTable, Expr list1,
			Expr list2) {
		super(staticSymbolTable);
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
	public String toString() {
		return list1 + " || " + list2;
	}
}

class Cons extends Expr {

	private Expr exp;
	private Expr list;

	public Cons(HashMap<String, Elem> staticSymbolTable, Expr exp, Expr list) {
		super(staticSymbolTable);
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
	public String toString() {
		return "CONS ( " + exp + ", " + list + " )";
	}
}

class Car extends Expr {

	private Expr list;

	public Car(HashMap<String, Elem> staticSymbolTable, Expr list) {
		super(staticSymbolTable);
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
	public String toString() {
		return "CAR ( " + list + " )";
	}
}

class Cdr extends Expr {

	private Expr list;

	public Cdr(HashMap<String, Elem> staticSymbolTable, Expr list) {
		super(staticSymbolTable);
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
	public String toString() {
		return "CDR ( " + list + " )";
	}
}

class NullP extends Expr {

	private Expr list;

	public NullP(HashMap<String, Elem> staticSymbolTable, Expr list) {
		super(staticSymbolTable);
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
	public String toString() {
		return "NULLP ( " + list + " )";
	}
}

class IntP extends Expr {

	private Expr exp;

	public IntP(HashMap<String, Elem> staticSymbolTable, Expr exp) {
		super(staticSymbolTable);
		this.exp = exp;
	}

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		return exp.eval(symbolTable).isNum() ? new Elem(1)
				: new Elem(0);
	}

	@Override
	public String toString() {
		return "INTP ( " + exp + " )";
	}
}

class ListP extends Expr {

	private Expr exp;

	public ListP(HashMap<String, Elem> staticSymbolTable, Expr exp) {
		super(staticSymbolTable);
		this.exp = exp;
	}

	@Override
	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		return exp.eval(symbolTable).isList() ? new Elem(1)
				: new Elem(0);
	}

	@Override
	public String toString() {
		return "LISTP ( " + exp + " )";
	}
}

class Proc extends Expr {

	private ParamList parameterlist;
	private StatementList stmtlist;

	public Proc(HashMap<String, Elem> staticSymbolTable, ParamList pl,
			StatementList sl) {
		super(staticSymbolTable);
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
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
}

// =============================================================================

// statements

abstract class Statement extends Component {
	public Statement(HashMap<String, Elem> staticSymbolTable) {
		super(staticSymbolTable);
	}
}

class ReturnStatement extends Statement {

	private Expr expr;

	public ReturnStatement(HashMap<String, Elem> staticSymbolTable, Expr e) {
		super(staticSymbolTable);
		expr = e;
	}

	public Elem eval(HashMap<String, Elem> symbolTable)
			throws ReturnValue, RuntimeException {
		throw new ReturnValue(expr.eval(symbolTable));
	}

	@Override
	public String toString() {
		return "RETURN " + expr.toString();
	}

}

class AssignStatement extends Statement {

	private String name;
	private Expr expr;

	public AssignStatement(HashMap<String, Elem> staticSymbolTable, String id,
			Expr e) {
		super(staticSymbolTable);
		name = id;
		expr = e;
	}

	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		symbolTable.put(name, expr.eval(symbolTable));
		return null;
	}

	@Override
	public String toString() {
		return name + " := " + expr;
	}
}

class IfStatement extends Statement {

	private Expr expr;
	private StatementList stmtlist1, stmtlist2;

	public IfStatement(HashMap<String, Elem> staticSymbolTable, Expr e,
			StatementList list1, StatementList list2) {
		super(staticSymbolTable);
		expr = e;
		stmtlist1 = list1;
		stmtlist2 = list2;
	}

	public IfStatement(HashMap<String, Elem> staticSymbolTable, Expr e,
			StatementList list) {
		this(staticSymbolTable, e, list, null);
	}

	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		Elem cond = expr.eval(symbolTable);
		if (!cond.isNum())
			throw new RuntimeException("IF condition must be an integer, " +
					"received: " + cond.type());
		if (cond.getNum() > 0) {
			stmtlist1.eval(symbolTable);
		} else if (stmtlist2 != null) {
			stmtlist2.eval(symbolTable);
		}
		return null;
	}

	@Override
	public String toString() {
		return "IF" + expr.toString();
	}
}

class WhileStatement extends Statement {

	private Expr expr;
	private StatementList stmtlist;

	public WhileStatement(HashMap<String, Elem> staticSymbolTable, Expr e,
			StatementList list) {
		super(staticSymbolTable);
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
			throw new RuntimeException("WHILE condition must be an integer: "
					+ "WHILE " + cond + " DO ... invalid");
		}
		return null;
	}

	@Override
	public String toString() {
		return "WHILE " + expr.toString() + " DO " + stmtlist.toString();
	}
}

class RepeatStatement extends Statement {

	private Expr expr;
	private StatementList sl;

	public RepeatStatement(HashMap<String, Elem> staticSymbolTable,
			StatementList list, Expr e) {
		super(staticSymbolTable);
		expr = e;
		sl = list;
	}

	public Elem eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {
		Elem cond = null;
		try {
			do {
				sl.eval(symbolTable);
			} while ((cond = expr.eval(symbolTable)).getNum() > 0);
		} catch (Exception e) {
			throw new RuntimeException("REPEAT condition must be an integer: "
					+ "REPEAT ... UNTIL " + cond + " invalid");
		}
		return null;
	}

	@Override
	public String toString() {
		return "REPEAT " + expr.toString() + " UNTIL" + sl.toString();
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
		return list.toString();
	}
}

// =============================================================================

class StatementList {

	private LinkedList<Statement> statementlist;

	public StatementList(Statement statement) {
		statementlist = new LinkedList<Statement>();
		statementlist.add(statement);
	}

	public void eval(HashMap<String, Elem> symbolTable)
			throws RuntimeException {

		for (Statement stmt : statementlist) {
			stmt.eval(symbolTable);
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

class Program {

	private StatementList stmtlist;

	public Program(StatementList list) {
		stmtlist = list;
	}

	public void eval(HashMap<String, Elem> symbolTable) {

		stmtlist.eval(symbolTable);

	}

	public void dump(HashMap<String, Elem> symbolTable) {
		System.out.println("Dumping out all the variables...");
		if (symbolTable != null) {
			for (String name : symbolTable.keySet()) {
				System.out.println(name + "=" + symbolTable.get(name));
			}
		}
	}
}
