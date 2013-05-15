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

	// global label counter
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
	 * Adds a new temporary entry to the given procedure's symbol table, and
	 * assigns the next available offset to it.
	 */
	public static String addTemp(Proc proc, HashMap<String, SymbolValue> consts) {
		int offset = proc.getOffsetAndIncrement();
		updateConst(consts, offset);
		String name = "T" + offset;
		proc.symbolTable().put(name,
				new SymbolValue(name, null, SymbolType.TEMP, offset));
		return name;
	}

	/**
	 * Adds a new label entry to the symbol table, w.r.t the global label
	 * counter.
	 */
	public static String addLabel(Proc proc) {
		String name = "L" + LABEL_COUNTER++;
		proc.symbolTable().put(name,
				new SymbolValue(name, null, SymbolType.LABEL, null));
		return name;
	}
	
	/**
	 * Checks if the given parameter name exists in the symbol table, and
	 * returns null as error if found. Otherwise, adds the parameter with the
	 * given offset as address. Returns the parameter name if added.
	 */
	public static String addParam(Proc proc,
			HashMap<String, SymbolValue> consts, String param) {
		// if already defined, return null (as error)
		if (proc.symbolTable().containsKey(param))
			return null;
		
		int offset = proc.getOffsetAndIncrement();
		updateConst(consts, offset);
		proc.symbolTable().put(param, new SymbolValue(param, null,
				SymbolType.PARAM, offset));
		return param;
	}

	/**
	 * Checks if the given variable name exists in the symbol table, and adds it
	 * if not. Returns the variable name.
	 */
	public static String updateVar(Proc proc,
			HashMap<String, SymbolValue> consts, String var) {
		if (!proc.symbolTable().containsKey(var)) {
			int offset = proc.getOffsetAndIncrement();
			updateConst(consts, offset);
			proc.symbolTable().put(var,
					new SymbolValue(var, null, SymbolType.VAR, offset));
		}
		return var;
	}

	/**
	 * Updates the given symbol table to hold the given const entry, in case it
	 * does not already contains it. Constant values are given names
	 * corresponding to their values (e.g. 23 will be named TWO_THREE). Returns
	 * the name of the symbol table entry key.
	 */
	public static String updateConst(HashMap<String, SymbolValue> consts,
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
		if (!consts.containsKey(name))
			consts.put(name,
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
	LDA, LDI, STA, STI, ADD, SUB, MUL, JMP, JMI, JMZ, JMN, CAL, HLT, NONE
}

/**
 * Class for instructions, constructed from optional label, instruction type and
 * variable/temp/const argument.
 */
class Instruction {

	// fields
	private Integer line;
	private String label; // null == no label
	private InstructionType type;
	private String arg;
	
	// private constructors
	
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
	
	// factory
	
	/**
	 * @return the list of instructions corresponding to the given label,
	 *         instruction type and argument. If absolute is true, returns just
	 *         one instruction. Otherwise, constructs the set of instructions to
	 *         get the argument as an offset from FP.
	 */
	public static LinkedList<Instruction> getInstructionsFor(
			HashMap<String, SymbolValue> consts, Proc proc, String label,
			InstructionType type, String arg) {
		// determine whether to use absolute addresses or relative ones
		boolean absolute =
				// constant
				consts.containsKey(arg) ||
				// one of SP / FP / buffers
				arg == Program.SP_ADDR ||
				arg == Program.FP_ADDR ||
				arg == Program.BUFF1_ADDR ||
				arg == Program.BUFF2_ADDR ||
				// instructions that address labels straightforwardly
				type == InstructionType.JMP ||
				type == InstructionType.JMZ ||
				type == InstructionType.JMN ||
				// instructions with no arguments
				type == InstructionType.CAL ||
				type == InstructionType.HLT ||
				type == InstructionType.NONE;
		
		LinkedList<Instruction> res = new LinkedList<>();
		
		if (absolute) {
			// apply the instruction straightforwardly
			res.add(new Instruction(type, arg, label));
		}
		else {
			// backup the current value of AC to BUFF2 (not required for
			// LDA and LDI but done in any case)
			// STA BUFF2
			res.add(new Instruction(InstructionType.STA, Program.BUFF2_ADDR));
			// calculate absolute address from FP + offset
			// LDA FP
			res.add(new Instruction(InstructionType.LDA, Program.FP_ADDR));
			// ADD <offset>
			int offset = proc.symbolTable().get(arg).addr();
			String offsetStr = SymbolValue.updateConst(consts, offset);
			res.add(new Instruction(InstructionType.ADD, offsetStr));
			// STA BUFF1
			res.add(new Instruction(InstructionType.STA, Program.BUFF1_ADDR));
			
			switch (type) {
			case LDA:
				res.add(new Instruction(InstructionType.LDI, Program.BUFF1_ADDR));
				break;
			case LDI:
				res.add(new Instruction(InstructionType.LDI, Program.BUFF1_ADDR));
				res.add(new Instruction(InstructionType.STA, Program.BUFF1_ADDR));
				res.add(new Instruction(InstructionType.LDI, Program.BUFF1_ADDR));
				break;
			case STA:
				res.add(new Instruction(InstructionType.LDA, Program.BUFF2_ADDR));
				res.add(new Instruction(InstructionType.STI, Program.BUFF1_ADDR));
				break;
			case STI:
				res.add(new Instruction(InstructionType.LDI, Program.BUFF1_ADDR));
				res.add(new Instruction(InstructionType.STA, Program.BUFF1_ADDR));
				res.add(new Instruction(InstructionType.LDA, Program.BUFF2_ADDR));
				res.add(new Instruction(InstructionType.STI, Program.BUFF1_ADDR));
				break;
			case ADD:
				res.add(new Instruction(InstructionType.LDI, Program.BUFF1_ADDR));
				res.add(new Instruction(InstructionType.ADD, Program.BUFF2_ADDR));
				break;
			case SUB:
				res.add(new Instruction(InstructionType.LDI, Program.BUFF1_ADDR));
				res.add(new Instruction(InstructionType.STA, Program.BUFF1_ADDR));
				res.add(new Instruction(InstructionType.LDA, Program.BUFF2_ADDR));
				res.add(new Instruction(InstructionType.SUB, Program.BUFF1_ADDR));
				break;
			case MUL:
				res.add(new Instruction(InstructionType.LDI, Program.BUFF1_ADDR));
				res.add(new Instruction(InstructionType.MUL, Program.BUFF2_ADDR));
				break;
			case JMP:
				// never gets here
				break;
			case JMI:
				res.add(new Instruction(InstructionType.JMI, Program.BUFF1_ADDR));
				break;
			case JMZ:
				// never gets here
				break;
			case JMN:
				// never gets here
				break;
			case CAL:
				// never gets here
				break;
			case HLT:
				// never gets here
				break;
			case NONE:
				// never gets here
				break;
			}
		}
		return res;
	}

	/**
	 * Returns the string representation of the instruction. If the linked
	 * parameter is true, returns the final address as the instruction argument.
	 */
	public String toString(HashMap<String, SymbolValue> consts,
			HashMap<String, SymbolValue> symbolTable,
			SortedMap<String, Proc> functionTable, boolean linked) {		
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
			if (linked) {
				if (symbolTable.containsKey(arg))
					// parameter, variable, temporary
					res += " " + symbolTable.get(arg).addr();
				else if (consts.containsKey(arg))
					// constant
					res += " " + consts.get(arg).addr();
				else if (functionTable.containsKey(arg))
					// function
					res += " " + functionTable.get(arg).getAddr();
				else {
					// error
					System.err.println("unrecognized argument in line " + line
							+ ": " + arg);
					System.exit(-1);
				}
			}
			else
				res += " " + arg;
		}
		// semicolon
		if (type != InstructionType.NONE)
		{
			res += " ;";
			if (line != null)
				res += " (line " + line + ")";
		}
		return res;
	}

	// getters

	public Integer line() {
		return line;
	}
	
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
	
	public void setLine(int line) {
		this.line = line;
	}
	
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
			HashMap<String, SymbolValue> consts,
			Proc proc,
			SortedMap<String, Proc> functionTable);
}

// =============================================================================

abstract class Expr extends Component {

	/**
	 * Returns the translation of applying the given binary operator on the
	 * given expressions.
	 */
	public static LinkedList<Instruction> getBinaryOpInsts(Expr expr1,
			Expr expr2, InstructionType op,
			HashMap<String, SymbolValue> consts,
			Proc proc,
			SortedMap<String, Proc> functionTable) {
		LinkedList<Instruction> res = new LinkedList<>();
		// translate two sub-expressions
		LinkedList<Instruction> code1 = expr1.translate(consts, proc,
				functionTable);
		LinkedList<Instruction> code2 = expr2.translate(consts, proc,
				functionTable);
		String t1 = code1.getLast().arg();
		String t2 = code2.getLast().arg();
		// create final expression
		// code1 (T1)
		res.addAll(code1);
		// code2 (T2)
		res.addAll(code2);
		// LDA T1
		res.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.LDA, t1));
		// OP T2
		res.addAll(Instruction.getInstructionsFor(consts, proc, null, op, t2));
		// ST t3
		res.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.STA, SymbolValue.addTemp(proc,consts)));
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
			HashMap<String, SymbolValue> consts,
			Proc proc,
			SortedMap<String, Proc> functionTable) {
		LinkedList<Instruction> res = new LinkedList<>();
		// LDA IDENT
		res.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.LDA, SymbolValue.updateVar(proc, consts, name)));
		// STA t_n
		res.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.STA, SymbolValue.addTemp(proc, consts)));
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
			HashMap<String, SymbolValue> consts,
			Proc proc,
			SortedMap<String, Proc> functionTable) {
		LinkedList<Instruction> res = new LinkedList<>();
		// LDA NUMBER
		res.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.LDA, SymbolValue.updateConst(consts, value)));
		// STA t_n
		res.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.STA, SymbolValue.addTemp(proc, consts)));
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
			HashMap<String, SymbolValue> consts,
			Proc proc,
			SortedMap<String, Proc> functionTable) {
		return getBinaryOpInsts(expr1, expr2, InstructionType.MUL, consts,
				proc, functionTable);
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
			HashMap<String, SymbolValue> consts,
			Proc proc,
			SortedMap<String, Proc> functionTable) {
		return getBinaryOpInsts(expr1, expr2, InstructionType.ADD, consts,
				proc, functionTable);
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
			HashMap<String, SymbolValue> consts,
			Proc proc,
			SortedMap<String, Proc> functionTable) {
		return getBinaryOpInsts(expr1, expr2, InstructionType.SUB, consts,
				proc, functionTable);
	}
}

class FunctionCall extends Expr {

    private String funcid;
    private ExpressionList explist;

    public FunctionCall(String id, ExpressionList el) {
        funcid = id;
        explist = el;
    }

	@Override
	public LinkedList<Instruction> translate(
			HashMap<String, SymbolValue> consts,
			Proc proc,
			SortedMap<String, Proc> functionTable) {
		
		LinkedList<Instruction> insts = new LinkedList<>();
		// get the relevant procedure and record size
		Proc procToCall = functionTable.get(funcid);
		int size = procToCall.getActivationRecordSize();
		// make sure number of parameters fits
		if (procToCall.numParams() != explist.getExpressions().size()) {
			System.err.println("Param count does not match");
			System.exit(-1);
		}
		
		// --- calculate all parameter expressions (stored in temps) ---
		LinkedList<Instruction> exprInsts;
		LinkedList<String> paramTemps = new LinkedList<>();
		for (Expr e: explist.getExpressions()) {
			exprInsts = e.translate(consts, proc, functionTable);
			insts.addAll(exprInsts);
			paramTemps.add(exprInsts.getLast().arg());
		}
		
		// --- update FP & SP ---
		
		// update constants table with the size of the callee's activation
		// record and fetch constants "1" and "2" for calculations
		String sizeStr = SymbolValue.updateConst(consts, size);
		String one = SymbolValue.updateConst(consts, 1);
		String two = SymbolValue.updateConst(consts, 2);
		
		// calculate prev_FP position (SP + size - 1) in new activation record
		// and store FP to it
		// LDA SP
		insts.add(new Instruction(InstructionType.LDA, Program.SP_ADDR));
		// ADD <size>
		insts.add(new Instruction(InstructionType.ADD, sizeStr));
		// SUB 1
		insts.add(new Instruction(InstructionType.SUB, one));
		// STA BUFF1
		insts.add(new Instruction(InstructionType.STA, Program.BUFF1_ADDR));
		// store current FP in prev_FP address
		// LDA FP
		insts.add(new Instruction(InstructionType.LDA, Program.FP_ADDR));
		// STI BUFF1
		insts.add(new Instruction(InstructionType.STI, Program.BUFF1_ADDR));
		
		// update FP (which is current SP)
		// LDA SP
		insts.add(new Instruction(InstructionType.LDA, Program.SP_ADDR));
		// STA FP
		insts.add(new Instruction(InstructionType.STA, Program.FP_ADDR));
		
		// update SP (which is current SP + <size>)
		// LDA SP
		insts.add(new Instruction(InstructionType.LDA, Program.SP_ADDR));
		// ADD <size>
		insts.add(new Instruction(InstructionType.ADD, sizeStr));
		// STA SP
		insts.add(new Instruction(InstructionType.STA, Program.SP_ADDR));
		
		// --- calculate and store parameters ---
		
		// set all callee parameters in callee activation record
		int offset = 1; // parameter offsets always start at 1
		String offsetStr;
		for (String paramTemp: paramTemps) {
			// calculate value to set, by accessing the absolute address of paramTemp
			// calculated from prev_FP (= curr SP - 1) + temp's offset
			// LDA SP
			insts.add(new Instruction(InstructionType.LDA, Program.SP_ADDR));
			// SUB 1
			insts.add(new Instruction(InstructionType.SUB, one));
			// STA BUFF1 (BUFF1 now contains address of prev_FP)
			insts.add(new Instruction(InstructionType.STA, Program.BUFF1_ADDR));
			// LDI BUFF1
			insts.add(new Instruction(InstructionType.LDI, Program.BUFF1_ADDR));
			// ADD <temp-offset>
			offsetStr = SymbolValue.updateConst(consts,
					proc.symbolTable().get(paramTemp).addr());
			insts.add(new Instruction(InstructionType.ADD, offsetStr));
			// STA BUFF1 (BUFF1 now contains address of param's temp value)
			insts.add(new Instruction(InstructionType.STA, Program.BUFF1_ADDR));
			
			// calculate absolute address for current parameter
			// LDA FP
			insts.add(new Instruction(InstructionType.LDA, Program.FP_ADDR));
			// ADD <offset>
			offsetStr = SymbolValue.updateConst(consts, offset);
			offset++;
			insts.add(new Instruction(InstructionType.ADD, offsetStr));
			// STA BUFF2
			insts.add(new Instruction(InstructionType.STA, Program.BUFF2_ADDR));
			
			// load temp value and store it in activation record
			// LDI BUFF1
			insts.add(new Instruction(InstructionType.LDI, Program.BUFF1_ADDR));
			// STI BUFF2
			insts.add(new Instruction(InstructionType.STI, Program.BUFF2_ADDR));
		}
		
		// --- call the procedure (implicitly stores return address in SP) ---
		// CAL
		insts.add(new Instruction(InstructionType.CAL, null));
		
		// ---------------------------------------------------------------------
		// callee procedure is running...
		// returns to the following instruction(s) with the return value
		// updated in its activation record
		// ---------------------------------------------------------------------
		
		// --- store callee return value in local temporary ---
		
		// LDA SP
		insts.add(new Instruction(InstructionType.LDA, Program.SP_ADDR));
		// SUB 2
		insts.add(new Instruction(InstructionType.SUB, two));
		// STA BUFF1
		insts.add(new Instruction(InstructionType.STA, Program.BUFF1_ADDR));
		// LDI BUFF1
		insts.add(new Instruction(InstructionType.LDI, Program.BUFF1_ADDR));
		// STA <res>
		String res = SymbolValue.addTemp(proc, consts);
		insts.add(new Instruction(InstructionType.STA, res));
		
		// --- revert to old SP and FP ---

		// revert to old FP (from prev_FP at address current SP - 1)
		// LDA SP
		insts.add(new Instruction(InstructionType.LDA, Program.SP_ADDR));
		// SUB 1
		insts.add(new Instruction(InstructionType.SUB, one));
		// STA BUFF1
		insts.add(new Instruction(InstructionType.STA, Program.BUFF1_ADDR));
		// LDI BUFF1
		insts.add(new Instruction(InstructionType.LDI, Program.BUFF1_ADDR));
		// STA FP
		insts.add(new Instruction(InstructionType.STA, Program.FP_ADDR));
		
		// revert to old SP (current SP - <size>)
		// LDA SP
		insts.add(new Instruction(InstructionType.LDA, Program.SP_ADDR));
		// SUB <size>
		insts.add(new Instruction(InstructionType.SUB, sizeStr));
		// STA SP
		insts.add(new Instruction(InstructionType.STA, Program.SP_ADDR));
		
		// make sure the returned value is the argument of the last instruction
		// of this function call. done arbitrarily using LDA
		// LDA <res>
		insts.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.LDA, res));
		
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
			HashMap<String, SymbolValue> consts,
			Proc proc,
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
			HashMap<String, SymbolValue> consts,
			Proc proc,
			SortedMap<String, Proc> functionTable) {

		LinkedList<Instruction> insts = new LinkedList<>();
		// calculate return value expression
		LinkedList<Instruction> resInsts = expr.translate(consts, proc,
				functionTable);
		String ret = resInsts.getLast().arg();
		// add all expr instructions
		insts.addAll(resInsts);
		
		// store result in return value address
		// first load <ret> and store in BUFF1
		// LDA <ret>
		insts.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.LDA, ret));
		// STA BUFF1
		insts.add(new Instruction(InstructionType.STA, Program.BUFF1_ADDR));
		// now calculate address for return value and store it in BUFF2
		// LDA SP
		insts.add(new Instruction(InstructionType.LDA, Program.SP_ADDR));
		// SUB 2
		String two = SymbolValue.updateConst(consts, 2);
		insts.add(new Instruction(InstructionType.SUB, two));
		// STA BUFF2
		insts.add(new Instruction(InstructionType.STA, Program.BUFF2_ADDR));
		// finally load <ret> from BUFF1 and store (indirect) in BUFF2
		// LDA <ret>
		insts.add(new Instruction(InstructionType.LDA, Program.BUFF1_ADDR));
		// STI BUFF2
		insts.add(new Instruction(InstructionType.STI, Program.BUFF2_ADDR));
		
		// jump indirectly to SP (that holds return address)
		// JMI SP
		insts.add(new Instruction(InstructionType.JMI, Program.SP_ADDR));
		
		return insts;
	}
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
			HashMap<String, SymbolValue> consts,
			Proc proc,
			SortedMap<String, Proc> functionTable) {
		LinkedList<Instruction> insts = new LinkedList<>();
		LinkedList<Instruction> exprCode = expr.translate(consts, proc,
				functionTable);
		String t = exprCode.getLast().arg();
		// expr code
		insts.addAll(exprCode);
		// LDA t
		insts.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.LDA, t));
		// STA IDENT
		insts.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.STA, SymbolValue.updateVar(proc, consts, name)));
		return insts;
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
			HashMap<String, SymbolValue> consts,
			Proc proc,
			SortedMap<String, Proc> functionTable) {
		LinkedList<Instruction> insts = new LinkedList<>();
		// code_e
		LinkedList<Instruction> cond = expr.translate(consts, proc,
				functionTable);
		String t = cond.getLast().arg();
		insts.addAll(cond);
		// LDA t
		insts.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.LDA, t));
		// JMN L1
		String l1 = SymbolValue.addLabel(proc);
		insts.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.JMN, l1));
		// JMZ L1
		insts.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.JMZ, l1));
		// code1
		LinkedList<Instruction> code1 = stmtlist1.translate(consts, proc,
				functionTable);
		insts.addAll(code1);
		// JMP L2
		String l2 = SymbolValue.addLabel(proc);
		insts.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.JMP, l2));
		// L1: code2
		LinkedList<Instruction> code2 = stmtlist2.translate(consts, proc,
				functionTable);
		code2.getFirst().setLabel(l1);
		insts.addAll(code2);
		// L2:
		insts.addAll(Instruction.getInstructionsFor(consts, proc, l2,
				InstructionType.NONE, null));
		return insts;
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
			HashMap<String, SymbolValue> consts,
			Proc proc,
			SortedMap<String, Proc> functionTable) {
		LinkedList<Instruction> insts = new LinkedList<>();
		// L1: code_e
		String l1 = SymbolValue.addLabel(proc);
		LinkedList<Instruction> cond = expr.translate(consts, proc,
				functionTable);
		cond.getFirst().setLabel(l1);
		String t = cond.getLast().arg();
		insts.addAll(cond);
		// LDA t
		insts.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.LDA, t));
		// JMN L2
		String l2 = SymbolValue.addLabel(proc);
		insts.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.JMN, l2));
		// JMZ L2
		insts.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.JMZ, l2));
		// code_s
		LinkedList<Instruction> body = stmtlist.translate(consts, proc,
				functionTable);
		insts.addAll(body);
		// JMP L1
		insts.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.JMP, l1));
		// L2:
		insts.addAll(Instruction.getInstructionsFor(consts, proc, l2,
				InstructionType.NONE, null));
		return insts;
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
			HashMap<String, SymbolValue> consts,
			Proc proc,
			SortedMap<String, Proc> functionTable) {
		LinkedList<Instruction> insts = new LinkedList<>();
		// L1: code_s
		String l1 = SymbolValue.addLabel(proc);
		LinkedList<Instruction> body = stmtlist.translate(consts, proc,
				functionTable);
		body.getFirst().setLabel(l1);
		insts.addAll(body);
		// code_e
		LinkedList<Instruction> cond = expr.translate(consts, proc,
				functionTable);
		String t = cond.getLast().arg();
		insts.addAll(cond);
		// LDA t
		insts.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.LDA, t));
		// JMN L2
		String l2 = SymbolValue.addLabel(proc);
		insts.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.JMN, l2));
		// JMZ L2
		insts.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.JMZ, l2));
		// JMP L1
		insts.addAll(Instruction.getInstructionsFor(consts, proc, null,
				InstructionType.JMP, l1));
		// L2:
		insts.addAll(Instruction.getInstructionsFor(consts, proc, l2,
				InstructionType.NONE, null));
		return insts;
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
			HashMap<String, SymbolValue> consts,
			Proc proc,
			SortedMap<String, Proc> functionTable) {
		LinkedList<Instruction> res = new LinkedList<>();
		for (Statement stmt : statementlist)
			res.addAll(stmt.translate(consts, proc, functionTable));
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
	
	// data for translated code
	private int offsetCounter = 1;
	private LinkedList<Instruction> insts;
	private HashMap<String, SymbolValue> symbolTable;
	private Integer addr = null;
	private Integer numInstructions = null;
	private Integer activationRecordSize = null;
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

	public LinkedList<Instruction> translate(
			HashMap<String, SymbolValue> consts,
			SortedMap<String, Proc> functionTable) {
		// if already translated, return
		if (insts != null)
			return insts;		
		insts = new LinkedList<>();
		
		// --- bind parameters to their offsets ---
		
		String assigned;
		for (String param: parameterlist.getParamList()) {
			// add parameter
			assigned = SymbolValue.addParam(this, consts, param);
			// increase offset
			offsetCounter++;
			if (assigned == null) {
				System.err.println("Parameter " + param + " already defined");
				System.exit(-1);
			}
		}
		
		// --- add body instructions ---
		for (Statement s: stmtlist.getStatements())
			insts.addAll(s.translate(consts, this, functionTable));
		
		// add HLT to end of "main" procedure ONLY
		if (isMain)
			insts.add(new Instruction(InstructionType.HLT, null));
		
		// --- merge label-only instructions with following instructions ---
		Iterator<Instruction> iter = insts.iterator();
		if (iter.hasNext())
		{
			Instruction prev = iter.next(), curr;
			while (iter.hasNext()) {
				curr = iter.next();
				// if prev is a label-only instruction
				// and curr has no label, merge
				if (prev.label() != null && prev.type() == InstructionType.NONE
						&& curr.label() == null) {
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
		
		// update required size of activation record
		// initialized to 3 for:
		// - return value
		// - prev FP
		// - return address
		activationRecordSize = 3;
		// add 1 for each entry in the symbol table except labels
		for (SymbolValue val: symbolTable.values())
			if (val.type() != SymbolType.LABEL &&
				val.type() != SymbolType.CONST) // should never happen anyway
				activationRecordSize++;
		
		return insts;
	}
	
	/**
	 * Sets the start address (line) of the procedure to the given one,
	 * and the lines of every instruction in the procedure body.
	 */
	public void setLinesStartingAt(int procStartLine) {
		// if not translated, do nothing
		if (insts == null)
			return;
		
		addr = procStartLine;
		Iterator<Instruction> iter = insts.iterator();
		int i = addr;
		while (iter.hasNext()) {
			iter.next().setLine(i);
			i++;
		}
	}
	
	/**
	 * @return number of expected parameters.
	 */
	public int numParams() {
		return parameterlist.getParamList().size();
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
	
	/**
	 * @return the required size of the activation record, or null if the
	 * procedure is not translated yet.
	 */
	public Integer getActivationRecordSize() {
		return activationRecordSize;
	}
	
	/**
	 * @return the procedure's symbol table.
	 */
	public HashMap<String, SymbolValue> symbolTable() {
		return symbolTable;
	}
	
	/**
	 * @return the value of the offset prior to incrementing it.
	 */
	public int getOffsetAndIncrement() {
		int currOffset = offsetCounter;
		offsetCounter++;
		return currOffset;
	}
	
	/**
	 * @return the translated instruction list, or null if not translated yet.
	 */
	public LinkedList<Instruction> getTrans() {
		return insts;
	}
}

class Program {
	
	// for output files
	private static String TRANS_PATH = "trans.txt";
	private static String TRANS_LINK_PATH = "linked.txt";
	
	private int initSP;
	private int initFP;
	private StatementList stmtlist;
	private TreeMap<String, LinkedList<Instruction>> trans;
	private HashMap<String, SymbolValue> consts = new HashMap<>();
	private TreeMap<String, Proc> functionTable;
	
	// global SP and FP
	public static String SP_ADDR = "1";
	public static String FP_ADDR = "2";
	public static String BUFF1_ADDR = "3";
	public static String BUFF2_ADDR = "4";
	
	public Program(StatementList list) {
		stmtlist = list;
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
		// - main procedure
		// - all other procedures
		trans = new TreeMap<>();
		for (String procName : functionTable.keySet()) {
			trans.put(procName,
					functionTable.get(procName)
							.translate(consts, functionTable));
		}
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
			currProc.setLinesStartingAt(line);
			line += currProc.numInstructions();
		}
		
		// at this point params, vars and temps are linked (their addresses are
		// offsets w.r.t. their containing procedure) and procedures are linked
		// as well (they have a fixed starting line in the code) so only need to:
		
		// 1) assign addresses to constants
		int constAddr = 5; // 1-4 saved for SP, FP, BUFF1 and BUFF2
		for (SymbolValue constant: consts.values()) {
			constant.setAddr(constAddr);
			constAddr++;
		}
		
		// 2) assign line numbers to labels
		HashMap<String, SymbolValue> symbolTable;
		String label;
		for (String procName: functionTable.keySet()) {
			currProc = functionTable.get(procName);
			symbolTable = currProc.symbolTable();
			for (Instruction inst: currProc.getTrans()) {
				if ((label = inst.label()) != null)
					symbolTable.get(label).setAddr(inst.line());
			}
		}
		
		// finally, set initial values to SP and FP
		initFP = 4 + consts.size();
		initSP = initFP + functionTable.get(Proc.MAIN_NAME)
				.getActivationRecordSize();
	}
	
	// -------------------------------------------------------------------------
	// dump to file methods
	// -------------------------------------------------------------------------
	
	/**
	 * Dumps symbolic, optimized and both linked compiled program to files.
	 */
	public void dump() {
		// translated (symbolic)
		dump(TRANS_PATH, false);
		// translated (linked)
		dump(TRANS_LINK_PATH, true);
	}
	
	/**
	 * Dumps the symbolic / linked program code to file.
	 */
	public void dump(String path, boolean linked) {
		if (trans == null)
			return;
		// dump instructions to file
		try {
			PrintWriter pw = new PrintWriter(new File(path));
			LinkedList<Instruction> insts;
			boolean firstLine;
			String toPrint;
			for (String procName: trans.keySet()) {
				insts = trans.get(procName);
				firstLine = true;
				for (Instruction inst : insts) {
					toPrint = inst.toString(consts, functionTable.get(procName)
							.symbolTable(), functionTable, linked);
					if (firstLine) {
						toPrint += " --- Proc: " + procName + " ---";
						firstLine = false;
					}
					pw.println(toPrint);
				}
				pw.flush();
			}
			pw.close();
		} catch (IOException e) {
			System.err.println("Exception thrown while writing program to " +
					"file " + path);
			System.exit(-1);
		}
		// if linked, dump initial memory image
		if (linked)
			dumpInitMemImage(path);
	}
	
	/**
	 * Dumps initial FP, SP, buffers, constants and main's symbol table to
	 * the given linked memory file. Should be used only for linked.
	 */
	private void dumpInitMemImage(String path) {
		path = path.replace(".txt","_mem.txt");
		SortedMap<Integer,String> mem = new TreeMap<>();
		// SP
		mem.put(1, initSP + "");
		// FP
		mem.put(2, initFP + "");
		// buffers
		mem.put(3, 0 + "");
		mem.put(4, 0 + "");
		// constants
		for (SymbolValue constant: consts.values()) {
			mem.put(constant.addr(), constant.value() + "");
		}
		// main's symbol table
		HashMap<String, SymbolValue> mainSymbolTable = functionTable.get(
				Proc.MAIN_NAME).symbolTable();
		SymbolValue val;
		for (String key: mainSymbolTable.keySet()) {
			val = mainSymbolTable.get(key);
			if (val.type() == SymbolType.LABEL)
				continue;
			if (val.addr() == null) {
				System.out.println("Warning! address for " + key + " is null " +
						"while dumping linked RAL memory to file " + path);
				continue;
			}
			// put value in absolute address, calculated from FP + offset
			mem.put(initFP + val.addr(), val.value() + "");
		}
		
		// finally, write to mem file
		try {
			PrintWriter pw = new PrintWriter(new File(path));
			String content;
			for (int key: mem.keySet()) {
				content = mem.get(key);
				pw.println(key + "\t" + (content == null ? 0 : content) + "; ");
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
		LinkedList<Instruction> insts;
		for (String procName : trans.keySet()) {
			insts = trans.get(procName);
			for (Instruction inst : insts)
				res += inst.toString(consts, functionTable.get(procName)
						.symbolTable(), functionTable, linked)
						+ "\n";
		}
		return res;
	}
}
