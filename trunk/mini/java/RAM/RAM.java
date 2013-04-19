import java.io.*;
import java.util.*;

public class RAM {

	private static final int DEFMEMSIZE = 100;
	private static final int DEFPROGSIZE = 10;
	private static final int MAXSTRSIZE = 160;

	private int[] memory;
	private Instruction[] program;
	private int memorySize;
	private int programSize;
	private int pc;
	private int ac;

	public RAM() {
		memorySize = DEFMEMSIZE;
		programSize = DEFPROGSIZE;
		memory = new int[memorySize+1];
		program = new Instruction[programSize+1];
		pc = 1;  ac = 0;
		for (int i=0;i<=memorySize;i++)
			memory[i] = 0;
	}

	public RAM(int pSize, int mSize)
	{
		memorySize = mSize;
		programSize = pSize;
		memory = new int[memorySize+1];  // indexing for RAM memory starts at 1
		program = new Instruction[programSize+1]; // indexing for RAM program starts at 1
		pc = 1;  ac = 0;
		for (int i=0;i<=memorySize;i++)
			memory[i] = 0;
		for (int i=0;i<=programSize;i++)
			program[i] = new Instruction();
	}

	// Initialize RAM with hardwired program and memory
	// pc is set to 1 and ac is set to 0.
	public void init()
	{
		program[1].opcode = Instruction.OPCODES.LDA;
		program[1].operand = 3;
		program[2].opcode = Instruction.OPCODES.SUB;
		program[2].operand = 4;
		program[3].opcode = Instruction.OPCODES.JMZ;
		program[3].operand = 7;
		program[4].opcode = Instruction.OPCODES.LDA;
		program[4].operand = 1;
		program[5].opcode = Instruction.OPCODES.STA;
		program[5].operand = 5;
		program[6].opcode = Instruction.OPCODES.HLT;
		program[7].opcode = Instruction.OPCODES.LDA;
		program[7].operand = 2;
		program[8].opcode = Instruction.OPCODES.STA;
		program[8].operand = 5;
		program[9].opcode = Instruction.OPCODES.HLT;
		programSize = 9;

		memory[1] = 0;
		memory[2] = 1;
		memory[3] = 2;
		memory[4] = 1;
		memory[5] = 3;
		pc = 1;  ac = 0;
	}

	// Initialize RAM with program in file with the name pInput
	// and initial memory configuration in the file with name mInput
	// pc is set to 1 and ac is set to 0.  programSize is set to the number
	// of instructions read.
	public void init(String pInput, String mInput) {
		// Initialize Memory
		int addr, value;
		String str = new String();

		// Initialize memory

		BufferedReader mFile = null;

		try {
			mFile = new BufferedReader(new FileReader(mInput));
		}
		catch(FileNotFoundException e) {
			System.err.println("Error: memory file not found");
			System.exit(1);
		}

		try {
			String line = null;
			while((line = mFile.readLine()) != null) {
				line = line.split(";")[0]; // remove comments, if any
				StringTokenizer st = new StringTokenizer(line, " \t\n\r\f");
				addr = Integer.parseInt(st.nextToken());
				value = Integer.parseInt(st.nextToken());

				if (addr < 1 || addr > memorySize) {
					System.err.println("Error:  illegal memory location");
					System.exit(1);
				}

				memory[addr] = value;
			}

			mFile.close();
		}
		catch(IOException e) {
			System.err.println("IO Error while reading memory file");
			System.exit(1);
		}

		// Initialize program
		String instName = null;

		BufferedReader pFile = null;
		try {
			pFile = new BufferedReader(new FileReader(pInput));
		}
		catch(FileNotFoundException e) {
			System.err.println("Error: program file not found");
			System.exit(1);
		}

		pc = 1;

		try {
			String line = null;

			while((line = pFile.readLine()) != null) {
				if(line.trim().length() == 0) {
					continue;
				}
				else if(line.trim().startsWith(";")) {
					continue;
				}

				StringTokenizer st = new StringTokenizer(line.trim(), " \t\n\r\f;");
				instName = st.nextToken();
				if(instName.equals("LDA")) {
					program[pc].opcode = Instruction.OPCODES.LDA;
					program[pc].operand = Integer.parseInt(st.nextToken());
					pc++;
				}
				else if(instName.equals("LDI")) {
					program[pc].opcode = Instruction.OPCODES.LDI;
					program[pc].operand = Integer.parseInt(st.nextToken());
					pc++;
				}
				else if(instName.equals("STA")) {
					program[pc].opcode = Instruction.OPCODES.STA;
					program[pc].operand = Integer.parseInt(st.nextToken());
					pc++;
				}
				else if(instName.equals("STI")) {
					program[pc].opcode = Instruction.OPCODES.STI;
					program[pc].operand = Integer.parseInt(st.nextToken());
					pc++;
				}
				else if(instName.equals("ADD")) {
					program[pc].opcode = Instruction.OPCODES.ADD;
					program[pc].operand = Integer.parseInt(st.nextToken());
					pc++;
				}
				else if(instName.equals("SUB")) {
					program[pc].opcode = Instruction.OPCODES.SUB;
					program[pc].operand = Integer.parseInt(st.nextToken());
					pc++;
				}
				else if(instName.equals("JMP")) {
					program[pc].opcode = Instruction.OPCODES.JMP;
					program[pc].operand = Integer.parseInt(st.nextToken());
					pc++;
				}
				else if(instName.equals("JMZ")) {
					program[pc].opcode = Instruction.OPCODES.JMZ;
					program[pc].operand = Integer.parseInt(st.nextToken());
					pc++;
				}
				else if(instName.equals("JMN")) {
					program[pc].opcode = Instruction.OPCODES.JMN;
					program[pc].operand = Integer.parseInt(st.nextToken());
					pc++;
				}
				else if(instName.equals("HLT")) {
					program[pc].opcode = Instruction.OPCODES.HLT;
					pc++;
				}
				else { 
					System.err.println("Error:  Illegal Instruction");
					System.exit(1);
				}

				if (pc-1 > programSize) {
					System.err.println("Error: program too large");
					System.exit(1);
				}
			}

			pFile.close();
		}
		catch(IOException e) {
			System.err.println("IO Error while reading program file");
			System.exit(1);
		}

		programSize = pc;
		pc = 1;
		ac = 0;
	}

	// simulate execution of RAM with given program and memory configuration.
	// Notes:
	//    1. Program may not terminate (if HLT is not executed)
	//    2. Currently no error checking is performed.  Checks for valid program 
	//       and memory addresses and illegal opcodes should be provided.
	public void execute() {
		int x;
		Instruction.OPCODES op;
		boolean halted = false;

		while (!halted) {
			op = program[pc].opcode;
			switch(op) {
				case LDA:
					x = program[pc].operand;
					ac = memory[x];
					pc++;
					break;

				case LDI:
					x = program[pc].operand;
					ac = memory[memory[x]];
					pc++;
					break;

				case STA:
					x = program[pc].operand;
					memory[x] = ac;
					pc++;
					break;

				case ADD:
					x = program[pc].operand;
					ac = ac + memory[x];
					pc++;
					break;

				case SUB:
					x = program[pc].operand;
					ac = ac - memory[x];
					pc++;
					break;

				case JMP:
					x = program[pc].operand;
					pc = x;
					break;

				case JMZ:
					x = program[pc].operand;
					if (ac == 0)
						pc = x;
					else
						pc++;
					break;

				case HLT:
					halted = true;
					break;
			}
		}
	}

	// Dump memory contents
	public void dump() {
		System.out.println("RAM Memory Contents");
		for (int i=1;i<=memorySize;i++)
			System.out.println(i + "   " + memory[i]);
	}
}
