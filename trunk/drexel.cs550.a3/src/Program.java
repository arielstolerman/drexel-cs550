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
import java.util.Map.Entry;

// =============================================================================
// added classes and enums
// =============================================================================

/**
 * Enum for symbol table entry type
 */
enum SymbolType {
	TEMP("temporary"), VAR("variable"), CONST("constant"), LABEL("label");
	
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
	private Integer value;
	private SymbolType type;
	private Integer addr;

	// getters

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
		return new SymbolValue(value != null ? value.intValue() : null, type,
				null);
	}
	
	// setters
	
	public void setAddr(int addr) {
		this.addr = addr;
	}

	/**
	 * Private constructor from value, type and address.
	 */
	private SymbolValue(Integer value, SymbolType type, Integer addr) {
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
		symbolTable.put(name, new SymbolValue(null, SymbolType.TEMP, null));
		return name;
	}

	/**
	 * Adds a new label entry to the symbol table, w.r.t the global label
	 * counter.
	 */
	public static String addLabel(HashMap<String, SymbolValue> symbolTable) {
		String name = "L" + LABEL_COUNTER++;
		symbolTable.put(name, new SymbolValue(null, SymbolType.TEMP, null));
		return name;
	}

	/**
	 * Checks if the given variable name exists in the symbol table, and adds it
	 * if not. Returns the variable name.
	 */
	public static String updateVar(HashMap<String, SymbolValue> symbolTable,
			String var) {
		if (!symbolTable.containsKey(var))
			symbolTable.put(var, new SymbolValue(null, SymbolType.VAR, null));
		return var;
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
					new SymbolValue(value, SymbolType.CONST, null));

		return name;
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
		String res = label == null ? "     " : String.format("%-5s", label
				+ ":");
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
			HashMap<String, SymbolValue> symbolTable);
}

// =============================================================================

abstract class Expr extends Component {

	/**
	 * Returns the translation of applying the given binary operator on the
	 * given expressions.
	 */
	public static LinkedList<Instruction> getBinaryOpInsts(Expr expr1,
			Expr expr2, InstructionType op,
			HashMap<String, SymbolValue> symbolTable) {
		LinkedList<Instruction> res = new LinkedList<>();
		// translate two sub-expressions
		LinkedList<Instruction> code1 = expr1.translate(symbolTable);
		LinkedList<Instruction> code2 = expr2.translate(symbolTable);
		String t1 = code1.getLast().arg();
		String t2 = code2.getLast().arg();
		// create final expression
		// code1 (T1)
		res.addAll(code1);
		// code2 (T2)
		res.addAll(code2);
		// LDA T1
		res.add(new Instruction(InstructionType.LDA, t1));
		// OP t2
		res.add(new Instruction(op, t2));
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
			HashMap<String, SymbolValue> symbolTable) {
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
			HashMap<String, SymbolValue> symbolTable) {
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
			HashMap<String, SymbolValue> symbolTable) {
		return getBinaryOpInsts(expr1, expr2, InstructionType.MUL, symbolTable);
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
			HashMap<String, SymbolValue> symbolTable) {
		return getBinaryOpInsts(expr1, expr2, InstructionType.ADD, symbolTable);
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
			HashMap<String, SymbolValue> symbolTable) {
		return getBinaryOpInsts(expr1, expr2, InstructionType.SUB, symbolTable);
	}
}

abstract class Statement extends Component {
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
			HashMap<String, SymbolValue> symbolTable) {
		LinkedList<Instruction> res = new LinkedList<>();
		LinkedList<Instruction> exprCode = expr.translate(symbolTable);
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
			HashMap<String, SymbolValue> symbolTable) {
		LinkedList<Instruction> res = new LinkedList<>();
		// code_e
		LinkedList<Instruction> cond = expr.translate(symbolTable);
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
		LinkedList<Instruction> code1 = stmtlist1.translate(symbolTable);
		res.addAll(code1);
		// JMP L2
		String l2 = SymbolValue.addLabel(symbolTable);
		res.add(new Instruction(InstructionType.JMP, l2));
		// L1: code2
		LinkedList<Instruction> code2 = stmtlist2.translate(symbolTable);
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
			HashMap<String, SymbolValue> symbolTable) {
		LinkedList<Instruction> res = new LinkedList<>();
		// L1: code_e
		String l1 = SymbolValue.addLabel(symbolTable);
		LinkedList<Instruction> cond = expr.translate(symbolTable);
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
		LinkedList<Instruction> body = stmtlist.translate(symbolTable);
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
			HashMap<String, SymbolValue> symbolTable) {
		LinkedList<Instruction> res = new LinkedList<>();
		// L1: code_s
		String l1 = SymbolValue.addLabel(symbolTable);
		LinkedList<Instruction> body = stmtlist.translate(symbolTable);
		body.getFirst().setLabel(l1);
		res.addAll(body);
		// code_e
		LinkedList<Instruction> cond = expr.translate(symbolTable);
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

class StatementList extends Component {

	private LinkedList<Statement> statementlist;

	public StatementList(Statement statement) {
		statementlist = new LinkedList<Statement>();
		statementlist.add(statement);
	}
	
	public void insert(Statement s) {
		// we need to add it to the front of the list
		statementlist.add(0, s);
	}

	public LinkedList<Statement> getStatements() {
		return statementlist;
	}

	@Override
	public LinkedList<Instruction> translate(
			HashMap<String, SymbolValue> symbolTable) {
		LinkedList<Instruction> res = new LinkedList<>();
		for (Statement stmt : statementlist)
			res.addAll(stmt.translate(symbolTable));
		return res;
	}
}

class Program {
	
	// for output files
	private static String TRANS_PATH = "trans.txt";
	private static String TRANS_LINK_PATH = "linked.txt";
	private static String OPT_PATH = "opt.txt";
	private static String OPT_LINK_PATH = "opt_linked.txt";
	
	private StatementList stmtlist;
	private LinkedList<Instruction> trans;	// translated
	private LinkedList<Instruction> opt;	// optimized
	private HashMap<String, SymbolValue> symbolTable;		// translated
	private HashMap<String, SymbolValue> optSymbolTable;	// optimized

	public Program(StatementList list) {
		stmtlist = list;
		symbolTable = new HashMap<>();
	}
	
	// -------------------------------------------------------------------------
	// compile methods
	// -------------------------------------------------------------------------
	
	/**
	 * Translates, optimizes and links the code.
	 */
	public void compile() {
		translate();
		optimize();
	}
	
	/**
	 * Translates the program to RAL with symbolic representation.
	 */
	public void translate() {
		trans = stmtlist.translate(symbolTable);
		// add halt at end if does not exist
		if (trans.getLast().type() != InstructionType.HLT)
			trans.add(new Instruction(InstructionType.HLT, null));
		// iterate and merge label-only instructions with following instructions
		Iterator<Instruction> iter = trans.iterator();
		if (!iter.hasNext())
			return;
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
		// link
		link(symbolTable);
	}

	/**
	 * Optimizes the translated set of instructions with peephole optimization. 
	 */
	public void optimize() {
		// if not translated yet, return
		if (trans == null)
			return;
		
		// copy translated and symbol table
		opt = new LinkedList<>();
		for (Instruction inst: trans)
			opt.add(new Instruction(inst));
		optSymbolTable = new HashMap<>();
		for (String key: symbolTable.keySet())
			optSymbolTable.put(key, symbolTable.get(key).getCopyNoAddr());
		
		// 1) remove redundant sta->lda on the same var
		//	  leave only the sta command
		ArrayList<Instruction> insts = new ArrayList<>(opt);
		int size = insts.size();
		Instruction curr, next;
		for (int i = 0; i < size - 1; i++) {
			curr = insts.get(i);
			next = insts.get(i + 1);
			if (curr == null)
				continue;
			if (curr.type() == InstructionType.STA &&
				next.type() == InstructionType.LDA &&
				curr.arg() == next.arg()) {
				// remove redundant load instruction
				insts.set(i + 1, null);
			}
		}
		for (Iterator<Instruction> iter = insts.iterator(); iter.hasNext();)
			if (iter.next() == null)
				iter.remove();
		opt = new LinkedList<>(insts);
		
		// TODO optimize more?
		
		// remove unused symbols from symbol table
		Set<String> used = new HashSet<>();
		for (Instruction inst: opt)
			used.add(inst.arg());
		Set<String> unused = new HashSet<>();
		for (String key: optSymbolTable.keySet())
			if (!used.contains(key))
				unused.add(key);
		for (String key: unused)
			optSymbolTable.remove(key);
		
		// link
		link(optSymbolTable);
	}

	/**
	 * Links the symbols in the symbol table to hard-coded addresses.
	 */
	public void link(HashMap<String, SymbolValue> symbolTable) {
		// iterate over symbols and count consts and vars
		// update label address to line
		int line = 0;
		int consts = 0, vars = 0;
		SymbolValue symVal;
		for (String key: symbolTable.keySet()) {
			line++;
			switch ((symVal = symbolTable.get(key)).type()) {
			case CONST:
				consts++;
				break;
			case LABEL:
				symVal.setAddr(line);
				break;
			case VAR:
				vars++;
				break;
			case TEMP:
				// do nothing
			}
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
		// optimized (symbolic)
		dump(OPT_PATH, opt, optSymbolTable, false);
		// optimized (linked)
		dump(OPT_LINK_PATH, opt, optSymbolTable, true);
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
						"; " + val.type());
			}
			pw.flush();
			pw.close();
		} catch (IOException e) {
			System.err.println("Exception thrown while writing program memory " +
					"to file " + path);
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
