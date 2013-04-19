public class Instruction {

	public enum OPCODES { LDA, LDI, STA, 
			      		  STI, ADD, SUB, 
			      		  JMP, JMZ, JMN, HLT };

	public OPCODES opcode;
	public int operand;
}


