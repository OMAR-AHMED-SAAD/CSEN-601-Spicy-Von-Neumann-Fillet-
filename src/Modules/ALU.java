package Modules;

public class ALU {
	private int aluValue;
	private boolean zeroValue;

	public void calculateResult(int control, int data1, int data2) {
		// int res
		switch (control) {
		case 0:
			aluValue = data1 + data2;
			break;// Add
		case 1:
			aluValue = data1 - data2;
			break; // Subtract
		case 2:
			aluValue = data1 * data2;
			break; // MultiplyI
		case 3:
			aluValue = data1 & data2;
			break; // AndI
		case 4:
			aluValue = data1 | data2;
			break; // OrI
		case 5:
			aluValue = data1 << data2;
			break; // ShiftL
		case 6:
			aluValue = data1 >> data2;
			break; // ShiftR
		}
		if (aluValue == 0)
			zeroValue = true;

	}

	public int getAluValue() {
		return aluValue;
	}

	public void setAluValue(int aluValue) {
		this.aluValue = aluValue;
	}

	public boolean isZeroValue() {
		return zeroValue;
	}

	public void setZeroValue(boolean zeroValue) {
		this.zeroValue = zeroValue;
	}

	public int getAluOP(int opcode) {

		switch (opcode) {
		case 0:
			return 0;
		case 1:
			return 1;
		case 2:
			return 2;
		case 3:
			return 0;
		case 4:
			return 1;
		case 5:
			return 3;
		case 6:
			return 4;
		case 7:
			return -1; // jump
		case 8:
			return 5;
		case 9:
			return 6;
		case 10:
			return 0;
		case 11:
			return 0;

		}
		return -1; // jump

	}
}
