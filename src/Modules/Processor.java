package Modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

public class Processor {
	private final ArrayList<String> opCode = new ArrayList<String>(
			Arrays.asList("add", "sub", "muli", "addi", "bne", "andi", "ori", "j", "sll", "srl", "lw", "sw"));
	private final ArrayList<String> registers = new ArrayList<String>(Arrays.asList("r0", "r1", "r2", "r3", "r4", "r5",
			"r6", "r7", "r8", "r9", "r10", "r11", "r12", "r13", "r14", "r15", "r16", "r17", "r18", "r19", "r20", "r21",
			"r22", "r23", "r24", "r25", "r26", "27", "r28", "r29", "r30", "r31"));
	private String programFilePath = "src/main/resources/program.txt";// fill with file path later
	private int[] memory = new int[2048];
	private int endOfProgram;
	private int[] registerFile = new int[32];
	private int pc = 0;
	private int oldPc = 0;
	private int CLK = 1;
	private int instructionCount = 0;
	private ALU alu = new ALU();
	private ArrayList<Instruction> runningInstructions = new ArrayList<Instruction>();
	private boolean flush = false;
	// pipeline registers
	private int fetchedInstruction;
	private Hashtable<String, Integer> ID = new Hashtable<String, Integer>();
	private Hashtable<String, Integer> EX = new Hashtable<String, Integer>();
	private Hashtable<String, Integer> MEM = new Hashtable<String, Integer>();
	private StringBuffer printer;

	public Processor(String programFilePath) throws IOException {
		this.programFilePath = programFilePath;
		reader();
		dispatcher();
		print();
	}

	public void reader() throws IOException {
		File file = new File(programFilePath);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		int memooryIndex = 0;
		while ((st = br.readLine()) != null) {
			String[] input = st.toLowerCase().split(" ");
			ArrayList<String> rFormat = new ArrayList<String>(Arrays.asList("add", "sub", "sll", "srl"));
			ArrayList<String> iFormat = new ArrayList<String>(
					Arrays.asList("muli", "addi", "bne", "andi", "ori", "lw", "sw"));
			ArrayList<String> jFormat = new ArrayList<String>(Arrays.asList("j"));
			if (rFormat.contains(input[0]))
				memory[memooryIndex++] = rFormatHelper(input);
			else if (iFormat.contains(input[0]))
				memory[memooryIndex++] = iFormatHelper(input);
			else if (jFormat.contains(input[0]))
				memory[memooryIndex++] = jFormatHelper(input);
		}
		br.close();
		endOfProgram = memooryIndex;
	}

	private int rFormatHelper(String[] input) {
		int instruction = 0;
		int opcode = opCode.indexOf(input[0]);
		int reg1 = registers.indexOf(input[1]);
		int reg2 = registers.indexOf(input[2]);
		int reg3 = registers.indexOf(input[3]);
		if (opcode == 0 || opcode == 1)
			instruction = (opcode << 28) | (reg1 << 23) | (reg2 << 18) | (reg3 << 13);
		else {
			int immediate = Integer.parseInt(input[3]) & (int) (Math.pow(2, 13) - 1);
			instruction = (opcode << 28) | (reg1 << 23) | (reg2 << 18) | (immediate);
		}
		return instruction;
	}

	private int iFormatHelper(String[] input) {
		int instruction = 0;
		int opcode = opCode.indexOf(input[0]);
		int reg1 = registers.indexOf(input[1]);
		int reg2 = registers.indexOf(input[2]);
		int imm = Integer.parseInt(input[3]) & (int) (Math.pow(2, 18) - 1);
		instruction = (opcode << 28) | (reg1 << 23) | (reg2 << 18) | (imm);
		return instruction;
	}

	private int jFormatHelper(String[] input) {
		int instruction = 0;
		int opcode = this.opCode.indexOf(input[0]);
		int address = Integer.parseInt(input[1]);
		instruction = (opcode << 28) | (address);
		return instruction;
	}

	public void dispatcher() {
		while (pc < endOfProgram || !runningInstructions.isEmpty()) {
			flush = false;
			printer = new StringBuffer();
			for (Instruction instr : runningInstructions) {
				String stageName = instr.stages.pop();
				runStage(stageName);
				printer.insert(0,
						"\n" + Integer.toBinaryString(instr.instruction) + " is in " + stageName + "Stage" + "\n");
				if (stageName.equals("WB"))
					instr.removeFlag = true;
				if (flush && stageName.equals("EX")) {
					for (Instruction instrTemp : runningInstructions)
						if (instr != instrTemp)
							instrTemp.removeFlag = true;
				}
			}
			if (CLK % 2 != 0 && runningInstructions.size() < 4
					&& ((pc < endOfProgram && !flush) || (flush && oldPc < endOfProgram))) {
				fetch();
				instructionCount++;
				Instruction instruction = new Instruction(fetchedInstruction);
				runningInstructions.add(instruction);
				if (flush)
					instruction.removeFlag = true;
			}
			for (int i = 0; i < runningInstructions.size(); i++)
				if (runningInstructions.get(i).removeFlag)
					runningInstructions.remove(i--);
			printer.insert(0, "Clock Cycle: " + CLK + "\n");
			printer.append("--------------------------------------------------");
			CLK++;
			System.out.println(printer);
		}
	}

	public void runStage(String stageName) {
		switch (stageName) {
		case "ID":
			decode();
			break;
		case "EX":
			execute();
			break;
		case "MEM":
			memory();
			break;
		case "WB":
			writeback();
			break;
		}
	}

	public void fetch() {
		if (flush)
			fetchedInstruction = memory[oldPc];
		else
			fetchedInstruction = memory[pc++];
		if (flush)
			printer.insert(0,
					"\n" + Integer.toBinaryString(fetchedInstruction) + " is in IF Stage\ninput parameters: PC " + oldPc
							+ " ,output parameters: PC " + (oldPc + 1) + "\n");
		else
			printer.insert(0, "\n" + Integer.toBinaryString(fetchedInstruction)
					+ " is in IF Stage\ninput parameters: PC " + (pc - 1) + " ,output parameters: PC " + (pc) + "\n");

		oldPc = pc;
	}

	public void decode() {
		String print = CLK % 2 == 0 ? "First Decode Cycle \n" : "Second Decode Cycle \n";
		print += "input parameters: PC " + oldPc + "\n";
		if (CLK % 2 == 0) {
			print += "no output yet;still decoding \n";
			printer.insert(0, print);
			return;
		}
		int instruction = fetchedInstruction;
		int opcode = (instruction >> 28) & (int) Math.pow(2, 4) - 1; // bits31:28
		int register1 = (instruction >> 23) & (int) Math.pow(2, 5) - 1; // bits27:23
		int register2 = (instruction >> 18) & (int) Math.pow(2, 5) - 1; // bit22:18
		int register3 = (instruction >> 13) & (int) Math.pow(2, 5) - 1; // bits17:13
		int shamt = (instruction) & (int) Math.pow(2, 13) - 1; // bits12:0
		int imm = (instruction) & (int) Math.pow(2, 18) - 1; // bits17:0
		int address = (instruction) & (int) Math.pow(2, 28) - 1; // bits27:0
		int r1Value = registerFile[register1];
		int r2Value = registerFile[register2];
		int r3Value = registerFile[register3];
		String forwarding = "";
		if (EX.get("dest") != null && EX.get("dest") == register2 && register2 != 0 && EX.get("regWrite") != null
				&& EX.get("regWrite") == 1 && EX.get("opCode")!=10) {
			r2Value = EX.get("aluResult");
			forwarding += "Register" + register2
					+ " decoded Vlaue is changed due to forwarding and detected in decode since it's not a LW \n";
		}
		if (EX.get("dest") != null && EX.get("dest") == register3 && register3 != 0 && EX.get("regWrite") != null
				&& EX.get("regWrite") == 1 && EX.get("opCode")!=10) {
			r3Value = EX.get("aluResult");
			forwarding += "Register" + register3
					+ " decoded Vlaue is changed due to forwarding and detected in decode since it's not a LW \n";
		}
		if (EX.get("dest") != null && EX.get("dest") == register1 && (opcode == 4 || opcode == 11) && register1 != 0
				&& EX.get("regWrite") != null && EX.get("regWrite") == 1 && EX.get("opCode")!=10) {
			r1Value = EX.get("aluResult");
			forwarding += "Register" + register1
					+ " decoded Vlaue is changed due to forwarding and detected in decode since it's not a LW \n";
		}
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
			ID.put("srcReg1", register2);
			ID.put("srcReg2", register3);

			if (opcode == 8 || opcode == 9) { // SLL OR SRL
				ID.put("data2", shamt);
				ID.remove("srcReg2");

			}
		} else if (opcode == 2 || opcode == 3 || opcode == 4 || opcode == 5 || opcode == 6 || opcode == 10
				|| opcode == 11) {
			ID.put("data2", imm);
			ID.put("data1", r2Value);
			ID.put("dest", register1); // check law store
			ID.put("regWrite", 1);
			ID.put("srcReg1", register2);

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
				ID.put("srcReg1", register1);
				ID.put("srcReg2", register2);

			}
		} else { // jump
			ID.put("address", address);
			ID.put("data1", 0); // DON'T CARE
			ID.put("dest", 0); // DON'T CARE
			ID.put("data2", 0); // DON'T CARE
		}
		print += "output parameters: \n";
		print += forwarding;
		print += ID.toString() + "\n";
		printer.insert(0, print);

	}

	public void execute() {
		String print = CLK % 2 == 0 ? "First Execute Cycle \n" : "Second Execute Cycle \n";
		print += "input parameters: PC " + ID.get("pc") + ",data 1 " + ID.get("data1") + ",data 2 " + ID.get("data2")
				+ ",branch immediate " + ID.get("immediate") + ",address " + ID.get("address") + "\n";

		int data1 = ID.get("data1");
		int data2 = ID.get("data2");

		if (CLK % 2 != 0 && MEM.get("regWrite") != null && MEM.get("regWrite") == 1) {// handling data hazards
			boolean flag = false;
			if (ID.get("srcReg1") != null && MEM.get("dest") == ID.get("srcReg1") && ID.get("srcReg1") != 0) {
				data1 = registerFile[MEM.get("dest")];
				flag = data1 != ID.get("data1");
			}
			if (ID.get("srcReg2") != null && MEM.get("dest") == ID.get("srcReg2") && ID.get("srcReg2") != 0) {
				data2 = registerFile[MEM.get("dest")];
				flag = flag == false ? data1 != ID.get("data1") : true;
			}
			print = "Second Execute Cycle \n";
			print += flag ? "Change in input due to previous instruction changing value of register\n" : "";
			print += "input parameters: PC " + ID.get("pc") + ",data 1 " + data1 + ",data 2 " + data2
					+ ",branch immediate " + ID.get("immediate") + ",address " + ID.get("address") + "\n";
		}

		if (CLK % 2 == 0) {
			print += "no output yet;still executing \n";
			printer.insert(0, print);
			return;
		}

		alu.calculateResult(ID.get("aluOp"), data1, data2);
		EX.clear();
		EX.put("memRead", ID.get("memRead"));
		EX.put("memToReg", ID.get("memToReg"));
		EX.put("memWrite", ID.get("memWrite"));
		EX.put("dest", ID.get("dest"));
		EX.put("regWrite", ID.get("regWrite"));
		EX.put("aluResult", alu.getAluValue());
		EX.put("opCode", ID.get("opCode"));

		if (ID.get("opCode") == 7) {// jump
			int jumpPc = ID.get("pc") & (int) (Math.pow(2, 4) - 1) << 28;
			this.pc = jumpPc | ID.get("address");
			EX.put("pc", this.pc);
			flush = true;
			ID.clear();
		} else if (ID.get("branch") == 1 && !alu.isZeroValue()) { // branch if NOTTTTT equal
			this.pc += (ID.get("immediate") - 1);

			EX.put("pc", this.pc);
			// flush
			flush = true;
			ID.clear();

		}
		print += "output parameters: \n";
		print += EX.toString() + "\n";
		printer.insert(0, print);
	}

	public void memory() {
		String print = "input parameters: alu result " + EX.get("aluResult") + ",destination register " + EX.get("dest")
				+ "\n";
		MEM.clear();
		if (EX.get("memRead") == 1)
			MEM.put("memResult", memory[EX.get("aluResult")]);
		else
			MEM.put("aluResult", EX.get("aluResult"));
		if (EX.get("memWrite") == 1) {
			memory[EX.get("aluResult")] = registerFile[EX.get("dest")];
			print += "memory address " + EX.get("aluResult") + "new Value: " + registerFile[EX.get("dest")] + "\n";
		}
		MEM.put("memToReg", EX.get("memToReg"));
		MEM.put("regWrite", EX.get("regWrite"));
		MEM.put("dest", EX.get("dest"));
		print += "output parameters: \n";
		print += MEM.toString() + "\n";
		printer.insert(0, print);
	}

	public void writeback() {
		String print = "input parameters: Register Dest:" + MEM.get("regWrite") + " ,alu result " + MEM.get("aluResult")
				+ ",memory read " + MEM.get("memResult") + " ,MemoryToRegister control: " + MEM.get("memToReg")
				+ " , Destination(if any):" + MEM.get("dest") + "\n";
		if (MEM.get("regWrite") == 1) {
			if (MEM.get("memToReg") == 1)
				registerFile[MEM.get("dest")] = MEM.get("memResult");
			else
				registerFile[MEM.get("dest")] = MEM.get("aluResult");
		}
		registerFile[0] = 0;
		if (MEM.get("regWrite") == 1)
			print += "regsiter " + MEM.get("dest") + " new value " + registerFile[MEM.get("dest")] + "\n";
		printer.insert(0, print);
	}

	public void print() {
		printer = new StringBuffer();
		printer.append("\n");
		printer.append("Register File { ");
		for (int i = 0; i < registerFile.length; i++)
			printer.append("Register " + i + ": ").append(registerFile[i]).append(", ");
		printer.deleteCharAt(printer.length() - 1);
		printer.append(" }");
		printer.append("\n");
		printer.append("Memory { ");
		int j = 0;
		for (int memoryWord : memory)
			printer.append("Address " + j++ + ": " + memoryWord + ", ");
		printer.deleteCharAt(printer.length() - 1);
		printer.append(" }");
		printer.append("\n");
		printer.append("Register File In Binary Format { ");
		for (int i = 0; i < registerFile.length; i++)
			printer.append("Register " + i + ": ").append(Integer.toBinaryString(registerFile[i])).append(", ");
		printer.deleteCharAt(printer.length() - 1);
		printer.append(" }");
		printer.append("\n");
		printer.append("Memory In Binary Format { ");
		j = 0;
		for (int memoryWord : memory)
			printer.append("Address " + j++ + ": " + Integer.toBinaryString(memoryWord) + ", ");
		printer.deleteCharAt(printer.length() - 1);
		printer.append(" }");
		System.out.println(printer);
	}

	public static void main(String[] args) throws IOException {
		new Processor("src/resources/program.txt");
//		ArrayList<String> opCode1 = new ArrayList<String>(
//				Arrays.asList("add", "sub", "muli", "addi", "bne", "andi", "ori", "j", "sll", "srl", "lw", "sw"));
//		System.out.println(opCode1.indexOf("addi"));
	}

}
