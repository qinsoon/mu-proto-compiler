package compiler.phase.mc.linearscan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import uvm.CompiledFunction;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCMemoryOperand;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;
import uvm.mc.linearscan.Interval;
import compiler.UVMCompiler;
import compiler.phase.mc.AbstractMCCompilationPhase;
import compiler.util.OrderedList;

public class SimpleLinearScan extends AbstractMCCompilationPhase {
	// the last few registers are used as scratch registers for spilling values
	private static final int RESERVE_SCRATCH_REGS = 1;	

	public SimpleLinearScan(String name, boolean verbose) {
		super(name, verbose);
	}

	@Override
	protected void visitCompiledFunction(CompiledFunction cf) {
		linearScan(cf);
		replaceRegistersAndInsertSpillingCode(cf);
	}

	StackManager stack;
	CompiledFunction currentCF;
	
	OrderedList<Interval> unhandled;
	LinkedList<Interval> active;
	LinkedList<Interval> inactive;
	LinkedList<Interval> handled;
	
	LinkedList<MCRegister> freeGPRs;
	LinkedList<MCRegister> freeFPRs;
	
	private void linearScan(CompiledFunction cf) {
		currentCF = cf;
		stack = new StackManager(cf);
		
		unhandled = new OrderedList<Interval>(cf.intervals.values(), new Comparator<Interval>() {
            @Override
            public int compare(Interval o1, Interval o2) {
                if (o1.getBegin() < o2.getBegin())
                    return -1;
                else if (o1.getBegin() == o2.getBegin())
                    return 0;
                else return 1;
            }
        });
		active 		= new LinkedList<Interval>();
		inactive 	= new LinkedList<Interval>();
		handled 	= new LinkedList<Interval>();
		
		freeGPRs	= new LinkedList<MCRegister>();
		freeFPRs	= new LinkedList<MCRegister>();
		initFreeRegisters();
		
		for (int i = UVMCompiler.MCDriver.getNumberOfGPR() - RESERVE_SCRATCH_REGS; i < UVMCompiler.MCDriver.getNumberOfGPR(); i++) {
			verboseln("Reserved " + UVMCompiler.MCDriver.getGPRName(i) + " as scratch GPR registers");
		}
		for (int i = UVMCompiler.MCDriver.getNumberOfFPR() - RESERVE_SCRATCH_REGS; i < UVMCompiler.MCDriver.getNumberOfFPR(); i++) {
			verboseln("Reserved " + UVMCompiler.MCDriver.getFPRName(i) + " as scratch FPR registers");
		}
		
		while (!unhandled.isEmpty()) {			
			Interval cur = unhandled.poll();
			
			verboseln("");
			verboseln("Polling " + cur.getOrig().prettyPrintREPOnly());
			
			verboseFreeRegisters();
			
			checkAcitveIntervals(cur.getBegin());
			checkInactiveIntervals(cur.getBegin());
			
			LinkedList<MCRegister> f = collectAvailableRegisters(cur);
			
			if (f.isEmpty()) {
				assignMemLoc(cur);
			} else {
				assignPhysReg(cur, f);
			}
		}
		
		cf.stackManager = stack;
	}	

	private void checkAcitveIntervals(int pos) {
		for (int i = 0; i < active.size(); ) {
			Interval it = active.get(i);
			verbose("check active:" + it.getOrig().prettyPrintREPOnly());
			
			if (it.getEnd() < pos) {
				verbose(" -> handled, ");
				active.remove(i);
				handled.add(it);
				returnPhysicalReg(it);
				
				verboseln(it.getPhysicalReg().prettyPrint() + " freed");
				verboseFreeRegisters();
				
				continue;
			} else if (!it.isLiveAt(pos)) {
				verbose(" -> inactive，");
				active.remove(i);
				inactive.add(it);
				returnPhysicalReg(it);
				
				verboseln(it.getPhysicalReg().prettyPrint() + " freed");
				verboseFreeRegisters();
				
				continue;
			}
			
			verboseln("");
			i++;
		}
	}

	private void checkInactiveIntervals(int pos) {
		for (int i = 0; i < inactive.size(); ) {
			Interval it = inactive.get(i);
			verbose("check inactive:" + it.getOrig().prettyPrintREPOnly());
			
			if (it.getEnd() < pos) {
				verboseln(" -> handled， ");
				inactive.remove(i);
				handled.add(it);
				
				continue;
			} else if (it.isLiveAt(pos)) {
				verbose(" -> active, ");
				verboseln(it.getPhysicalReg().prettyPrint() + " used");
				inactive.remove(i);
				active.add(it);
				
				removePhysicalRegFromFree(it.getPhysicalReg());
				verboseFreeRegisters();
				
				continue;
			}
			
			verboseln("");
			i++;
		}
	}
	
	private void verboseFreeRegisters() {
		verbose("free GPR: {");
		for (MCRegister reg : freeGPRs) {
			verbose(reg.prettyPrint() + ",");
		}
		verboseln("}");
		
		verbose("free FPR: {");
		for (MCRegister reg : freeFPRs) {
			verbose(reg.prettyPrint() + ",");
		}
		verboseln("}");
	}
	
	private LinkedList<MCRegister> collectAvailableRegisters(Interval cur) {
		LinkedList<MCRegister> ret = null;
		
		if (cur.getDataType() == MCRegister.DATA_GPR) {
			ret = new LinkedList<MCRegister>(freeGPRs);
		} else if (cur.getDataType() == MCRegister.DATA_DP) {
			ret = new LinkedList<MCRegister>(freeFPRs);
		} else {
			UVMCompiler.error("unimplemented data type when collecting available registers");
		}
		
		for (Interval i : inactive) {
			if (i.nextIntersectionWith(cur) != -1)
				ret.remove(i.getPhysicalReg());
		}
		
		for (Interval i : unhandled) {
			if (i.isFixed() && i.nextIntersectionWith(cur) != -1)
				ret.remove(i.getPhysicalReg());
		}
		
		verboseln("Getting available registers:");
		verbose("{");
		for (MCRegister reg : ret)
			verbose(reg.prettyPrint() + ",");
		verboseln("}");
		
		return ret;
	}

	private void initFreeRegisters() {
		// init GPRs
		for (int i = 0; i < UVMCompiler.MCDriver.getNumberOfGPR() - RESERVE_SCRATCH_REGS; i++) {
			MCRegister reg = currentCF.findOrCreateRegister(UVMCompiler.MCDriver.getGPRName(i), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
			freeGPRs.add(reg);
		}
		
		// init FPRs
		for (int i = 0; i < UVMCompiler.MCDriver.getNumberOfFPR() - RESERVE_SCRATCH_REGS; i++) {
			MCRegister reg = currentCF.findOrCreateRegister(UVMCompiler.MCDriver.getFPRName(i), MCRegister.MACHINE_REG, MCRegister.DATA_DP);
			freeFPRs.add(reg);
		}
	}
	
	private void returnPhysicalReg(Interval it) {
		if (it.getPhysicalReg() == null)
			return;
		
		if (it.getDataType() == MCRegister.DATA_GPR)
			freeGPRs.add(it.getPhysicalReg());
		else if (it.getDataType() == MCRegister.DATA_DP)
			freeFPRs.add(it.getPhysicalReg());
		else {
			UVMCompiler.error("unimplemented data type when returning physical registers");
		}
	}
	
	private void removePhysicalRegFromFree(MCRegister reg) {
		if (reg.getDataType() == MCRegister.DATA_GPR) 
			freeGPRs.remove(reg);
		else if (reg.getDataType() == MCRegister.DATA_DP)
			freeFPRs.remove(reg);
		else {
			UVMCompiler.error("unimplemented data type when removing physical register from free set");
		}
	}

	private void assignPhysReg(Interval cur, LinkedList<MCRegister> f) {
		// if cur is fixed with special registers		
		if (cur.isFixed() && RegisterCoalescing.isNonGeneralPurposeMachineReg(cur.getPhysicalReg())) {
			return;
		}
		
		MCRegister candidate = null;
		
		if (cur.isFixed()) {
			if (f.contains(cur.getPhysicalReg()))
				candidate = cur.getPhysicalReg();
			else UVMCompiler.error("Trying to assign a physical reg to fixed interval " + cur.getPhysicalReg().prettyPrint() + " but it is not available");
		} else {
			candidate = f.poll();
			cur.setPhysicalReg(candidate);
		}
		
		if (cur.getDataType() == MCRegister.DATA_GPR)
			freeGPRs.remove(cur.getPhysicalReg());
		else if (cur.getDataType() == MCRegister.DATA_DP)
			freeFPRs.remove(cur.getPhysicalReg());
		else {
			UVMCompiler.error("unimplemented data type in assignPhysReg()");
		}
		
		active.add(cur);
		verboseln("Assign " + candidate.prettyPrintREPOnly() + " to " + cur.getOrig().prettyPrintREPOnly());
	}
	
	HashMap<Interval, Integer> intervalWeight = new HashMap<Interval, Integer>();

	private void assignMemLoc(Interval cur) {
		verboseln("Finding evacuating candidate for " + cur.getOrig().prettyPrintREPOnly());
		
		Set<MCRegister> allRegisters = getAllPhysicalRegisters(cur.getDataType());
		HashMap<MCRegister, Integer> weight = new HashMap<MCRegister, Integer>();
		
		// for all registers r do w[r]<-0
		for (MCRegister reg : allRegisters) {
			weight.put(reg, 0);
		}
		
		// we need to init interval weights
		if (intervalWeight.isEmpty())
			initIntervalWeight();		
		
		// for all intervals in active, inactive and (fixed) unhandled do
		//   if i overlaps cur then w[i.reg]<-w[i.reg]+i.weight
		for (Interval i : active) {
			if (i.nextIntersectionWith(cur) != -1)
				increaseRegWeight(weight, i.getPhysicalReg(), i);
		}
		
		for (Interval i : inactive) {
			if (i.nextIntersectionWith(cur) != -1)
				increaseRegWeight(weight, i.getPhysicalReg(), i);
		}
		
		for (Interval i : unhandled) {
			if (i.isFixed() && i.nextIntersectionWith(cur) != -1)
				increaseRegWeight(weight, i.getPhysicalReg(), i);
		}
		
		verboseln("Register weight:");
		for (Entry<MCRegister, Integer> e : weight.entrySet()) {
			verboseln(e.getKey().prettyPrintREPOnly() + "=" + e.getValue());
		}
		
		// find r such that w[r] is minimum
		Integer minimumWeight = Integer.MAX_VALUE;
		MCRegister candidateReg = null;
		for (MCRegister reg : weight.keySet()) {
			Integer i = weight.get(reg);
			if (i < minimumWeight) {
				minimumWeight = i;
				candidateReg = reg;
			}
		}
		
		verboseln("Picking candidate:" + candidateReg.prettyPrintREPOnly());
		
		// if cur.weight < w[r] then
		//   assign a memory location to cur and move cur to handled
		if (intervalWeight.get(cur) < minimumWeight) {
			verbose("Spill current: " + cur.getOrig().prettyPrintREPOnly());
			
			MCMemoryOperand mem = stack.spillInterval(cur);
			cur.setSpill(mem);
			
			verboseln(" to " + mem.prettyPrint());
			
			currentCF.setReserveScratchRegs(1);
			
			handled.add(cur);
		} else {
			// move all active or inactive intervals to which r was assigned to handled
			// assign mem locations to them
			verboseln("Assign " + candidateReg.prettyPrintREPOnly() + " to current:" + cur.getOrig().prettyPrintREPOnly());
			verboseln("Evacuating any active/inactive interval that is using " + candidateReg.prettyPrintREPOnly());
			
			for (int i = 0; i < active.size(); ) {
				Interval it = active.get(i);
				if (it.getPhysicalReg().equals(candidateReg)) {
					verbose("Spill active:" + it.getOrig().prettyPrintREPOnly());
					
					active.remove(i);
					
					MCMemoryOperand mem = stack.spillInterval(it);
					it.setSpill(mem);
					handled.add(it);
					
					verboseln(" to " + mem.prettyPrint());
					
					currentCF.setReserveScratchRegs(1);
					
					continue;
				}
				
				i++;
			}
			for (int i = 0; i < inactive.size(); ) {
				Interval it = inactive.get(i);
				if (it.getPhysicalReg().equals(candidateReg)) {
					verbose("Spill inactive:" + it.getOrig().prettyPrintREPOnly());
					
					inactive.remove(i);
					
					MCMemoryOperand mem = stack.spillInterval(it);
					it.setSpill(mem);
					handled.add(it);
					
					verboseln(" to " + mem.prettyPrint());
					
					currentCF.setReserveScratchRegs(1);
					
					continue;
				}
				
				i++;
			}
			
			// assert
			if (cur.isFixed() && !cur.getPhysicalReg().equals(candidateReg)) {
				UVMCompiler.error("Trying to reassign another register " + candidateReg.prettyPrintREPOnly() + " to a fixed interval " + cur.getOrig().prettyPrintREPOnly());
			}
			
			// cur.reg <- r
			cur.setPhysicalReg(candidateReg);
			active.add(cur);
			
			if (cur.getDataType() == MCRegister.DATA_GPR)
				freeGPRs.remove(candidateReg);
			else if (cur.getDataType() == MCRegister.DATA_DP)
				freeFPRs.remove(candidateReg);
			else {
				UVMCompiler.error("unimplemented data type when evacuating and re-assigning physical register");
			}
		}
	}
	
	private void increaseRegWeight(HashMap<MCRegister, Integer> weight, MCRegister reg, Interval i) {
		verboseln("increase reg weight for " + reg.prettyPrint() + " " + reg.hashCode() + " of interval " + i.hashCode());
		
		Integer w = null;
		if (weight.containsKey(reg))
			w = weight.get(reg);
		else w = 0;
		
		if (w == Integer.MAX_VALUE || intervalWeight.get(i) == Integer.MAX_VALUE)
			w = Integer.MAX_VALUE;
		else w += intervalWeight.get(i);
		weight.put(reg, w);
	}
	
	private void initIntervalWeight() {		
		for (AbstractMachineCode mc : currentCF.mc) {
			if (mc.isPhi())
				continue;
			
			// ops
			for (int i = 0; i < mc.getNumberOfOperands(); i++) {
				MCOperand op = mc.getOperand(i);
				if (op instanceof MCRegister) {
					MCRegister reg = (MCRegister) mc.getOperand(i);
					increaseIntervalWeightForReg(reg);
				}
			}
			
			// define
			MCRegister def = mc.getDefineAsReg();
			if (def != null)
				increaseIntervalWeightForReg(def);
			
			// implicit uses
			for (int i = 0; i < mc.getNumberOfImplicitUses(); i++) {
				MCRegister reg = (MCRegister) mc.getImplicitUse(i);
				increaseIntervalWeightForReg(reg);
			}
			
			// implicit defs
			for (int i = 0; i < mc.getNumberOfImplicitDefines(); i++) {
				MCRegister reg = (MCRegister) mc.getImplicitDefine(i);
				increaseIntervalWeightForReg(reg);
			}
		}
		
		verboseln("--- Interval Weight --- initialized as:");
		for (Entry<Interval, Integer> e : intervalWeight.entrySet()) {
			verbose("Interval " + e.getKey().hashCode() + " with reg ");
			verbose(e.getKey().getOrig().prettyPrint());
			verbose("(" + e.getKey().getOrig().hashCode() + ")");
			verbose("=");
			verboseln(e.getValue());
		}
	}
	
	private void increaseIntervalWeightForReg(MCRegister reg) {
		Interval i = currentCF.intervals.get(reg.REP());
		
		if (i == null)
			return;
		
		Integer weight = null;
		if (intervalWeight.containsKey(i))
			weight = intervalWeight.get(i);
		else weight = new Integer(0);
		
		if (i.isFixed())
			weight = Integer.MAX_VALUE;
		else weight += 1;
		intervalWeight.put(i, weight);
	}

	private Set<MCRegister> getAllPhysicalRegisters(int dataType) {
        HashSet<MCRegister> physRegisters = new HashSet<MCRegister>();
        
        if (dataType == MCRegister.DATA_GPR) {
            // init physRegisters to all GPRs
            for (int i = 0; i < UVMCompiler.MCDriver.getNumberOfGPR() - RESERVE_SCRATCH_REGS; i++) {
                MCRegister reg = currentCF.findOrCreateRegister(UVMCompiler.MCDriver.getGPRName(i), MCRegister.MACHINE_REG, dataType);
                physRegisters.add(reg);
            }
        } else if (dataType == MCRegister.DATA_DP) {
            // init physRegisters to double-precision FPRs
            for (int i = 0; i < UVMCompiler.MCDriver.getNumberOfFPR() - RESERVE_SCRATCH_REGS; i++) {
                MCRegister reg = currentCF.findOrCreateRegister(UVMCompiler.MCDriver.getFPRName(i), MCRegister.MACHINE_REG, dataType);
                physRegisters.add(reg);
            }
        } else if (dataType == MCRegister.DATA_SP) {
            // init physRegisters to single-precision FPRs
            UVMCompiler.error("unimplemented reg alloc for sp FPR");
        } else {
            UVMCompiler.error("unexpected interval data type: MEM");
        }
        
        return physRegisters;
    }

	/**
	 * https://www.usenix.org/legacy/event/jvm02/full_papers/alpern/alpern_html/node15.html
	 * @param cf
	 */
	private void replaceRegistersAndInsertSpillingCode(CompiledFunction cf) {
		verboseln("--- inserting spilling code and replace op for " + cf.getOriginFunction().getName() + " ---");
		
		MCRegister scratchGPR = cf.findOrCreateRegister(
				UVMCompiler.MCDriver.getGPRName(UVMCompiler.MCDriver.getNumberOfGPR() - RESERVE_SCRATCH_REGS), MCRegister.MACHINE_REG, MCRegister.DATA_GPR);
		MCRegister scratchFPR = cf.findOrCreateRegister(
				UVMCompiler.MCDriver.getFPRName(UVMCompiler.MCDriver.getNumberOfFPR() - RESERVE_SCRATCH_REGS), MCRegister.MACHINE_REG, MCRegister.DATA_DP);				
		Interval cachedSpilledGPR = null;
		Interval cachedSpilledFPR = null;

		for (MCBasicBlock bb : cf.BBs) {
			ArrayList<AbstractMachineCode> newMC = new ArrayList<AbstractMachineCode>();
			
			cachedSpilledGPR = null;
			cachedSpilledFPR = null;
			
			for (AbstractMachineCode mc : bb.getMC()) {
				verboseln("checking mc: " + mc.prettyPrintREPOnly());
							
				// if this mc leaves a basic block (call/jump/fall-through/exception)
				// store back the cached value
				if (mc.isBranchingCode() || mc.isCall() || mc.isRet()) {
					if (cachedSpilledGPR != null) {
						AbstractMachineCode store = UVMCompiler.MCDriver.genMove(cachedSpilledGPR.getSpill(), scratchGPR);
						newMC.add(store);
						verboseln("  inserting store before jump: " + store.prettyPrintREPOnly());
					}
					if (cachedSpilledFPR != null) {
						AbstractMachineCode store = UVMCompiler.MCDriver.genDPMove(cachedSpilledFPR.getSpill(), scratchFPR);
						newMC.add(store);
						verboseln("  inserting store before jump: " + store.prettyPrintREPOnly());
					}
				}
				
				// precheck how many spilled temporaries in this mc
				int spilledTemps = 0;
				int spilledTempsThatNeedsReg = 0;
				for (int i = 0; i < mc.getNumberOfOperands(); i++) {
					MCOperand op = mc.getOperand(i);
					if (op instanceof MCRegister) {
						MCRegister reg = (MCRegister) op;
						
						if (reg.getType() == MCRegister.MACHINE_REG)
							continue;
						
						Interval interval = cf.intervals.get(reg.REP());
						
						if (interval.getSpill() != null)
							spilledTemps ++;
						if (interval.isRegOnlyUseAt(mc.sequence))
							spilledTempsThatNeedsReg++;
					}
				}
				
				boolean useMemOpWheneverPossible = (spilledTempsThatNeedsReg <= RESERVE_SCRATCH_REGS) && spilledTemps > spilledTempsThatNeedsReg;
				
				// check all the temporaries used
				boolean usingScratchRegisterInThisMC = false;
				
				for (int i = 0; i < mc.getNumberOfOperands(); i++) {
					MCOperand op = mc.getOperand(i);
					verboseln("  checking op: " + op.prettyPrint());
					
					if (op instanceof MCRegister && ((MCRegister) op).getType() == MCRegister.MACHINE_REG)
						continue;
					
					if (op instanceof MCRegister) {
						MCRegister reg = (MCRegister) mc.getOperand(i);
						Interval it = cf.intervals.get(reg.REP());
						
						boolean allowMemOp = !it.isRegOnlyUseAt(mc.sequence);
						
						// if this temporary gets a physical reg, simply replace
						if (it.getSpill() == null) {
							mc.setOperand(i, it.getPhysicalReg());
						} 
						// if this temporary is already in a scratch register, simply replace
						else if (it == cachedSpilledGPR) {
							if (useMemOpWheneverPossible && allowMemOp) {
								mc.setOperand(i, it.getSpill());
							} else {
								usingScratchRegisterInThisMC = true;
								mc.setOperand(i, scratchGPR);
								verboseln("  *** using scratch register! " + scratchGPR.prettyPrint());
							}
						} else if (it == cachedSpilledFPR) {
							if (useMemOpWheneverPossible && allowMemOp) {
								mc.setOperand(i, it.getSpill());
							} else {
								usingScratchRegisterInThisMC = true;
								mc.setOperand(i, scratchFPR);
								verboseln("  *** using scratch register! " + scratchFPR.prettyPrint());
							}
						}
						// otherwise, we either use the mem loc if allowed, or load the temporary into scratch registers
						else {
							// if this mc allows mem locations, simply replace
							if (allowMemOp) {
								mc.setOperand(i, it.getSpill());
							} else {
								usingScratchRegisterInThisMC = true;								
								
								if (it.getDataType() == MCRegister.DATA_GPR) {
									verboseln("  *** using scratch register! " + scratchGPR.prettyPrint());
									
									// store current scratch GPR
									if (cachedSpilledGPR != null) {
										AbstractMachineCode store = UVMCompiler.MCDriver.genMove(cachedSpilledGPR.getSpill(), scratchGPR);
										verboseln("  insert store:" + store.prettyPrintREPOnly());
										newMC.add(store);
									}
									
									// load
									AbstractMachineCode load  = UVMCompiler.MCDriver.genMove(scratchGPR, it.getSpill());									
									verboseln("  insert load :" + load.prettyPrintREPOnly());									
									newMC.add(load);
									
									cachedSpilledGPR = it;
									
									mc.setOperand(i, scratchGPR);
								} else if (it.getDataType() == MCRegister.DATA_DP) {
									verboseln("  *** using scratch register! " + scratchFPR.prettyPrint());
									
									// store current scratch FPR
									if (cachedSpilledFPR != null) {
										AbstractMachineCode store = UVMCompiler.MCDriver.genMove(cachedSpilledFPR.getSpill(), scratchFPR);
										verboseln("  insert store:" + store.prettyPrintREPOnly());
										newMC.add(store);
									}
									
									// load
									AbstractMachineCode load  = UVMCompiler.MCDriver.genMove(scratchFPR, it.getSpill());
									verboseln("  insert load :" + load.prettyPrintREPOnly());
									newMC.add(load);
									
									cachedSpilledFPR = it;
									
									mc.setOperand(i, scratchFPR);
								} else {
									UVMCompiler.error("unimplemented data type in inserting spilling code");
								}
							}
						}
					} // end of (if op instanceof MCRegister)
					else if (op instanceof MCMemoryOperand) {
						MCMemoryOperand memOp = (MCMemoryOperand) op;
						
						if (memOp.getBase() != null) {
							MCRegister base = memOp.getBase();
							
							if (base.getREPType() != MCRegister.MACHINE_REG) {
								Interval it = cf.intervals.get(base.REP());
								if (it.getPhysicalReg() != null) {
									memOp.setBase(it.getPhysicalReg());
								} else {
									System.out.println(base.prettyPrintREPOnly() + " in " + memOp.prettyPrint() + " hasnt been assigned a phys reg");
									UVMCompiler.error("Error in replacing register");
								}
							}
						}
					}
				} // end of checking all operands
				
				// check define
				MCRegister def = mc.getDefineAsReg();
				if (def != null && def.getType() != MCRegister.MACHINE_REG) {
					verboseln("  checking def:" + def.prettyPrint());
					
					Interval it = cf.intervals.get(def.REP());
					
					if (it != null) {
						boolean allowMemOp = !it.isRegOnlyUseAt(mc.sequence + 1);
						
						// if this temporary gets a physical reg, simply replace
						if (it.getSpill() == null) {
							mc.setDefine(it.getPhysicalReg());
						} else if (it.getSpill() != null) {
							// if this mc allows mem locations, simply replace
							if (allowMemOp) {
								mc.setDefine(it.getSpill());
							} else {
								
								// if this temporary already in a scratch reg, simply use it
								if (it.getDataType() == MCRegister.DATA_GPR) {
									verboseln("  *** using scratch register! " + scratchGPR.prettyPrint());
									
									if (cachedSpilledGPR == it)
										mc.setDefine(scratchGPR);
									else {
										if (usingScratchRegisterInThisMC) {
											System.out.println(mc.prettyPrintOneline());
											System.out.println("already used scratch register for " + cachedSpilledGPR.getOrig().prettyPrintREPOnly());
											System.out.println("but still need the scratch register for " + it.getOrig().prettyPrintREPOnly());
											UVMCompiler.error("already using the scratch register in this mc, increase number of reserved registers");
										}
										
										// store current scratch GPR
										if (cachedSpilledGPR != null) {
											AbstractMachineCode store = UVMCompiler.MCDriver.genMove(cachedSpilledGPR.getSpill(), scratchGPR);
											verboseln("  insert store:" + store.prettyPrintREPOnly());
											newMC.add(store);
										}
										
										cachedSpilledGPR = it;									
										mc.setDefine(scratchGPR);
									}
								} else if (it.getDataType() == MCRegister.DATA_DP) {
									verboseln("  *** using scratch register! " + scratchFPR.prettyPrint());
									
									if (cachedSpilledFPR == it)
										mc.setDefine(scratchFPR);
									else {
										if (usingScratchRegisterInThisMC) {
											System.out.println(mc.prettyPrintOneline());
											System.out.println("already used scratch register for " + cachedSpilledFPR.getOrig().prettyPrintREPOnly());
											System.out.println("but still need the scratch register for " + it.getOrig().prettyPrintREPOnly());
											UVMCompiler.error("already using the scratch register in this mc, increase number of reserved registers");
										}
										
										if (cachedSpilledFPR != null) {
											AbstractMachineCode store = UVMCompiler.MCDriver.genMove(cachedSpilledFPR.getSpill(), scratchFPR);
											verboseln("  insert store:" + store.prettyPrintREPOnly());
											newMC.add(store);
										}
										
										cachedSpilledFPR = it;
										mc.setDefine(scratchFPR);
									}
								} else {
									UVMCompiler.error("unimplemented data type in inserting spilling code");
								}
							}
						} // end of checking define
					}
					

				}
				
				verboseln("  => " + mc.prettyPrintOneline());
				newMC.add(mc);
			}
			
			bb.setMC(newMC);
		}	

	}
}
