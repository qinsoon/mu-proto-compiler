package uvm.inst;

import uvm.Instruction;

/**
 * I use this to mark why each machine code is inserted for implementing calling conventions
 * only mark what we are interested in. 
 *
 */
public class InstPseudoCCInstruction extends Instruction {
	public enum CCInstType {
		CALLER_SAVE_REGISTERS, CALLEE_SAVE_REGISTERS,
		CALLER_RESTORE_REGISTERS, CALLEE_RESTORE_REGISTERS,
		CALLER_PREPARE_PARAM_REG, CALLER_PREPARE_PARAM_STACK, 
	}
	
	public static final InstPseudoCCInstruction CALLER_SAVE_REGISTERS = new InstPseudoCCInstruction(CCInstType.CALLER_SAVE_REGISTERS);
	public static final InstPseudoCCInstruction CALLEE_SAVE_REGISTERS = new InstPseudoCCInstruction(CCInstType.CALLEE_SAVE_REGISTERS);
	public static final InstPseudoCCInstruction CALLER_RESTORE_REGISTERS = new InstPseudoCCInstruction(CCInstType.CALLER_RESTORE_REGISTERS);
	public static final InstPseudoCCInstruction CALLEE_RESTORE_REGISTERS = new InstPseudoCCInstruction(CCInstType.CALLEE_RESTORE_REGISTERS);
	public static final InstPseudoCCInstruction CALLER_PREPARE_PARAM_REG = new InstPseudoCCInstruction(CCInstType.CALLER_PREPARE_PARAM_REG);
	public static final InstPseudoCCInstruction CALLER_PREPARE_PARAM_STACK = new InstPseudoCCInstruction(CCInstType.CALLER_PREPARE_PARAM_STACK);
	
	CCInstType type;
	
	private InstPseudoCCInstruction(CCInstType type) {
		this.type = type;
	}
	
	public CCInstType getType() {
		return type;
	}

	@Override
	public String prettyPrint() {
		return type.toString().toUpperCase();
	}

}
