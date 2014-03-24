package compiler;

import java.util.List;

import burm.BURM_GENERATED;
import uvm.CompiledFunction;
import uvm.Function;
import uvm.IRTreeNode;
import uvm.Instruction;
import uvm.MicroVM;
import uvm.mc.AbstractMachineCode;

public class MachineCodeEmission {
    public static void execute() {
        for (Function f : MicroVM.v.funcs.values()) {
            CompiledFunction cf = new CompiledFunction(f);
            
            for (IRTreeNode node : f.tree) {
                List<AbstractMachineCode> mc = BURM_GENERATED.emitCode(node);
                if (node instanceof Instruction) {
                    if (((Instruction) node).getLabel() != null) {
                        mc.get(0).setLabel(new uvm.mc.Label(((Instruction) node).getLabel().getName()));
                    }
                } else UVMCompiler.error("node " + node.prettyPrint() + " is not an inst");
                
                cf.addMachineCode(mc);
            }
            
            System.out.println(f.getName() + " machine code:");
            for (AbstractMachineCode mc : cf.getMachineCode()) {
                System.out.println(mc.prettyPrint());
            }
            System.out.println();
        }
    }
}
