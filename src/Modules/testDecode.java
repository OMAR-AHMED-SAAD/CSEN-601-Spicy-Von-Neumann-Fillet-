package Modules;

import java.util.Hashtable;

public class testDecode {
	private int[] registerFile = new int[32];
	private int pc = 0;
	private int CLK = 0;
	private String programFilePath = "";// fill with file path later
	private ALU alu = new ALU();
	public void decode(int fetchedInstruction , int opcodeex) {
		Hashtable<String, Integer> ID = new Hashtable<String, Integer>();
		int instruction = fetchedInstruction;
		int opcode = opcodeex;//(instruction >> 28) & (int) Math.pow(2, 4) - 1; // bits31:28
		int register1 = (instruction >> 23) & (int) Math.pow(2, 5) - 1; // bits27:23
		int register2 = (instruction >> 18) & (int) Math.pow(2, 5) - 1; // bit22:18
		int register3 = (instruction >> 13) & (int) Math.pow(2, 5) - 1; // bits17:13
		int shamt = (instruction) & (int) Math.pow(2, 13) - 1; // bits12:0
		int imm = (instruction) & (int) Math.pow(2, 18) - 1; // bits17:0
		int address = (instruction) & (int) Math.pow(2, 28) - 1; // bits27:0
		int r1Value = registerFile[register1];
		int r2Value = registerFile[register2];
		int r3Value = registerFile[register3];
		int signExtend = imm >> 17;
		if (signExtend == 1)
			imm = imm | 0xFFFF0000;
		ID.clear();
		ID.put("pc", pc);
		ID.put("opCode", opcode);
		ID.put("aluOp", alu.getAluOP(opcode));
		ID.put("memWrite", 0);
		ID.put("memRead", 0);
		ID.put("memToReg", 0);
		ID.put("branch", 0);
		ID.put("regWrite", 0);
		if (opcode == 0 || opcode == 1 || opcode == 8 || opcode == 9) { // Rtype
			ID.put("data1", r2Value);
			ID.put("data2", r3Value);
			ID.put("dest", register1);
			ID.put("regWrite", 1);
			if (opcode == 8 || opcode == 9) // SLL OR SRL
				ID.put("data2", shamt);
		} else if (opcode == 2 || opcode == 3 || opcode == 4 || opcode == 5 || opcode == 6 || opcode == 10
				|| opcode == 11) {
			ID.put("data2", imm);
			ID.put("data1", r2Value);
			ID.put("dest", register1); // check law store
			ID.put("regWrite", 1);
			if (opcode == 10) { // LOAD
				ID.put("memToReg", 1);
				ID.put("memRead", 1);
			} else if (opcode == 11) {// STORE
				ID.put("memWrite", 1);
				ID.put("regWrite", 0);
			} else if (opcode == 4) { // Branch
				ID.put("data1", r1Value);
				ID.put("data2", r2Value);
				ID.put("immediate", imm);
				ID.put("branch", 1);
				ID.put("dest", 0);
				ID.put("regWrite", 0);
			}
		} else { // jump
			ID.put("address", address);
			ID.put("data1", 0); // DON'T CARE
			ID.put("dest", 0); // DON'T CARE
			ID.put("data2", 0); // DON'T CARE
		}
		System.out.println(ID);
	}
	public static void main(String[] args) {
		new testDecode().decode(0, 0);
			
	}
}
