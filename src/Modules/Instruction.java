package Modules;

import java.util.Stack;

public class Instruction {
	int instruction;
	Stack<String> stages = new Stack<String>();
boolean removeFlag=false;
	public Instruction(int instruction) {
		this.instruction = instruction;
		stages.push("WB");
		stages.push("MEM");
		stages.push("EX");
		stages.push("EX");
		stages.push("ID");
		stages.push("ID");
	}
	@Override
	public String toString() {
		return "Instruction [instruction=" + instruction + ", stages=" + stages + ", removeFlag=" + removeFlag + "]";
	}
}
