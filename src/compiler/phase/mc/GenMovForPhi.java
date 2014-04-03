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
import compiler.phase.CompilationPhase;

/*
 *  Check paper <Linear Scan Register Allocation in the context of SSA form and Register Constraints> Sec 4.1 
 */
public class GenMovForPhi extends CompilationPhase {

    public GenMovForPhi(String name) {
        super(name);
    }
    
    @Override
    public void execute() {
        int genMovBBIndex = 0;
        int genMovRegIndex = 0;
        
        for (CompiledFunction cf : MicroVM.v.compiledFuncs) {
            for (MCBasicBlock bb : cf.BBs) {
                if (bb.getPhi() == null)
                    continue;
                
                System.out.println("bb #" + bb.getLabel().getName() + " contains phi. ");
                
                for (int j = 0; j < bb.getPredecessors().size(); ) {
                    MCBasicBlock p = bb.getPredecessors().get(j);
                    System.out.println("check its predecessor: #" + p.getLabel().getName());
                    MCBasicBlock n;
                    if (bb.getPredecessors().size() > 1 && p.getSuccessor().size() > 1) {
                        System.out.println("inserting new bb");
                        n = new MCBasicBlock("genmov" + genMovBBIndex);
                        genMovBBIndex++;
                        
                        bb.getPredecessors().remove(p);
                        p.getSuccessor().remove(bb);
                        
                        n.addPredecessors(p);
                        p.addSuccessor(n);
                        n.addSuccessor(bb);
                        bb.addPredecessors(n);
                        
                        // reset j
                        j = 0;
                    } else {
                        n = p;
                        j++;
                    }
                    
                    for (AbstractMachineCode mc : bb.getMC()) {
                        if (mc.isPhi()) {
                            int opdForP = -1;
                            System.out.println("searching for value for " + p.getLabel().getName());
                            for (int i = 2; i < mc.getNumberOfOperands(); i += 2) {
                                MCLabel l = (MCLabel) mc.getOperand(i);
                                System.out.println("l=" + l.getName());
                                if (l.getName().equals(p.getLabel().getName()))
                                    opdForP = i - 1;
                            }
                            
                            if (opdForP != -1) {
                                MCRegister genMovReg = MCRegister.findOrCreate("gen_mov_reg" + genMovRegIndex, MCRegister.OTHER_SYMBOL_REG);
                                genMovRegIndex++;
                                
                                // TODO
                                // i <- new RegMov(phi.opd(p))
                                AbstractMachineCode i = new X64Driver().genMove(genMovReg, mc.getOperand(opdForP));
                                // phi.opd(p) <- i
                                mc.setOperand(opdForP, genMovReg);
                                // append i to n
                                n.addMC(i);
                                // TODO join i with phi
                                // ignore now
                            }
                        }
                    }
                }
            }   // end of for (MCBasicBlock bb : cf.BBs)
            
            System.out.println("\nAfter gen mov:\n");
            System.out.println(cf.prettyPrint());
        }
    }
}
