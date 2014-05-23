package uvm.mc;

import java.util.Arrays;
import java.util.HashSet;

public class MCConstant {
    public static final HashSet<MCConstant> constants = new HashSet<MCConstant>();
    private static long index = 0;
    
    public static MCConstant findOrCreateConstant(String namePrefix, long[] value) {
        for (MCConstant c : constants)
            if (Arrays.equals(c.value, value)) {
                return c;
            }
        
        MCConstant newConstant = new MCConstant(new MCLabel(namePrefix + index), value);
        index++;
        constants.add(newConstant);
        return newConstant;
    }
    
    MCLabel label;    
    long[] value;
    
    private MCConstant(MCLabel label, long[] value) {
        this.label = label;
        this.value = value;
    }

    public MCLabel getLabel() {
        return label;
    }

    public long[] getValue() {
        return value;
    }    
}
