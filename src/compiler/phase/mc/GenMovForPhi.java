package compiler.phase.mc;

import java.util.ArrayList;
import java.util.List;

import burm.mc.X64Driver;
import uvm.CompiledFunction;
import uvm.MicroVM;
import uvm.mc.AbstractMCDriver;
import uvm.mc.AbstractMachineCode;
import uvm.mc.MCBasicBlock;
import uvm.mc.MCLabel;
import uvm.mc.MCRegister;
import compiler.UVMCompiler;
import compiler.phase.CompilationPhase;

/*
 *  Check paper <Linear Scan Register Allocation in the context of SSA form and Register Constraints> Sec 4.1 
 */
public class GenMovForPhi extends CompilationPhase {

    public GenMovForPhi(String name) {
        super(name);
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
    public void execute() {
        int genMovBBIndex = 0;
        int genMovRegIndex = 0;
        
        for (CompiledFunction cf : MicroVM.v.compiledFuncs) {
            // to avoid concurernt modification on the list, we put new BB here
            // and add them after traversal
            List<MCBasicBlock> newBBs = new ArrayList<MCBasicBlock>();
            
            for (MCBasicBlock bb : cf.BBs) {
                if (bb.getPhi() == null)
                    continue;
                
                for (int j = 0; j < bb.getPredecessors().size(); ) {
                    MCBasicBlock p = bb.getPredecessors().get(j);
                    MCBasicBlock n;
                    
                    boolean newBlock = false;
                    
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
                        
                        newBlock = true;
                        
                        // reset j
                        j = 0;
                    } else {
                        n = p;
                        j++;
                    }
                    
                    for (AbstractMachineCode mc : bb.getMC()) {
                        if (mc.isPhi()) {
                            int opdForP = -1;
                            for (int i = 1; i < mc.getNumberOfOperands(); i += 2) {
                                MCLabel l = (MCLabel) mc.getOperand(i);
                                if (l.getName().equals(p.getLabel().getName()))
                                    opdForP = i - 1;
                            }
                            
                            if (opdForP != -1) {
                                MCRegister genMovReg = cf.findOrCreateRegister("gen_mov_reg" + genMovRegIndex, MCRegister.OTHER_SYMBOL_REG);
                                genMovRegIndex++;
                                
                                // i <- new RegMov(phi.opd(p))
                                AbstractMachineCode i = UVMCompiler.MCDriver.genMove(genMovReg, mc.getOperand(opdForP));
                                if (newBlock)
                                    i.setLabel(n.getLabel());
                                // phi.opd(p) <- i
                                mc.setOperand(opdForP, genMovReg);
                                mc.setOperand(opdForP + 1, n.getLabel());
                                // append i to n
                                n.addMC(i);
                                // this is arbitrary order
                                cf.addMachineCode(i);
                                // TODO join i with phi
                                // i is genMovReg
                                // phi is mc.getReg()
                                genMovReg.setREP(mc.getReg());
                            }
                        }
                    }
                }
            }   // end of for (MCBasicBlock bb : cf.BBs)
            
            if (!newBBs.isEmpty()) {
                cf.BBs.addAll(newBBs);
                newBBs.clear();
            }            
            
            System.out.println("\nAfter gen mov:\n");
            System.out.println(cf.prettyPrint());
        }
    }
}
