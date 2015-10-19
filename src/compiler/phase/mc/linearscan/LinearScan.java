package compiler.phase.mc.linearscan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uvm.CompiledFunction;
import uvm.StackManager;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCLabel;
import uvm.mc.MCMemoryOperand;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;
import uvm.mc.linearscan.Interval;
import compiler.UVMCompiler;
import compiler.phase.mc.AbstractMCCompilationPhase;
import compiler.util.MultiValueMap;
import compiler.util.OrderedList;
import compiler.util.Pair;

@Deprecated
public class LinearScan extends AbstractMCCompilationPhase {

    public LinearScan(String name, boolean verbose) {
        super(name, verbose);
    }
    
    OrderedList<Interval> unhandled = null;
    LinkedList<Interval>  active    = null;
    LinkedList<Interval>  inactive  = null;
    LinkedList<Interval>  handled   = null;
    
    CompiledFunction currentCF;
    StackManager stack;
    HashMap<Integer, List<Pair<Interval, Interval>>> moves;

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
    	verboseln("=== linear scan on " + cf.getOriginFunction().getName() + " === ");
    	for (MCBasicBlock bb : cf.BBs)
    		verboseln(bb.prettyPrintREPOnly());
    	
        linearScan(cf);
        resolution(cf);
        
        verboseln("=== Register Mapping table ===");
        for (MCRegister vReg : cf.intervals.keySet()) {
        	verboseln(vReg.prettyPrintREPOnly() + ":");
        	Interval cur = cf.intervals.get(vReg);
        	verboseln(cur.prettyPrint());
        	verboseln("");
        }
    }
    
    private void resolution(CompiledFunction cf) {
    	verboseln("--- resolution for " + cf.getOriginFunction().getName() + " ---");
    	// build live in and out
    	// i.e. for every BB, what is the first and last interval for virtual registers
    	HashMap<MCBasicBlock, HashMap<MCRegister, Pair<Interval, Interval>>> map = 
    			new HashMap<MCBasicBlock, HashMap<MCRegister, Pair<Interval, Interval>>>();
    	
    	for (MCBasicBlock bb : cf.BBs) {
    		int start = bb.getFirst().sequence;
    		int end = bb.getLast().sequence;
    		
    		HashMap<MCRegister, Pair<Interval, Interval>> liveInAndOut
    			= cf.getLiveInAndOutBetween(start, end);
    		
    		if (liveInAndOut != null)
    			map.put(bb, liveInAndOut);
    	}
    	
    	// find out back edges
    	for (MCBasicBlock bb : cf.BBs) {
    		List<MCBasicBlock> backedgeList = bb.getBackEdges();
    		int insertPoint = bb.getLast().sequence;
    		List<Pair<Interval, Interval>> inserts = moves.get(insertPoint);
    		
    		for (MCBasicBlock target : backedgeList) {
    			verboseln("for backedge " + bb.getName() + "->" + target.getName());
    			verboseln(" from " + bb.getLast().sequence + " jumping to " + target.getFirst().sequence);
    			
    			UVMCompiler._assert(bb.getSuccessor().size() == 1, "a BB has backedge, but has more than one successors, cant insert reg transfer moves");
    			HashMap<MCRegister, Pair<Interval, Interval>> currentLiveOut = map.get(bb);
    			HashMap<MCRegister, Pair<Interval, Interval>> targetLiveIn = map.get(target);

    			for(MCRegister vReg : currentLiveOut.keySet()) {
    				if (!targetLiveIn.containsKey(vReg))
    					continue;

    				Interval liveOutInterval = currentLiveOut.get(vReg).getSecond();    				
    				Interval liveInInterval = targetLiveIn.get(vReg).getFirst();
    				
    				if (liveOutInterval != null && liveInInterval != null) {
	    				if (inserts == null)
	    					inserts = new ArrayList<Pair<Interval, Interval>>();
	    				
	    				verboseln(" " + liveOutInterval.prettyPrint() + " -> " + liveInInterval.prettyPrint());
	    				inserts.add(new Pair<Interval, Interval>(liveOutInterval, liveInInterval));
    				}
    			}
    		}
    		
    		if (inserts != null)
    			moves.put(insertPoint, inserts);
    	}
    	
    	cf.regMoveCodeInsertion = moves;
    	cf.stackManager = stack;
    }

	private void linearScan(CompiledFunction cf) {
		verboseln("----- linear scan for " + cf.getOriginFunction().getName() + " -----");
        
        verboseln("--- all intervals ---");
        for (Interval i : cf.intervals.values()) {
        	verboseln(i.prettyPrint());
        }
        verboseln("");
        
        currentCF = cf;
        stack = new StackManager(cf);
        moves = new HashMap<Integer, List<Pair<Interval, Interval>>>();
        
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
        
        active   = new LinkedList<Interval>();
        inactive = new LinkedList<Interval>();
        handled  = new LinkedList<Interval>();
        
        while (!unhandled.isEmpty()) {
            Interval current = unhandled.poll();
            int position = current.getBegin();
            verboseln("Handling: " + current.getOrig().prettyPrint() + " begins at " + position + "(ends at " + current.getEnd() + ")");
            verboseln(current.prettyPrint());
            
            verboseln("Expiring intervals: ");
            
            // check for intervals in active that are handled or inactive
            for (int i = 0; i < active.size(); ) {
                Interval it = active.get(i);
                
                // if it ends before position
                // move it from active to handled
                if (it.getEnd() < position) {
                    active.remove(i);
                    handled.add(it);
                    verboseln(it.getOrig().prettyPrint() + "(in " + it.getPhysicalReg().prettyPrint() + "): active -> handled");
                    
                    continue;
                } 
                // if it does not cover position
                // move it from active to inactive
                else if (!it.isLiveAt(position)) {
                    active.remove(i);
                    verboseln(it.getOrig().prettyPrint() + "(in " + it.getPhysicalReg().prettyPrint() + "): active -> inactive");
                    checkAndAddToInactive(it);                    
                    
                    continue;
                }
                
                i++;
            }
            
            // check for intervals in inactive that are handled or active
            for (int i = 0; i < inactive.size(); ) {
                Interval it = inactive.get(i);
                // if it ends before position
                // move it from inactive to handled
                if (it.getEnd() < position) {
                    inactive.remove(i);
                    handled.add(it);
                    verboseln(it.getOrig().prettyPrint() + "(in " + it.getPhysicalReg().prettyPrint() + "): inactive -> handled");
                    
                    continue;
                }
                // if it covers position
                // move it from inactive to active
                else if (it.isLiveAt(position)) {
                    inactive.remove(i);
                    verboseln(it.getOrig().prettyPrint() + "(in " + it.getPhysicalReg().prettyPrint() + "): inactive -> active");
                    checkAndAddToActive(it);
                    
                    continue;
                }
                
                i++;
            }
            
            verboseln("done...");
            
            // if current is a fixed interval, then we dont need to allocate for it
            if (current.isFixed()) {
                verboseln("fixed interval");
                if (current.isLiveAt(position)) {
                	// if any other active is using this reg, have to split it
                	for (int i = 0; i < active.size(); i++) {
                		Interval it = active.get(i);
                		if (it.getPhysicalReg() == current.getPhysicalReg()) {
                			active.remove(i);
                    		Interval afterSplit = it.splitAt(position);
                    		unhandled.add(afterSplit);
                    		informSplitActiveInterval(it, afterSplit);
                    		verboseln("spilting " + it.getOrig().getName() + " at " + position);
                    		break;
                    	}
                	}
                    
                    verboseln("add to active");
                    checkAndAddToActive(current);
                } else {
                    verboseln("add to inactive");
                    checkAndAddToInactive(current);
                }               
                
                verboseln("");
                continue;
            }
            
            // find a register for current
            verbose("trying find a free reg...");
            boolean succ = tryAllocateFreeReg(current);
            if (!succ) {
            	verboseln("failed");
//            	verboseln("firstRegOnlyUse=" + current.firstRegOnlyUse());
        		verboseln("possibly spilling or evacuate");
//            	if (position == current.firstRegOnlyUse()) {
//                	// this is a split interval, its 'begin' is the first regonly use. 
//                	// we have to evacuate other intervals to allow this interval in register
//            		verboseln("require a reg, evacuate exisiting regs");
//            		evacuateRegFor(current, position);
//            	} else {
//            		// normal spilling process
//            		verboseln("possibly spilling or evacuate");
//            		allocateBlockedReg(current);
//            	}

        		allocateBlockedReg(current);
            }
            
            // check if current has a register assigned
            if (current.getPhysicalReg() != null) {
            	verboseln("physical reg assigned");
            	checkAndAddToActive(current);
            }
            
            verboseln("");                
        }
	}
	
	private void informSplitActiveInterval(Interval firstHalf, Interval secondHalf) {
		// we didnt actually split
		if (firstHalf == null || secondHalf == null)
			return;
		
		// if the two intervals are not adjacent, we do not need to insert any transfer code
		// two intervals are possibly not adjacent, if we happen to split at the edge of a lifetime hole
		if (firstHalf.getEnd() != secondHalf.getBegin() - 1)
			return;
		
		int insertionPoint = firstHalf.getEnd();
		if (insertionPoint % 2 != 0) {
			insertionPoint --;
		}
		
		Pair<Interval, Interval> p = new Pair<Interval, Interval>(firstHalf, secondHalf);
		
		List<Pair<Interval, Interval>> list = moves.get(insertionPoint);
		if (list == null) {
			list = new ArrayList<Pair<Interval, Interval>>();
		}
		list.add(p);
		moves.put(insertionPoint, list);
		
		verboseln("spliting interval, resulting in reg transfer: " + firstHalf.getOrig().prettyPrint() + " at " + insertionPoint);
	}

	private Set<MCRegister> getAllPhysicalRegisters(int dataType) {
        HashSet<MCRegister> physRegisters = new HashSet<MCRegister>();
        
        if (dataType == MCRegister.DATA_GPR) {
            // init physRegisters to all GPRs
            for (int i = 0; i < UVMCompiler.MCDriver.getNumberOfGPR(); i++) {
                MCRegister reg = currentCF.findOrCreateRegister(UVMCompiler.MCDriver.getGPRName(i), MCRegister.MACHINE_REG, dataType);
                physRegisters.add(reg);
            }
        } else if (dataType == MCRegister.DATA_DP) {
            // init physRegisters to double-precision FPRs
            for (int i = 0; i < UVMCompiler.MCDriver.getNumberOfFPR(); i++) {
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

    private boolean tryAllocateFreeReg(Interval current) {
        Set<MCRegister> physRegisters = getAllPhysicalRegisters(current.getDataType());
        
        HashMap<MCRegister, Integer> freeUntilPos = new HashMap<MCRegister, Integer>();
        HashMap<MCRegister, Interval> intervalMap = new HashMap<MCRegister, Interval>();
        
        // set freeUntilPos of all physical registers to max int
        for (MCRegister reg : physRegisters)
            freeUntilPos.put(reg, Integer.MAX_VALUE);
        
        for (Interval it : active) {
        	freeUntilPos.put(it.getPhysicalReg(), 0);
        	intervalMap.put(it.getPhysicalReg(), it);
        }
        
        for (Interval it : inactive) {
        	if (it.isFixed()) {
        		freeUntilPos.put(it.getPhysicalReg(), 0);
        		intervalMap.put(it.getPhysicalReg(), it);
        		continue;
        	}
        	
            if (it.doesIntersectWith(current)) {
            	int oldValue = freeUntilPos.get(it.getPhysicalReg());
            	int newValue = it.nextIntersectionWith(current);
            	
            	// use the minimum
            	if (newValue < oldValue) {
            		freeUntilPos.put(it.getPhysicalReg(), newValue);
            		intervalMap.put(it.getPhysicalReg(), it);
            		verboseln("inactive: " + it.getPhysicalReg().prettyPrint() + ".freeUntilPos=" + newValue + " from " + it.getOrig().prettyPrint());
            	}
            }
        }
        
        // get the register with the highest freeUntilPos
        MCRegister candidate = null;
        int highestFreeUntilPos = -1;
        for (MCRegister reg : freeUntilPos.keySet()) {
            int i = freeUntilPos.get(reg);
            if (i > highestFreeUntilPos) {
                candidate = reg;
                highestFreeUntilPos = i;
            }
        }
        
        if (highestFreeUntilPos == 0 || highestFreeUntilPos == -1)
            // no register available without spilling
            return false;
        else if (current.getEnd() < highestFreeUntilPos) {
            // register available for the whole interval
            current.setPhysicalReg(candidate);
            verboseln("Allocate " + candidate.prettyPrint() + " to virtual reg " + current.getOrig().prettyPrint());
            return true;
        }
        else {
            // register available for the first part of the interval
            current.setPhysicalReg(candidate);
            verboseln("Allocate " + candidate.prettyPrint() + " to virtual reg " + current.getOrig().prettyPrint());
            
            Interval candidateInterval = intervalMap.get(candidate);
            
            // if we use the reg from active set, split current before freeUntilPos
            if (active.contains(candidateInterval)) {
	            Interval afterSplit = current.splitAt(highestFreeUntilPos);
	            unhandled.add(afterSplit);
	            informSplitActiveInterval(current, afterSplit);
	            verboseln("Split " + current.getOrig().prettyPrint() + " at " + highestFreeUntilPos);
            } 
            // if we use the reg from inactive set, split candidate at the end of this lifetime hole
            else if (inactive.contains(candidateInterval)) {
            	int endOfHole = candidateInterval.skipLifetimeHole(current.getBegin());
            	
            	Interval afterSplit = candidateInterval.splitAt(endOfHole);
            	inactive.remove(candidateInterval);
            	unhandled.add(afterSplit);
            	verboseln("Split and move " + candidateInterval.getOrig().prettyPrint() + " from inactive to active");
            }
            else {
            	UVMCompiler.error("trying to allocate a physical reg which is not either in active set or inactive set. Problem!!");
            }
            return true;
        }
    }
    
    /**
     * evacuate a physical reg in active for current, since current needs a reg on pos (reg only use)
     * @param current
     * @param pos
     */
    private void evacuateRegFor(Interval current, int pos) {
    	int leastRegOnlyUse = Integer.MAX_VALUE;
    	MCRegister candidate = null;
    	Interval candidateInterval = null;
    	
    	for (Interval it : active) {
    		int regOnlyUse = it.regOnlyUses(pos);
    		if (!it.isFixed() && !it.isRegOnlyUseAt(pos) && regOnlyUse < leastRegOnlyUse) {
    			leastRegOnlyUse = regOnlyUse;
    			candidate = it.getPhysicalReg();
    			candidateInterval = it;
    		}
    	}
    	
    	if (candidate == null) {
    		UVMCompiler.error("need to evacuate an interval to fit in " + current.getOrig().getName() + ", but failed to find a candidate");
    	}
    	
    	// evacuate candidate
    	
    	// remove from active
    	active.remove(candidateInterval);
    	
    	// split the interval
    	Interval afterSplit = candidateInterval.splitAt(pos);
    	unhandled.add(afterSplit);
    	informSplitActiveInterval(candidateInterval, afterSplit);
    	verboseln("split " + candidateInterval.getOrig().getName() + " at " + pos);
    	
    	// then we have the reg
    	current.setPhysicalReg(candidate);
    	
    	verboseln(" evacuating " + candidateInterval.getOrig().getName() + " using " + candidate.prettyPrint() + " for " + current.getOrig().getName());
	}
    
    private void allocateBlockedReg(Interval current) {
        Set<MCRegister> physRegisters = getAllPhysicalRegisters(current.getDataType());
        
        HashMap<MCRegister, Integer> nextUsePos = new HashMap<MCRegister, Integer>();
        for (MCRegister reg : physRegisters)
            nextUsePos.put(reg, Integer.MAX_VALUE);
        
        for (Interval it : active) {
        	if (it.isFixed()) {
        		System.out.println("active: " + it.getPhysicalReg().prettyPrint() + ".nextUsePos=-1");
        		nextUsePos.put(it.getPhysicalReg(), -1);
        	} else {
        		System.out.println("active: " + it.getPhysicalReg().prettyPrint() + ".nextUsePos=" + it.nextUseAfter(current.getBegin()));
        		nextUsePos.put(it.getPhysicalReg(), it.nextUseAfter(current.getBegin()));
        	}
        }
        
        for (Interval it : inactive) {
        	if (it.isFixed()) {
        		nextUsePos.put(it.getPhysicalReg(), -1);
        		System.out.println("active: " + it.getPhysicalReg().prettyPrint() + ".nextUsePos=-1");
        		continue;
        	}
        	
            if (it.doesIntersectWith(current)) {
            	System.out.println("inactive: " + it.getPhysicalReg().prettyPrint() + ".nextUsePos=" + it.nextUseAfter(current.getBegin()));
            	int oldValue = nextUsePos.get(it.getPhysicalReg());
            	int newValue = it.nextUseAfter(current.getBegin());
            	
            	if (newValue < oldValue) {
            		nextUsePos.put(it.getPhysicalReg(), newValue);
            	}            	
            }
        }
        
        // find register with highest nextUsePos
        MCRegister candidate = null;
        int highestNextUsePos = -1;
        for (MCRegister reg : nextUsePos.keySet()) {
            int i = nextUsePos.get(reg);
            if (i > highestNextUsePos) {
                highestNextUsePos = i;
                candidate = reg;
            }
        }
//        
//        if (candidate == null) {
//        	UVMCompiler.error("failed to find register candidate");
//        }
        
//        verboseln(" highest next use pos = " + highestNextUsePos + ", candidate = " + candidate.prettyPrint());
//        verboseln(" current first use = " + current.firstUse());
        
        // if first usage of current is after nextUsePos[reg]
        if (current.firstUse() >= highestNextUsePos || candidate == null) {
            // all other intervals are used before current
            // so it is best to spill current itself
        	
        	// check if we can spill current - if the first regonly pos for current isn't its begin, we can spill it
        	boolean spillCurrent = (current.firstRegOnlyUse() != current.getBegin());
        	
        	if (spillCurrent) {
	            MCMemoryOperand spillSlot = stack.spillInterval(current);
	            current.setSpill(spillSlot); 
	        	verboseln("Spill " + current.getOrig().prettyPrint() + " to " + spillSlot.prettyPrint());
	            
	            int firstRegOnlyUse = current.firstRegOnlyUse();
	            verboseln(" current first regonly use = " + firstRegOnlyUse);
	            // if some of the uses need a reg, then we need to split it and handle the split one later
	            if (firstRegOnlyUse != -1) {
		            Interval afterSplit = current.splitAt(firstRegOnlyUse);
		            unhandled.add(afterSplit);
		            informSplitActiveInterval(current, afterSplit);
		            verboseln(" spilting at " + firstRegOnlyUse);
	            }
        	} else {
        		// we have to find a register for current
        		evacuateRegFor(current, current.getBegin());
        	}
        } else {
            // spill intervals that currently block reg
            current.setPhysicalReg(candidate);
            verboseln("ReAllocate " + candidate.prettyPrint() + " to virtual register " + current.getOrig().prettyPrint());
            
            // find the interval in active to be split
            Interval toBeSplit = null;
            for (Interval it : active) {
                if (it.getPhysicalReg() == candidate) {
                	active.remove(it);
                    toBeSplit = it;
                    break;
                }
            }
            if (toBeSplit == null)
                UVMCompiler.error("cannot find the interval to be split in active set");
            
            // split active interval for reg at position
            // FIXME: position is current.getBegin() ?
            Interval afterSplit = toBeSplit.splitAt(current.getBegin());
            unhandled.add(afterSplit);
            informSplitActiveInterval(toBeSplit, afterSplit);
            verboseln(" " + toBeSplit.getOrig().prettyPrint() + " lost its reg, will be split at " + current.getBegin());
            
            // split any inactive interval for reg at the end of its lifetime hole
            for (Interval it : inactive) {
                if (it.getPhysicalReg() == candidate) {
                    int liveAgain = it.skipLifetimeHole(current.getBegin());
                    Interval newInterval = it.splitAt(liveAgain);
                    unhandled.add(newInterval);
                    verboseln(" spilt inactive interval " + newInterval.getOrig().prettyPrint() + " at " + liveAgain);
                }
            }
        }
        
        // make sure that current does not intersect with 
        // the fixed interval for reg
        int intersectWithFixedInterval = -1;
        for (MCRegister reg : currentCF.intervals.keySet()) {
            if (reg == current.getPhysicalReg()) {
                if (intersectWithFixedInterval == -1)
                    intersectWithFixedInterval = current.nextIntersectionWith(currentCF.intervals.get(reg));
                else {
                    UVMCompiler.error("There are more than one fixed interval related with " + reg.prettyPrint() + " in " + currentCF.getOriginFunction().getName());
                }
            }
        }
        
        if (intersectWithFixedInterval != -1) {
            // split current before this intersection
            Interval split = current.splitAt(intersectWithFixedInterval);
            unhandled.add(split);
            informSplitActiveInterval(current, split);
            verboseln(" current intersect with fixed interval, split at " + intersectWithFixedInterval);
        }
    }
    
    /**
     * check if active set contains the reg already
     * @param i
     */
    private void checkAndAddToActive(Interval i) {
    	for (Interval it : active) {
    		if (it.getPhysicalReg() == i.getPhysicalReg()) {
    			System.out.println("adding interval to active, but active set contains intervals with same phys reg");
    			System.out.println(i.prettyPrint());
    			System.out.println(it.prettyPrint());
    			UVMCompiler.error("same phys reg in active set");
    		}
    	}

    	active.add(i);
    }
    
    /**
     * check if active/inactive set contains the reg already
     * @param i
     */
    private void checkAndAddToInactive(Interval i) {
    	for (Interval it : active) {
    		if (it.getPhysicalReg() == i.getPhysicalReg()) {
    			System.out.println("adding interval to inactive, but active set contains intervals with same phys reg");
    			System.out.println(i.prettyPrint());
    			System.out.println(it.prettyPrint());
    			UVMCompiler.error("same phys reg in active set");
    		}
    	}
    	
    	for (Interval it : inactive) {
    		if (it.getPhysicalReg() == i.getPhysicalReg()) {
    			System.out.println("adding interval to inactive, but inactive set contains intervals with same phys reg");
    			System.out.println(i.prettyPrint());
    			System.out.println(it.prettyPrint());
    			UVMCompiler.error("same phys reg in inactive set");
    		}
    	}
    	
    	inactive.add(i);
    }
}
