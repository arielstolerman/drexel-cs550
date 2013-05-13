/* this is the cup file for the mini language
 * at http://www.cs.drexel.edu/~jjohnson/2006-07/winter/cs360/lectures/lec6.html
 * created by Xu, 2/5/07
 * 
 * Modified by Mike Kopack for CS550, 2009 Spring Qtr.
 * Should be at the same level of completeness as the Lecture 2c
 * C++ version.
 * 
 * -----------------------------------------------------------------------------
 * 
 * Assignment 3
 * Group 1
 * 
 */

import java.io.*;
import java.util.*;

// =============================================================================
// added classes and enums
// =============================================================================

/**
 * Enum for symbol table entry type
 */
enum SymbolType {
	TEMP("temporary"),
	VAR("variable"),
	PARAM("parameter"),
	CONST("constant"),
	LABEL("label");
	
	private SymbolType(String name) {
		this.name = name;
	}
	
	private String name;
	
	@Override
	public String toString() {
		return name;
	}
}

/**
 * Class for symbol table entry values, which includes value (can be unknown
 * before compilation), type and address (unknown before compilation).
 */
class SymbolValue {

	// global counters
	private static int TEMP_COUNTER = 0;
	private static int LABEL_COUNTER = 1;

	// fields
	private String key;
	private Integer value;
	private SymbolType type;
	private Integer addr;

	// getters
	
	public String key() {
		return key;
	}
	
	public Integer value() {
		return value;
	}

	public SymbolType type() {
		return type;
	}

	public Integer addr() {
		return addr;
	}
	
	/**
	 * Returns a copy of this symbol value without the address.
	 * Used for making fresh copy of symbol table entries for optimized.
	 */
	public SymbolValue getCopyNoAddr() {
		return new SymbolValue(key,
				value != null ? value.intValue() : null,
				type,
				null);
	}
	
	// setters
	
	public void setAddr(int addr) {
		this.addr = addr;
	}

	/**
	 * Private constructor from value, type and address.
	 */
	private SymbolValue(String key, Integer value, SymbolType type,
			Integer addr) {
		this.key = key;
		this.value = value;
		this.type = type;
		this.addr = addr;
	}

	// mapping of constants to names
	private static String[] CONST_TO_NAME = new String[] { "ZERO", "ONE",
			"TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", };

	// factory functions

	/**
	 * Adds a new temporary entry to the symbol table, w.r.t the global
	 * temporary counter.
	 */
	public static String addTemp(HashMap<String, SymbolValue> symbolTable) {
		String name = "T" + TEMP_COUNTER++;
		symbolTable.put(name,
				new SymbolValue(name, null, SymbolType.TEMP, null));
		return name;
	}

	/**
	 * Adds a new label entry to the symbol table, w.r.t the global label
	 * counter.
	 */
	public static String addLabel(HashMap<String, SymbolValue> symbolTable) {
		String name = "L" + LABEL_COUNTER++;
		symbolTable.put(name,
				new SymbolValue(name, null, SymbolType.LABEL, null));
		return name;
	}

	/**
	 * Checks if the given variable name exists in the symbol table, and adds it
	 * if not. Returns the variable name.
	 */
	public static String updateVar(HashMap<String, SymbolValue> symbolTable,
			String var) {
		if (!symbolTable.containsKey(var))
			symbolTable.put(var,
					new SymbolValue(var, null, SymbolType.VAR, null));
		return var;
	}
	
	/**
	 * Checks if the given parameter name exists in the symbol table, and adds
	 * it if not. Returns the parameter name.
	 */
	public static String updateParam(HashMap<String, SymbolValue> symbolTable,
			String param) {
		if (!symbolTable.containsKey(param))
			symbolTable.put(param,
					new SymbolValue(param, null, SymbolType.PARAM, null));
		return param;
	}

	/**
	 * Updates the given symbol table to hold the given const entry, in case it
	 * does not already contains it. Constant values are given names
	 * corresponding to their values (e.g. 23 will be named TWO_THREE). Returns
	 * the name of the symbol table entry key.
	 */
	public static String updateConst(HashMap<String, SymbolValue> symbolTable,
			int value) {
		// map value to corresponding name
		String name = "";
		if (value == 0)
			name = CONST_TO_NAME[0];
		else {
			String valStr = value + "";
			int len = valStr.length();
			for (int i = 0; i < len; i++)
				name += CONST_TO_NAME[Integer.parseInt(valStr.substring(i,
						i + 1))] + "_";
			name = name.substring(0, name.length() - 1);
		}

		// if name does not exist in symbol table, create entry
		if (!symbolTable.containsKey(name))
			symbolTable.put(name,
					new SymbolValue(name, value, SymbolType.CONST, null));

		return name;
	}
	
	@Override
	public String toString() {
		return "key: " + key + ", " + "val: " + value +
				", type: " + type + ", addr: " + addr;
	}
}

/**
 * Enum for RAL instruction types.
 */
enum InstructionType {
	LDA, LDI, STA, STI, ADD, SUB, MUL, JMP, JMZ, JMN, HLT, NONE
}

/**
 * Class for instructions, constructed from optional label, instruction type and
 * variable/temp/const argument.
 */
class Instruction {

	// fields
	private String label; // null == no label
	private InstructionType type;
	private String arg;
	
	/**
	 * Constructor from label only (for label only rows).
	 */
	public Instruction(String label) {
		this.label = label;
		type = InstructionType.NONE;
		arg = null;
	}
	
	/**
	 * Constructor from type and argument.
	 */
	public Instruction(InstructionType type, String arg) {
		label = null;
		this.type = type;
		this.arg = arg;
	}

	/**
	 * Constructor from type, argument and label.
	 */
	public Instruction(InstructionType type, String arg, String label) {
		this(type, arg);
		this.label = label;
	}
	
	/**
	 * Copy constructor.
	 */
	public Instruction(Instruction inst) {
		arg = inst.arg;
		label = inst.label;
		type = inst.type;
	}

	/**
	 * Returns the string representation of the instruction. If the linked
	 * parameter is true, returns the final address as the instruction argument.
	 */
	public String toString(HashMap<String, SymbolValue> symbolTable,
			boolean linked) {
		// add label if exists
		String res;
		if (linked) {
			res = "";
		} else {
			res = label == null ? "     " : String.format("%-5s", label
					+ ":");
		}
		// add instruction
		if (type != InstructionType.NONE)
			res += type;
		// add argument if exists
		if (arg != null) {
			if (linked)
				res += " " + symbolTable.get(arg).addr();
			else
				res += " " + arg;
		}
		// semicolon
		if (type != InstructionType.NONE)
			res += " ;";
		return res;
	}

	// getters

	public String label() {
		return label;
	}

	public InstructionType type() {
		return type;
	}

	public String arg() {
		return arg;
	}
	
	// setters
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setType(InstructionType type) {
		this.type = type;
	}
	
	public void setArg(String arg) {
		this.arg = arg;
	}
}

/**
 * Abstract class to impose implementation of the translate method
 */
abstract class Component {

	public abstract LinkedList<Instruction> translate(
			HashMap<String, SymbolValue> symbolTable,
			SortedMap<String, Proc> functionTable);
}

/**
 * Wrapper for statement "return" values
 */
class ReturnValue extends RuntimeException {

	private Integer retValue;

	public ReturnValue(Integer retValue) {
		this.retValue = retValue;
	}

	public Integer getRetValue() {
		return retValue;
	}
}

// =============================================================================

abstract class Expr extends Component {

	/**
	 * Returns the translation of applying the given binary operator on the
	 * given expressions.
	 */
	public static LinkedList<Instruction> getBinaryOpInsts(Expr expr1,
			Expr expr2, InstructionType op,
			HashMap<String, SymbolValue> symbolTable,
			SortedMap<String, Proc> functionTable) {
		LinkedList<Instruction> res = new LinkedList<>();
		// translate two sub-expressions
		LinkedList<Instruction> code1 = expr1.translate(symbolTable,
				functionTable);
		LinkedList<Instruction> code2 = expr2.translate(symbolTable,
				functionTable);
		String t1 = code1.getLast().arg();
		String t2 = code2.getLast().arg();
		// create final expression
		// code1 (T1)
		res.addAll(code1);
		// code2 (T2)
		res.addAll(code2);
		// if not a subtraction, switch next commands for possible peephole
		if (op != InstructionType.SUB) {
			// LDA T2
			res.add(new Instruction(InstructionType.LDA, t2));
			// OP T1
			res.add(new Instruction(op, t1));
		} else {
			// LDA T1
			res.add(new Instruction(InstructionType.LDA, t1));
			// OP T2
			res.add(new Instruction(op, t2));
		}
		// ST t3
		res.add(new Instruction(InstructionType.STA, SymbolValue
				.addTemp(symbolTable)));
		return res;
	}
}

class Ident extends Expr {

	private String name;

	public Ident(String s) {
		name = s;
	}

	@Override
	public LinkedList<Instruction> translate(
			HashMap<String, SymbolValue> symbolTable,
			SortedMap<String, Proc> functionTable) {
		LinkedList<Instruction> res = new LinkedList<>();
		// LDA IDENT
		res.add(new Instruction(InstructionType.LDA, SymbolValue.updateVar(
				symbolTable, name)));
		// STA t_n
		res.add(new Instruction(InstructionType.STA, SymbolValue
				.addTemp(symbolTable)));
		return res;
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
	public LinkedList<Instruction> translate(
			HashMap<String, SymbolValue> symbolTable,
			SortedMap<String, Proc> functionTable) {
		LinkedList<Instruction> res = new LinkedList<>();
		// LDA NUMBER
		res.add(new Instruction(InstructionType.LDA, SymbolValue.updateConst(
				symbolTable, value)));
		// STA t_n
		res.add(new Instruction(InstructionType.STA, SymbolValue
				.addTemp(symbolTable)));
		return res;
	}
}

class Times extends Expr {

	private Expr expr1, expr2;

	public Times(Expr op1, Expr op2) {
		expr1 = op1;
		expr2 = op2;
	}

	@Override
	public LinkedList<Instruction> translate(
			HashMap<String, SymbolValue> symbolTable,
			SortedMap<String, Proc> functionTable) {
		return getBinaryOpInsts(expr1, expr2, InstructionType.MUL, symbolTable,
				functionTable);
	}
}

class Plus extends Expr {

	private Expr expr1, expr2;

	public Plus(Expr op1, Expr op2) {
		expr1 = op1;
		expr2 = op2;
	}

	@Override
	public LinkedList<Instruction> translate(
			HashMap<String, SymbolValue> symbolTable,
			SortedMap<String, Proc> functionTable) {
		return getBinaryOpInsts(expr1, expr2, InstructionType.ADD, symbolTable,
				functionTable);
	}
}

class Minus extends Expr {

	private Expr expr1, expr2;

	public Minus(Expr op1, Expr op2) {
		expr1 = op1;
		expr2 = op2;
	}

	@Override
	public LinkedList<Instruction> translate(
			HashMap<String, SymbolValue> symbolTable,
			SortedMap<String, Proc> functionTable) {
		return getBinaryOpInsts(expr1, expr2, InstructionType.SUB, symbolTable,
				functionTable);
	}
}

class FunctionCall extends Expr {

    private String funcid;
    private ExpressionList explist;

    public FunctionCall(String id, ExpressionList el) {
        funcid = id;
        explist = el;
    }

//    public Integer eval(HashMap<String, Integer> nametable, SortedMap<String, Proc> functionTable, LinkedList var) {
//        return functiontable.get(funcid).apply(nametable, functiontable, var, explist);
//    }

	@Override
	public LinkedList<Instruction> translate(
			HashMap<String, SymbolValue> symbolTable,
			SortedMap<String, Proc> functionTable) {
		LinkedList<Instruction> insts = new LinkedList<>();
		//TODO prepare activation record
		
		// JMP to callee
		insts.add(new Instruction(InstructionType.JMP, funcid));
		return insts;
	}
}


abstract class Statement extends Component {}

class DefineStatement extends Statement {

	private String name;
	private Proc proc;

	public DefineStatement(String id, Proc process) {
		name = id;
		proc = process;
	}

	/**
	 * Adds the underlying procedure to the given function table.
	 * @param functionTable
	 */
	public void translate(SortedMap<String, Proc> functionTable) {
		functionTable.put(name, proc);
	}

	@Override
	/**
	 * Should never be called!
	 */
	public LinkedList<Instruction> translate(
			HashMap<String, SymbolValue> symbolTable,
			SortedMap<String, Proc> functionTable) {
		return null;
	}
}

class ReturnStatement extends Statement {

	private Expr expr;

	public ReturnStatement(Expr e) {
		expr = e;
	}

	@Override
	public LinkedList<Instruction> translate(
			HashMap<String, SymbolValue> symbolTable,
			SortedMap<String, Proc> functionTable) {
		// TODO Auto-generated method stub
		return null;
	}

//	public void eval(HashMap<String, Element> nametable,
//			SortedMap<String, Proc> functionTable, LinkedList var)
//			throws ReturnValue, RuntimeException {
//		throw new ReturnValue(expr.eval(nametable, functiontable, var));
//	}
}

class AssignStatement extends Statement {

	private String name;
	private Expr expr;

	public AssignStatement(String id, Expr e) {
		name = id;
		expr = e;
	}
	
	@Override
	public LinkedList<Instruction> translate(
			HashMap<String, SymbolValue> symbolTable,
			SortedMap<String, Proc> functionTable) {
		LinkedList<Instruction> res = new LinkedList<>();
		LinkedList<Instruction> exprCode = expr.translate(symbolTable,
				functionTable);
		String t = exprCode.getLast().arg();
		// expr code
		res.addAll(exprCode);
		// LDA t
		res.add(new Instruction(InstructionType.LDA, t));
		// STA IDENT
		res.add(new Instruction(InstructionType.STA, SymbolValue.updateVar(
				symbolTable, name)));
		return res;
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
	
	@Override
	public LinkedList<Instruction> translate(
			HashMap<String, SymbolValue> symbolTable,
			SortedMap<String, Proc> functionTable) {
		LinkedList<Instruction> res = new LinkedList<>();
		// code_e
		LinkedList<Instruction> cond = expr.translate(symbolTable,functionTable);
		String t = cond.getLast().arg();
		res.addAll(cond);
		// LDA t
		res.add(new Instruction(InstructionType.LDA, t));
		// JMN L1
		String l1 = SymbolValue.addLabel(symbolTable);
		res.add(new Instruction(InstructionType.JMN, l1));
		// JMZ L1
		res.add(new Instruction(InstructionType.JMZ, l1));
		// code1
		LinkedList<Instruction> code1 = stmtlist1.translate(symbolTable,
				functionTable);
		res.addAll(code1);
		// JMP L2
		String l2 = SymbolValue.addLabel(symbolTable);
		res.add(new Instruction(InstructionType.JMP, l2));
		// L1: code2
		LinkedList<Instruction> code2 = stmtlist2.translate(symbolTable,
				functionTable);
		code2.getFirst().setLabel(l1);
		res.addAll(code2);
		// L2:
		res.add(new Instruction(l2));
		return res;
	}
}

class WhileStatement extends Statement {

	private Expr expr;
	private StatementList stmtlist;

	public WhileStatement(Expr e, StatementList list) {
		expr = e;
		stmtlist = list;
	}
	
	@Override
	public LinkedList<Instruction> translate(
			HashMap<String, SymbolValue> symbolTable,
			SortedMap<String, Proc> functionTable) {
		LinkedList<Instruction> res = new LinkedList<>();
		// L1: code_e
		String l1 = SymbolValue.addLabel(symbolTable);
		LinkedList<Instruction> cond = expr.translate(symbolTable,
				functionTable);
		cond.getFirst().setLabel(l1);
		String t = cond.getLast().arg();
		res.addAll(cond);
		// LDA t
		res.add(new Instruction(InstructionType.LDA, t));
		// JMN L2
		String l2 = SymbolValue.addLabel(symbolTable);
		res.add(new Instruction(InstructionType.JMN, l2));
		// JMZ L2
		res.add(new Instruction(InstructionType.JMZ, l2));
		// code_s
		LinkedList<Instruction> body = stmtlist.translate(symbolTable,
				functionTable);
		res.addAll(body);
		// JMP L1
		res.add(new Instruction(InstructionType.JMP, l1));
		// L2:
		res.add(new Instruction(l2));
		return res;
	}
}

class RepeatStatement extends Statement {

	private Expr expr;
	private StatementList stmtlist;

	public RepeatStatement(StatementList list, Expr e) {
		expr = e;
		stmtlist = list;
	}
	
	@Override
	public LinkedList<Instruction> translate(
			HashMap<String, SymbolValue> symbolTable,
			SortedMap<String, Proc> functionTable) {
		LinkedList<Instruction> res = new LinkedList<>();
		// L1: code_s
		String l1 = SymbolValue.addLabel(symbolTable);
		LinkedList<Instruction> body = stmtlist.translate(symbolTable,
				functionTable);
		body.getFirst().setLabel(l1);
		res.addAll(body);
		// code_e
		LinkedList<Instruction> cond = expr.translate(symbolTable,
				functionTable);
		String t = cond.getLast().arg();
		res.addAll(cond);
		// LDA t
		res.add(new Instruction(InstructionType.LDA, t));
		// JMN L2
		String l2 = SymbolValue.addLabel(symbolTable);
		res.add(new Instruction(InstructionType.JMN, l2));
		// JMZ L2
		res.add(new Instruction(InstructionType.JMZ, l2));
		// JMP L1
		res.add(new Instruction(InstructionType.JMP, l1));
		// L2:
		res.add(new Instruction(l2));
		return res;
	}
}

class ParamList {

	private List<String> parameterlist;

	public ParamList() {
		parameterlist = new LinkedList<String>();
	}
	
	public ParamList(String name) {
		this();
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

class StatementList extends Component {

	private LinkedList<Statement> statementlist;

	public StatementList() {
		statementlist = new LinkedList<Statement>();
	}
	
	public StatementList(Statement statement) {
		this();
		statementlist.add(statement);
	}
	
	public void insert(Statement s) {
		// we need to add it to the front of the list
		statementlist.add(0, s);
	}
	
	public void insertFront(Statement s) {
		// add new statement to the front of the list
		statementlist.add(s);
	}

	public LinkedList<Statement> getStatements() {
		return statementlist;
	}

	@Override
	public LinkedList<Instruction> translate(
			HashMap<String, SymbolValue> symbolTable,
			SortedMap<String, Proc> functionTable) {
		LinkedList<Instruction> res = new LinkedList<>();
		for (Statement stmt : statementlist)
			res.addAll(stmt.translate(symbolTable,functionTable));
		return res;
	}
}

class Proc {

	/**
	 * Name of main procedure.
	 */
	public static final String MAIN_NAME = "__main__";
	
	private ParamList parameterlist;
	private StatementList stmtlist;
	private HashMap<String, SymbolValue> symbolTable;
	private Integer addr = null;
	private Integer numInstructions = null;
	private boolean isMain;

	public Proc(ParamList pl, StatementList sl) {
		symbolTable = new HashMap<>();
		parameterlist = pl;
		stmtlist = sl;
	}
	
	// factory method for creating a Proc for "main"
	public static Proc getMainProc(StatementList sl) {
		Proc main = new Proc(new ParamList(),sl);
		main.isMain = true;
		return main;
	}
	
	public Integer apply(SortedMap<String, Proc> functionTable,
			ExpressionList expressionlist) throws RuntimeException {
		//TODO
//		HashMap<String, SymbolValue> newSymbolTable = new HashMap<String, SymbolValue>();
//
//		// bind parameters in new name table
//		// we need to get the underlying List structure that the ParamList
//		// uses...
//		Iterator<String> p = parameterlist.getParamList().iterator();
//		Iterator<Expr> e = expressionlist.getExpressions().iterator();
//
//		if (parameterlist.getParamList().size() != expressionlist
//				.getExpressions().size()) {
//			System.out.println("Param count does not match");
//			System.exit(1);
//		}
//		while (p.hasNext() && e.hasNext()) {
//
//			// assign the evaluation of the expression to the parameter name.
//			newSymbolTable.put(p.next(),
//					e.next().eval(symbolTable, functiontable));
//			// System.out.println("Loading Nametable for procedure with: "+p+" = "+nametable.get(p));
//
//		}
//		// evaluate function body using new name table and
//		// old function table
//		// eval statement list and catch return
//		// System.out.println("Beginning Proceedure Execution..");
//		try {
//			stmtlist.eval(newSymbolTable, functiontable, var);
//		} catch (ReturnValue result) {
//			// Note, the result shold contain the proceedure's return value as a
//			// String
//			// System.out.println("return value = "+result.getMessage());
//			return result.getRetValue();
//		}
//		System.out.println("Error:  no return value");
//		System.exit(1);
//		// need this or the compiler will complain, but should never
//		// reach this...
		return null;
	}

	public LinkedList<Instruction> translate(
			SortedMap<String, Proc> functionTable) {
		LinkedList<Instruction> insts = new LinkedList<>();
		
		// TODO code here
		// - make sure to check if function calls are legal
		
		// add HLT to end of "main" procedure ONLY
		if (isMain)
			insts.add(new Instruction(InstructionType.HLT, null));
		
		// iterate and merge label-only instructions with following instructions
		Iterator<Instruction> iter = insts.iterator();
		if (iter.hasNext())
		{
			Instruction prev = iter.next(), curr;
			while (iter.hasNext()) {
				curr = iter.next();
				// if prev is a label-only instruction
				// and curr has no label, merge
				if (prev.label() != null && prev.type() == InstructionType.NONE &&
						curr.label() == null) {
					prev.setType(curr.type());
					prev.setArg(curr.arg());
					iter.remove(); // delete curr from set
				}
				// otherwise advance prev
				else {
					prev = curr;
				}
			}
		}
		
		// update number of instructions
		numInstructions = insts.size();
		
		return insts;
	}
	
	/**
	 * Sets the start address (line) of the procedure to the given one.
	 * @param procStartAddr
	 */
	public void setAddr(int procStartAddr) {
		addr = procStartAddr;
	}
	
	public Integer getAddr() {
		return addr;
	}
	
	/**
	 * @return the number of instructions in the translated procedure, or
	 * null if the procedure is not translated yet.
	 */
	public Integer numInstructions() {
		return numInstructions;
	}
}

class Program {
	
	// for output files
	private static String TRANS_PATH = "trans.txt";
	private static String TRANS_LINK_PATH = "linked.txt";
	
	private StatementList stmtlist;
	private LinkedList<Instruction> trans;
	//private HashMap<String, SymbolValue> symbolTable;
	private TreeMap<String, Proc> functionTable;

	public Program(StatementList list) {
		stmtlist = list;
		//symbolTable = new HashMap<>();
	}
	
	// -------------------------------------------------------------------------
	// compile methods
	// -------------------------------------------------------------------------
	
	/**
	 * Translates, optimizes and links the code.
	 */
	public void compile() {
		translate();
		link();
	}
	
	/**
	 * Translates the program to RAL with symbolic representation.
	 */
	public void translate() {
		// isolate main()'s code from all other procedures
		StatementList mainStmtList = new StatementList();
		for (Statement stmt: stmtlist.getStatements()) {
			if (stmt instanceof DefineStatement) {
				// add Proc to function table, untranslated
				((DefineStatement) stmt).translate(functionTable);
			} else {
				// add statement to main code
				mainStmtList.insertFront(stmt);
			}
		}
		// create new Proc for main
		functionTable.put(Proc.MAIN_NAME, Proc.getMainProc(mainStmtList));
		
		// translate in the following order:
		// - main code
		// - other procedures code
		trans = new LinkedList<>();
		for (String procName: functionTable.keySet())
			trans.addAll(functionTable.get(procName).translate(functionTable));
	}
	
	/**
	 * Links the symbols in the symbol table to hard-coded addresses.
	 */
	public void link() {
		// assign line numbers to procedure labels
		int line = 1;
		Proc currProc;
		for (String procName: functionTable.keySet()) {
			currProc = functionTable.get(procName);
			currProc.setAddr(line);
			line += currProc.numInstructions();
		}
		
		//TODO move all the following to Proc
		// iterate over symbols and count consts and vars
		int consts = 0, vars = 0;
		SymbolValue symVal;
		for (String key: symbolTable.keySet()) {
			switch ((symVal = symbolTable.get(key)).type()) {
			case CONST:
				consts++;
				break;
			case VAR:
				vars++;
				break;
			case TEMP:
				// do nothing
				break;
			case LABEL:
				// do nothing
				break;
			}
		}
		// update label address to line
		line = 0;
		String label;
		for (Instruction inst: trans) {
			line++;
			if ((label = inst.label()) != null)
				symbolTable.get(label).setAddr(line);
		}
		// initialize address counts (consts -> vars -> temps)
		int constAddr = 1;
		int varAddr = constAddr + consts;
		int tempAddr = varAddr + vars;
		// assign addresses
		for (String key: symbolTable.keySet()) {
			switch ((symVal = symbolTable.get(key)).type()) {
			case CONST:
				symVal.setAddr(constAddr);
				constAddr++;
				break;
			case VAR:
				symVal.setAddr(varAddr);
				varAddr++;
				break;
			case TEMP:
				symVal.setAddr(tempAddr);
				tempAddr++;
				break;
			case LABEL:
				// do nothing
				break;
			}
		}
	}
	
	// -------------------------------------------------------------------------
	// dump to file methods
	// -------------------------------------------------------------------------
	
	/**
	 * Dumps symbolic, optimized and both linked compiled program to files.
	 */
	public void dump() {
		// translated (symbolic)
		dump(TRANS_PATH, trans, symbolTable, false);
		// translated (linked)
		dump(TRANS_LINK_PATH, trans, symbolTable, true);
	}
	
	/**
	 * Dumps the symbolic translated program to file.
	 */
	public void dump(String path, LinkedList<Instruction> insts,
			HashMap<String,SymbolValue> symbolTable, boolean linked) {
		if (insts == null)
			return;
		// dump instructions to file
		try {
			PrintWriter pw = new PrintWriter(new File(path));
			for (Instruction inst: insts)
				pw.println(inst.toString(symbolTable,linked));
			pw.flush();
			pw.close();
		} catch (IOException e) {
			System.err.println("Exception thrown while writing program to " +
					"file " + path);
			System.exit(-1);
		}
		// if linked, dump initial memory image
		if (linked)
			dumpSymbolTable(path, symbolTable);
	}
	
	/**
	 * Dumps symbol table to corresponding mem file.
	 * Should be used only for linked.
	 */
	private void dumpSymbolTable(String path,
			HashMap<String,SymbolValue> symbolTable) {
		path = path.replace(".txt","_mem.txt");
		SortedMap<Integer,SymbolValue> mem = new TreeMap<>();
		SymbolValue val;
		for (String key: symbolTable.keySet()) {
			val = symbolTable.get(key);
			// skip labels
			if (val.type() == SymbolType.LABEL)
				continue;
			if (val.addr() == null) {
				System.out.println("Warning! address for " + key + " is null " +
						"while dumping linked RAL memory to file " + path);
				continue;
			}
			mem.put(val.addr(), val);
		}
		try {
			PrintWriter pw = new PrintWriter(new File(path));
			for (int key: mem.keySet()) {
				val = mem.get(key);
				pw.println(key + "\t" +
						(val.value() == null ? 0 : val.value()) +
						"; " + val.type() + " " + val.key());
			}
			pw.flush();
			pw.close();
		} catch (IOException e) {
			System.err.println("Exception thrown while writing program memory "
					+ "to file " + path);
			System.exit(-1);
		}
	}
	
	// -------------------------------------------------------------------------
	// print methods
	// -------------------------------------------------------------------------
	
	/**
	 * Returns the final compiled RAL program.
	 */
	public String output() {
		return output(true);
	}
	
	/**
	 * Returns the compiled RAL program linked or unlinked.
	 */
	public String output(boolean linked) {
		String res = "";
		for (Instruction inst: trans)
			res += inst.toString(symbolTable, linked) + "\n";
		return res;
	}
}
