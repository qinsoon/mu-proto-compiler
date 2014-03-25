package uvm.mc;

import java.util.List;

public abstract class AbstractMachineCode {
    protected String name;
    protected List<Operand> operands;
    
    protected uvm.mc.Label label;
    
    public void setLabel(uvm.mc.Label label) {
        this.label = label;
    }
    
    public Label getLabel(uvm.mc.Label label) {
        return label;
    }
    
    public String prettyPrint() {
        StringBuilder ret = new StringBuilder();
        if (label != null)
            ret.append("#" + label.name + ":\n");
        ret.append(name + " ");
        for (int i = 0; i < operands.size(); i++) {
            Operand o = operands.get(i);
            ret.append(o.prettyPrint());
            
            if (i != operands.size() - 1)
                ret.append(", ");
        }
        return ret.toString();
    }
}
