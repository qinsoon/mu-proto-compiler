package compiler.phase.mc.linearscan;

import java.util.ArrayList;
import java.util.List;

import burm.mc.X64Driver;
import uvm.CompiledFunction;
import uvm.MicroVM;
import uvm.mc.AbstractMCDriver;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCLabel;
import uvm.mc.MCOperand;
import uvm.mc.MCRegister;
import compiler.UVMCompiler;
import compiler.phase.AbstractCompilationPhase;
import compiler.phase.mc.AbstractMCCompilationPhase;

/*
 *  Check paper <Linear Scan Register Allocation in the context of SSA form and Register Constraints> Sec 4.1 
 */
public class GenMovForPhi extends AbstractMCCompilationPhase {

    public GenMovForPhi(String name, boolean verbose) {
        super(name, verbose);
    }
    
    public void changeSuccessor(MCBasicBlock b, MCBasicBlock oldSuccessor, MCBasicBlock newSuccessor) {
        b.getSuccessor().remove(oldSuccessor);
        b.addSuccessor(newSuccessor);
        
        // check if the last inst in b jumps to oldSuccessor
        AbstractMachineCode last = b.getLast();
        if (last.isJump() && ((MCLabel)last.getOperand(0)).getName().equals(oldSuccessor.getName())) {
            last.setOperand(0, newSuccessor.getLabel());
        }
    }

    @Override
    protected void visitCompiledFunction(CompiledFunction cf) {
        int genMovBBIndex = 0;
        int genMovRegIndex = 0;
        
        if (verbose)
            cf.printDotFile("beforeGenMov");
        // to avoid concurernt modification on the list, we put new BB here
        // and add them after traversal
        List<MCBasicBlock> newBBs = new ArrayList<MCBasicBlock>();
        
        for (MCBasicBlock bb : cf.BBs) {
            if (bb.getPhi() == null)
                continue;
            
            List<MCBasicBlock> predecessors = new ArrayList<MCBasicBlock>();
            predecessors.addAll(bb.getPredecessors());
            for (MCBasicBlock p : predecessors) {
                MCBasicBlock n;
                
                boolean addingLabelForNewBlock = false;
                
                if (bb.getPredecessors().size() > 1 && p.getSuccessor().size() > 1) {
                    // insert new bb between p and bb
                    n = new MCBasicBlock("genmov" + genMovBBIndex);
                    genMovBBIndex++;
                    
                    bb.getPredecessors().remove(p);
                    bb.addPredecessors(n);
                    
                    changeSuccessor(p, bb, n);
                    
                    n.addPredecessors(p);
                    n.addSuccessor(bb);
                    
                    newBBs.add(n);
                    
                    addingLabelForNewBlock = true;
                    verboseln("Created new block #" + n.getName());
                } else {
                    n = p;
                }
                
                for (AbstractMachineCode mc : bb.getMC()) {
                    if (mc.isPhi()) {
                        verboseln("examing phi: " + mc.prettyPrintNoLabel());
                        verboseln("trying to find value from #" + p.getName());
                        int opdForP = -1;
                        for (int i = 1; i < mc.getNumberOfOperands(); i += 2) {
                            MCLabel l = (MCLabel) mc.getOperand(i);
                            if (l.getName().equals(p.getLabel().getName()))
                                opdForP = i - 1;
                        }
                        
                        if (opdForP != -1) {
                            verboseln("inserted mov");
                            MCOperand opd = mc.getOperand(opdForP);
                            int opdDataType = -1;
                            if (opd instanceof MCRegister) {
                                opdDataType = ((MCRegister) opd).getDataType();
                            } else if (opd instanceof uvm.mc.MCIntImmediate) {
                                opdDataType = MCRegister.DATA_GPR;
                            } else if (opd instanceof uvm.mc.MCDPImmediate) {
                                opdDataType = MCRegister.DATA_DP;
                            } else if (opd instanceof uvm.mc.MCSPImmediate) {
                                opdDataType = MCRegister.DATA_SP;
                            } else {
                                UVMCompiler.error("genmov for unimplemented opd type: " + opd.getClass().toString());
                            }
                            
                            MCRegister genMovReg = cf.findOrCreateRegister("gen_mov_reg" + genMovRegIndex, MCRegister.OTHER_SYMBOL_REG, opdDataType);
                            genMovRegIndex++;
                            
                            // i <- new RegMov(phi.opd(p))
                            AbstractMachineCode mov = null;
                            
                            if (opdDataType == MCRegister.DATA_GPR)
                                mov = UVMCompiler.MCDriver.genMove(genMovReg, mc.getOperand(opdForP));
                            else if (opdDataType == MCRegister.DATA_DP)
                                mov = UVMCompiler.MCDriver.genDPMove(genMovReg, mc.getOperand(opdForP));
                            else if (opdDataType == MCRegister.DATA_SP)
                                mov = UVMCompiler.MCDriver.genSPMove(genMovReg, mc.getOperand(opdForP));
                            else UVMCompiler.error("unknown data type, cant pick a mov instruction: " + opdDataType);
                            
                            if (addingLabelForNewBlock) {
                                mov.setLabel(n.getLabel());
                                addingLabelForNewBlock = false;
                            }
                            // phi.opd(p) <- i
                            mc.setOperand(opdForP, genMovReg);
                            mc.setOperand(opdForP + 1, n.getLabel());
                            // append i to n
                            if (n.getLast() != null 
                                && n.getLast().isJump() 
                                && ((MCLabel)n.getLast().getOperand(0)).getName().equals(bb.getName())) {
                                // BB n jumps to this block
                                //so we need to put the mov instruction before the jump code
                                AbstractMachineCode jmp = n.getLast();
                                n.getMC().remove(jmp);
                                if (jmp.getLabel() != null) {
                                    mov.setLabel(jmp.getLabel());
                                    jmp.setLabel(null);
                                }
                                n.addMC(mov);
                                n.addMC(jmp);
                            } else
                                // otherwise we simply add mov to n
                                n.addMC(mov);
                            
                            // this is arbitrary order
                            cf.addMachineCode(mov);
                            
                            // join i with phi
                            // i is genMovReg
                            // phi is mc.getReg()
                            genMovReg.setREP(mc.getReg());
                        } else {
                            verboseln("didnt insert mov");
                        }
                    }
                }
            }
        }   // end of for (MCBasicBlock bb : cf.BBs)
        
        if (!newBBs.isEmpty()) {
            cf.BBs.addAll(newBBs);
            newBBs.clear();
        }            
        
        verboseln("\nAfter gen mov:\n");
        verboseln(cf.prettyPrint());
        
        if (verbose)
            cf.printDotFile("afterGenMov");

    }
}
